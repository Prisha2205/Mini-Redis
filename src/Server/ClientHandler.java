package Server;

import Protocol.Command;
import Protocol.CommandParser;
import Store.DataStore;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final DataStore dataStore;

    public ClientHandler(Socket clientSocket, DataStore dataStore) {
        this.clientSocket = clientSocket;
        this.dataStore = dataStore;
    }

    @Override
    public void run() {
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();

        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream()), true)
        ) {
            Command command;
            while ((command = CommandParser.parseCommand(in)) != null) {
                String response = dispatch(command);
                out.println(response);

                if (command.getName().equals("QUIT")) break;
            }
        } catch (IOException e) {
            System.out.println("Connection error with " + clientAddress + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
            System.out.println("Client disconnected: " + clientAddress);
        }
    }

    private String dispatch(Command command) {
        String cmd = command.getName();
        List<String> args = command.getArgs();

        switch (cmd) {
            case "PING":
                return args.isEmpty() ? "PONG" : args.get(0);

            case "ECHO":
                if (args.isEmpty()) return "ERR wrong number of arguments for 'echo'";
                return String.join(" ", args);

            case "SET":
                if (args.size() < 2) return "ERR wrong number of arguments for 'set'";
                dataStore.set(args.get(0), args.get(1));
                return "OK";

            case "GET": {
                if (args.size() != 1) return "ERR wrong number of arguments for 'get'";
                String result = dataStore.get(args.get(0));
                return result != null ? result : "(nil)";
            }

            case "DEL":
                if (args.size() != 1) return "ERR wrong number of arguments for 'del'";
                return dataStore.del(args.get(0)) ? "1" : "0";

            case "EXISTS":
                if (args.size() != 1) return "ERR wrong number of arguments for 'exists'";
                return dataStore.exists(args.get(0)) ? "1" : "0";

            case "EXPIRE":
                if (args.size() != 2) return "ERR wrong number of arguments for 'expire'";
                try {
                    long seconds = Long.parseLong(args.get(1));
                    return dataStore.expire(args.get(0), seconds) ? "1" : "0";
                } catch (NumberFormatException e) {
                    return "ERR value is not an integer or out of range";
                }

            case "QUIT":
                return "OK";

            default:
                return "ERR unknown command '" + cmd + "'";
        }
    }
}
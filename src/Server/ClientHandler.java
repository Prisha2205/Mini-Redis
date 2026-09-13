package Server;

import Protocol.Command;
import Protocol.CommandParser;
import Store.DataStore;
import Store.CommandExecutor;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final DataStore dataStore;
    private final CommandExecutor executor;

    public ClientHandler(Socket clientSocket, DataStore dataStore, CommandExecutor executor) {
        this.clientSocket = clientSocket;
        this.dataStore = dataStore;
        this.executor = executor;
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

            case "GET": {
                if (args.size() != 1) return "ERR wrong number of arguments for 'get'";
                String result = dataStore.get(args.get(0));
                return result != null ? result : "(nil)";
            }

            case "EXISTS":
                if (args.size() != 1) return "ERR wrong number of arguments for 'exists'";
                return dataStore.exists(args.get(0)) ? "1" : "0";

            case "QUIT":
                return "OK";

            case "SET":
            case "DEL":
            case "EXPIRE": {
                String result = executor.execute(command);
                return result; // executor always handles these three
            }

            default:
                return "ERR unknown command '" + cmd + "'";
        }
    }
}
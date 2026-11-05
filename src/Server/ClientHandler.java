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
                String response;
                try {//this try-catch block handles null pointer or random exceptions-> idk what exactly READ!!
                    response = dispatch(command);
                } catch (RuntimeException e) {
                    System.out.println(
                            "Error handling " + command.getName() + ": " + e
                    );

                    response = "-ERR internal error\r\n";
                }

                // Responses already contain their RESP line terminators.
                // Use print() so no extra line separator is appended.
                out.print(response);
                out.flush();

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
                String pong= args.isEmpty() ? "PONG" : args.get(0);
                return "+" + pong + "\r\n";

            case "ECHO":
                if (args.isEmpty()) return "-ERR wrong number of arguments for 'echo'\r\n";
                String e= String.join(" ", args);
                return "$" + e.length() + "\r\n" + e + "\r\n";

            case "GET": {
                if (args.size() != 1) return "-ERR wrong number of arguments for 'get'\r\n";
                String result = dataStore.get(args.get(0));
                if (result == null) {
                    return "$-1\r\n"; // RESP representation for nil / null
                }
                return "$" + result.length() + "\r\n" + result + "\r\n";
            }

            case "EXISTS":
                if (args.size() != 1) return "-ERR wrong number of arguments for 'exists'\r\n";
                return ":" + (dataStore.exists(args.get(0)) ? 1 : 0) + "\r\n";

            case "QUIT":
                return "+OK\r\n";

            case "SET":
            case "DEL":
            case "EXPIRE": {
                String result = executor.execute(command);
                return result; // executor always handles these three
            }

            default:
                return "-ERR unknown command '" + cmd + "'\r\n";
        }
    }
}

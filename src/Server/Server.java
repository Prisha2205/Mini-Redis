package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import Store.DataStore

public class Server {

    private static final int PORT = 6380;

    public static void main(String[] args) throws IOException {

        // Create a server socket that listens on port 6380
        ServerSocket serverSocket = new ServerSocket(PORT);
        DataStore dataStore = new DataStore();
        System.out.println("Mini-Redis listening on port " + PORT);

        while (true) {

            // Wait until a client connects
            Socket clientSocket = serverSocket.accept();

            System.out.println(
                    "Client connected: " + clientSocket.getInetAddress()
            );

            // Give this client its own thread
            Thread clientThread =
                    new Thread(new ClientHandler(clientSocket,dataStore));

            clientThread.start();
        }
    }
}
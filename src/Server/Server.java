package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import Store.*;
import Persistence.*;

public class Server {

    private static final int PORT = 6380;

    public static void main(String[] args) throws IOException {

        // Create a server socket that listens on port 6380
        ServerSocket serverSocket = new ServerSocket(PORT);
        DataStore dataStore = new DataStore();
        AofWriter aofWriter = new AofWriter("src/Commands.txt");
        CommandExecutor executor = new CommandExecutor(dataStore, aofWriter);
        private static final String AOF_FILE = "src/Commands.txt";

        AofReplayer replayer = new AofReplayer(AOF_FILE, executor, dataStore);
        replayer.replay();
        System.out.println("Mini-Redis listening on port " + PORT);

        while (true) {

            // Wait until a client connects
            Socket clientSocket = serverSocket.accept();

            System.out.println(
                    "Client connected: " + clientSocket.getInetAddress()
            );

            // Give this client its own thread
            Thread clientThread =
                    new Thread(new ClientHandler(clientSocket,dataStore,executor));

            clientThread.start();
        }
    }
}
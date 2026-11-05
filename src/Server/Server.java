package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Store.*;
import Persistence.*;

public class Server {

    private static final int DEFAULT_PORT = 6380;
    private static final String DEFAULT_AOF_FILE = "data/appendonly.aof";
    private static final int DEFAULT_MAX_KEYS = 1000;

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? fallback : v;
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(env("PORT", String.valueOf(DEFAULT_PORT)));
        String aofPath = env("AOF_PATH", DEFAULT_AOF_FILE);
        int maxKeys = Integer.parseInt(env("MAX_KEYS", String.valueOf(DEFAULT_MAX_KEYS)));
        System.out.println("1. Starting server");

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("2. ServerSocket created");

        DataStore dataStore = new DataStore(maxKeys);
        System.out.println("3. DataStore created");

        AofWriter aofWriter = new AofWriter(aofPath);
        System.out.println("4. AofWriter created");

        CommandExecutor executor = new CommandExecutor(dataStore, aofWriter);
        System.out.println("5. CommandExecutor created");

        AofReplayer replayer = new AofReplayer(aofPath, executor, dataStore);
        System.out.println("6. AofReplayer created");

        aofWriter.setEnabled(false);
        replayer.replay();
        aofWriter.setEnabled(true);
        System.out.println("7. Replay finished");

        System.out.println("Mini-Redis listening on port " + port);
        ExecutorService threadPool = Executors.newFixedThreadPool(50);
        while (true) {
            System.out.println("Waiting for client...");

            Socket clientSocket = serverSocket.accept();

            System.out.println(
                    "Client connected: " + clientSocket.getInetAddress()
            );


            threadPool.submit(new ClientHandler(clientSocket, dataStore, executor));;
        }
    }
}
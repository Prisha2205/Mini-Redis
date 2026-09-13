package Persistence;

import java.io.*;

public class AofWriter {

    private final BufferedWriter writer;

    public AofWriter(String filePath) throws IOException {
        // true = append mode: don't overwrite the file if it already exists,
        // just keep adding to the end of it (important — otherwise every
        // server restart would wipe your entire history!)
        this.writer = new BufferedWriter(new FileWriter(filePath, true));
    }

    public synchronized void log(String command) {
        try {
            writer.write(command);
            writer.newLine();
            writer.flush();   // force it to disk immediately (see note below)
        } catch (IOException e) {
            System.out.println("AOF write failed: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        writer.close();
    }
}
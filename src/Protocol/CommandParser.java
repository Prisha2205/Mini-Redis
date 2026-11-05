package Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandParser {
    private static final int MAX_ARGS = 1024;
    private static final int MAX_BULK_LEN = 1024 * 1024; // 1 MB per value
    /**
     * Reads one command from the stream. Returns null on clean EOF.
     * Supports RESP arrays (*N\r\n$len\r\ndata\r\n ...) and, as a fallback,
     * plain inline text (space-separated) for manual testing via telnet/nc.
     */
    public static Command parseCommand(BufferedReader reader) throws IOException {
        String line;
        do {
            line = reader.readLine();
            if (line == null) return null; // client disconnected
        } while (line.isEmpty());

        List<String> tokens = (line.charAt(0) == '*')
                ? parseRespArray(reader, line)
                : parseInline(line);

        if (tokens.isEmpty()) return parseCommand(reader);

        String name = tokens.get(0);
        List<String> args = tokens.subList(1, tokens.size());
        return new Command(name, args);
    }

    private static List<String> parseRespArray(BufferedReader reader, String header) throws IOException {
        int numArgs;
        try {
            numArgs = Integer.parseInt(header.substring(1).trim());
        } catch (NumberFormatException e) {
            throw new IOException("Invalid RESP array header: " + header);
        }
        if (numArgs <= 0) return new ArrayList<>();
        if (numArgs > MAX_ARGS) {
            throw new IOException("Too many arguments: " + numArgs);
        }

        List<String> tokens = new ArrayList<>(numArgs);
        for (int i = 0; i < numArgs; i++) {
            String bulkHeader = reader.readLine();
            if (bulkHeader == null || bulkHeader.isEmpty() || bulkHeader.charAt(0) != '$') {
                throw new IOException("Expected bulk string header, got: " + bulkHeader);
            }
            int len = Integer.parseInt(bulkHeader.substring(1).trim());
            if (len == -1) {
                tokens.add(null);
                continue;
            }
            if (len < 0 || len > MAX_BULK_LEN) {
                throw new IOException("Invalid bulk length: " + len);
            }

            char[] buf = new char[len];
            int read = 0;
            while (read < len) {
                int r = reader.read(buf, read, len - read);
                if (r == -1) throw new IOException("Unexpected EOF reading bulk string");
                read += r;
            }
            tokens.add(new String(buf));
            reader.readLine(); // consume trailing \r\n
        }
        return tokens;
    }

    private static List<String> parseInline(String line) {
        List<String> tokens = new ArrayList<>();
        for (String part : line.trim().split("\\s+")) {
            if (!part.isEmpty()) tokens.add(part);
        }
        return tokens;
    }
}
package Store;

import Protocol.Command;
import Persistence.AofWriter;

import java.util.List;

public class CommandExecutor {

    private final DataStore dataStore;
    private final AofWriter aofWriter;

    public CommandExecutor(DataStore dataStore, AofWriter aofWriter) {
        this.dataStore = dataStore;
        this.aofWriter = aofWriter;
    }

    // returns null if this command isn't a mutating command this executor handles
    public String execute(Command command) {
        String cmd = command.getName();
        List<String> args = command.getArgs();

        switch (cmd) {
            case "SET":
                if (args.size() < 2) return "ERR wrong number of arguments for 'set'";
                dataStore.set(args.get(0), args.get(1));
                aofWriter.log(command.toLogString());
                return "OK";

            case "DEL": {
                if (args.size() != 1) return "ERR wrong number of arguments for 'del'";
                boolean removed = dataStore.del(args.get(0));
                if (removed) aofWriter.log(command.toLogString());
                return removed ? "1" : "0";
            }

            case "EXPIRE": {
                if (args.size() != 2) return "ERR wrong number of arguments for 'expire'";
                try {
                    long seconds = Long.parseLong(args.get(1));
                    boolean success = dataStore.expire(args.get(0), seconds);
                    if (success) {
                        long expiryTimestamp = System.currentTimeMillis() + seconds * 1000;
                        aofWriter.log("EXPIRE " + args.get(0) + " " + expiryTimestamp + " ABS");
                    }
                    return success ? "1" : "0";
                } catch (NumberFormatException e) {
                    return "ERR value is not an integer or out of range";
                }
            }

            default:
                return null; // not a mutating command — caller should handle it elsewhere
        }
    }
}
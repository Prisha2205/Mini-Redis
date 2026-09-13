package Protocol;

import java.util.List;

public class Command {
    private final String name;
    private final List<String> args;

    public Command(String name, List<String> args) {
        this.name = name.toUpperCase();
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public List<String> getArgs() {
        return args;
    }

    public int argCount() {
        return args.size();
    }

    public String arg(int i) {
        return args.get(i);
    }
    public String toLogString() {
        return name + " " + String.join(" ", args);
    }
}
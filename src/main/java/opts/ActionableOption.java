package opts;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public abstract class ActionableOption {

    protected String name;
    protected String description;
    protected boolean hasArgs;
    protected Options options;

    public ActionableOption() {}

    public Options addOption(Options opts) {
        options = opts;
        options.addOption(name, hasArgs, description);
        return opts;
    }

    public abstract boolean execute(CommandLine cmd);

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean argsEnabled() {
        return hasArgs;
    }

}

package opts;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/*Class that models the commands/options. Requires a few parameters to work:

name: name of command to be referenced by command line interface.
description: description of command printed to help menu
hasArgs: true if command requires arguments, false if there is no need.


*/
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

    //Must be have an override. This is the method Commander.java calls when executing commmand.
    public abstract boolean execute(CommandLine cmd) throws Exception;

    //Standard getters + setters
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

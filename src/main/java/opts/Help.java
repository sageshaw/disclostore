package opts;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

public class Help extends ActionableOption {

    public Help() {
        super();
        name = "help";
        description = "prints help menu";
        hasArgs = false;
    }

    @Override
    public boolean execute(CommandLine cmd) {
        execute();
        return false;
    }

    public void execute() {
        HelpFormatter menu = new HelpFormatter();
        menu.printHelp("disclostore", options);
    }
}

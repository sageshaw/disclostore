package opts;

import exec.Database;
import org.apache.commons.cli.CommandLine;

public class AddFile extends ExtraArgOption {

    public AddFile() {
        super();
        name = "addfile";
        description = "sets up a file for storage. parameter: filename (ex: name.pdf)";
        hasArgs = true;
    }

    @Override
    public boolean execute(CommandLine cmd) throws Exception {

        String propertyName = getExtraArg("Enter property name: ");

        Database.getInstance().addFile(propertyName, cmd.getOptionValue(name));
        return true;

    }
}

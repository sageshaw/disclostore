package opts;

import exec.Gateway;
import org.apache.commons.cli.CommandLine;

public class AddFile extends ActionableOption {

    public AddFile() {
        super();
        name = "addfile";
        description = "sets up a file for storage. parameter: filename";
        hasArgs = true;
    }

    @Override
    public boolean execute(CommandLine cmd) throws Exception {

        Gateway.storage.addFile("123MainSt", cmd.getOptionValue(name));
        return true;

    }
}

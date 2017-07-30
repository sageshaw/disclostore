package opts;

import exec.Gateway;
import org.apache.commons.cli.CommandLine;

public class AddPropertyMetadata extends ActionableOption {

    public AddPropertyMetadata() {
        super();
        name = "addmetadata";
        description = "add default metadata to property. parameter: property name.";
        hasArgs = true;
    }

    @Override
    public boolean execute(CommandLine cmd) throws Exception {
        System.out.println("Adding metadata to: '" + cmd.getOptionValue(name) + "'");
        Gateway.storage.addPropertyMetadata(cmd.getOptionValue(name), "creator", Gateway.credentials.getAddress());
        return true;
    }
}

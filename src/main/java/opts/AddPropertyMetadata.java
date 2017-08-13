package opts;

import exec.Database;
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
        String propertyName = cmd.getOptionValue(name).toUpperCase();

        System.out.println("Adding metadata to: '" + propertyName + "'");
        Database.getInstance().addPropertyMetadata(propertyName, "creator", Gateway.credentials.getAddress());
        return true;
    }
}

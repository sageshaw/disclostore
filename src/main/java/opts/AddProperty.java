package opts;

import exec.Gateway;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class AddProperty extends PropertyOption {

    public AddProperty() {
        super();
        name = "addproperty";
        description = "add property to the database."; //TODO: enable security feature to prevent overwriting
        hasArgs = true;
    }

    @Override
    public boolean execute(CommandLine cmd) throws ExecutionException, InterruptedException, IOException {
        System.out.println("Creating property '" + cmd.getOptionValue(name) + "'");
        Gateway.storage.addProperty(cmd.getOptionValue(name));


        return true;
    }
}

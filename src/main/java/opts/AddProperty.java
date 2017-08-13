package opts;

import exec.Database;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class AddProperty extends ExtraArgOption {

    public AddProperty() {
        super();
        name = "addproperty";
        description = "add property to the database. This will overwrite any existing property with the same name";
        hasArgs = true;
    }

    @Override
    public boolean execute(CommandLine cmd) throws ExecutionException, InterruptedException, IOException {
        String propertyName = cmd.getOptionValue(name).toUpperCase();
        System.out.println("Creating property '" + propertyName + "'");
        Database.getInstance().addProperty(propertyName);


        return true;
    }
}

package opts;

import exec.Database;
import org.apache.commons.cli.CommandLine;

public class GetMetadata extends ActionableOption {

    public GetMetadata() {
        super();
        name = "getmetadata";
        description = "get property metadata. parameter: assigned name";
        hasArgs = true;
    }

    @Override
    public boolean execute(CommandLine cmd) throws Exception {

        String metadata = Database.getInstance().getPropertyMetadata(cmd.getOptionValue(name).toUpperCase(), "creator");

        System.out.println("Creator metadata: " + metadata);


        return true;
    }
}

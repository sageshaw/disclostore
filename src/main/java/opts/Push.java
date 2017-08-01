package opts;

import exec.FileTools;
import exec.Gateway;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Push extends ActionableOption{

    public Push() {
        super();
        name = "push";
        description = "push file to blockchain. parameter: file path";
        hasArgs = true;
    }

    public boolean execute(CommandLine cmd) throws IOException, ExecutionException, InterruptedException {

        File file = new File(cmd.getOptionValue(name));

        byte[] otherData = FileTools.encodeFileRaw(file);

        Gateway.storage.pushData(otherData);
        //Note: current contract can only hold one File (only has one array). That's why there is no need to specify
        //associated property.

        return true;
    }


}

package opts;

import exec.FileTools;
import exec.Gateway;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Push extends PropertyOption {

    public Push() {
        super();
        name = "push";
        description = "push file to blockchain. parameter: file path";
        hasArgs = true;
    }

    public boolean execute(CommandLine cmd) throws IOException, ExecutionException, InterruptedException {

        File file = new File(cmd.getOptionValue(name));

        byte[][] data = FileTools.encodeFile(file);

        String property = getProperty();

        Gateway.storage.pushData(property, file.getName(), data);

        return true;
    }


}

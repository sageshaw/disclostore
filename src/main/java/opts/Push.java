package opts;

import exec.Database;
import exec.FileTools;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Push extends ExtraArgOption {

    public Push() {
        super();
        name = "push";
        description = "push file to blockchain. parameter: file path";
        hasArgs = true;
    }

    public boolean execute(CommandLine cmd) throws IOException, ExecutionException, InterruptedException {

        File file = new File(cmd.getOptionValue(name));

        byte[][] data = FileTools.encodeFile(file);

        String property = getExtraArg("Enter property name: ");

        Database.getInstance().pushData(property, file.getName(), data);

        return true;
    }


}

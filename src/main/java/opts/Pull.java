package opts;

import exec.FileTools;
import exec.Gateway;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Pull extends ExtraArgOption {


    public Pull() {
        super();
        name = "pull";
        description = "pull file from blockchain. parameter: directory for file to be started, including output filename and extension";
        hasArgs = true;
    }

    @Override
    public boolean execute(CommandLine cmd) throws ExecutionException, InterruptedException, IOException {

        //TODO: implement property/file existence checks

        String property = getExtraArg("Enter property name: ");
        String fileName = cmd.getOptionValue(name);

        byte[][] data = Gateway.storage.pullData(property, fileName.substring(fileName.lastIndexOf("/") + 1));

        File file = FileTools.decodeFile(fileName, data);


        return true;
    }
}

package opts;

import exec.Database;
import exec.FileTools;
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


        String property = getExtraArg("Enter property name: ");
        String fileName = cmd.getOptionValue(name);

        byte[][] data = Database.getInstance().pullData(property, fileName.substring(fileName.lastIndexOf("/") + 1));

        //pullData will return a multidimensional array with -1 if error occured. Check and return false if necessary
        if (data[0][0] < 0) return false;

        File file = FileTools.decodeFile(fileName, data);


        return true;
    }
}

package opts;

import exec.FileTools;
import exec.Gateway;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Pull extends ActionableOption {


    public Pull() {
        super();
        name = "pull";
        description = "pull file from blockchain. parameter: assigned name at push";
        hasArgs = true;
    }

    @Override
    public boolean execute(CommandLine cmd) throws ExecutionException, InterruptedException, IOException {
        //TODO: implement for other properties as well

        byte[][] data = new byte[100000][32]; //TODO find a way to a more accurate array creation

        int btSeg = 0;

        do {

            data[btSeg] = Gateway.storage.pullData("123 Main St", "test", btSeg);
            btSeg++;

        } while (Gateway.storage.pullData("123 Main St", "test", btSeg) != null);

        File file = FileTools.decodeFile("test.txt", data);


        return true;
    }
}

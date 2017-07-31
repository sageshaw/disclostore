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
        byte[][] data = FileTools.encodeFile(file);


        for (int btSeg = 0; btSeg < data.length; btSeg++) {     //TODO implement for other properties as well
            Gateway.storage.pushData("123MainSt", "test", data[btSeg], btSeg);
        }


        return true;
    }


}

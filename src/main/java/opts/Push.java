package opts;

import exec.FileTools;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;

public class Push extends ActionableOption{

    public Push() {
        super();
        name = "push";
        description = "push file to blockchain. parameter: file path";
        hasArgs = true;
    }

    public boolean execute(CommandLine cmd) throws IOException {

        File file = new File(cmd.getOptionValue(name));
        byte[][] data = FileTools.encodeFile(file);


        return true;
    }


}

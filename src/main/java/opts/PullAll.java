package opts;

import org.apache.commons.cli.CommandLine;

public class PullAll extends ActionableOption {


    public PullAll() {
        super();
        name = "pullall";
        description = "pull all files from blockchain";
        hasArgs = false;
    }

    @Override
    public boolean execute(CommandLine cmd) {
        System.out.println("I'll pull all your files for you.... someday, :("); //TODO: implement pull all
        return false;
    }
}

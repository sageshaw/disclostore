package opts;

import org.apache.commons.cli.CommandLine;

public class PullOps extends ActionableOption {

    public PullOps() {
        super();
        name = "pullops";
        description = "list all files stored on blockchain";
        hasArgs = false;
    }

    @Override
    public boolean execute(CommandLine cmd) {
        System.out.println("I WILL GET YOUR LIST OPTIONS... someday. :("); //TODO: implement pull-options

        return false;
    }
}

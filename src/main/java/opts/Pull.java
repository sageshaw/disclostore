package opts;

import org.apache.commons.cli.CommandLine;

public class Pull extends ActionableOption {


    public Pull() {
        super();
        name = "pull";
        description = "pull file from blockchain. parameter: assigned name at push";
        hasArgs = true;
    }

    @Override
    public boolean execute(CommandLine cmd) {
        System.out.println("I WILL PULL FOR YOU... someday. :("); //TODO: implement pull

        return false;
    }
}

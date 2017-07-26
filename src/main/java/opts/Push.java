package opts;

import org.apache.commons.cli.CommandLine;

public class Push extends ActionableOption{

    public Push() {
        super();
        name = "push";
        description = "push file to blockchain. parameter: file path";
        hasArgs = true;
    }

    public boolean execute(CommandLine cmd) {

        System.out.println("I WILL PUSH YOU... someday :("); //TODO: implement push command

        return true;
    }

}

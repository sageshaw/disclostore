package exec;

import opts.*;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.List;

public class Commander {

    private Options options;
    private CommandLineParser parser;
    private CommandLine result;
    private List<ActionableOption> optionList = new ArrayList<ActionableOption>();

    public Commander() {
        optionList.add(new Help());
        optionList.add(new Push());
        optionList.add(new Pull());
        optionList.add(new PullAll());
        optionList.add(new PullOps());
        optionList.add(new AddProperty());
        optionList.add(new AddPropertyMetadata());
        optionList.add(new GetMetadata());

        options = new Options();
        for (int i = 0; i < optionList.size(); i++) {
            optionList.get(i).addOption(options);
        }

        parser = new DefaultParser();

    }

    public void parseCommand(String[] args)  {
        try {
            result = parser.parse(options, args);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Bugger. Invalid args. Printing help menu.\n");
            new HelpFormatter().printHelp("disclostore", options); //TODO: Find a way to use help op instead

        }


        for (int i = 0; i < optionList.size(); i++) {
            if (result.hasOption(optionList.get(i).getName())) {
                try {
                    optionList.get(i).execute(result);
                } catch (Exception e) {
                    System.out.println("Error processing your request.");
                    e.printStackTrace();
                }
                return;
            }
        }

        System.out.println("Invalid or empty command. Printing help menu.\n");
        new HelpFormatter().printHelp("disclostore", options);


    }

}

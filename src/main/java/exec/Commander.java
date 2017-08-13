package exec;

import opts.*;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.List;

public class Commander {

    private Options options;            //options object to hold command data for parsing (Apache Commons CLI)
    private CommandLineParser parser;   //actual parser
    private CommandLine result;         //the object that holds the parsed information
    private List <ActionableOption> optionList = new ArrayList <ActionableOption>(); //holds all available options defined as ActionableOptions
    //this holds the commands

    public Commander() {
        //The commands added to list. To disable a command in code, simply commend out the corresponding add statement
        optionList.add(new Help());
        optionList.add(new Push());
        optionList.add(new Pull());
        optionList.add(new AddProperty());
        optionList.add(new AddPropertyMetadata());
        optionList.add(new GetMetadata());
        optionList.add(new AddFile());

        //Loop through list and add them to options object for use.
        options = new Options();
        for (int i = 0; i < optionList.size(); i++) {
            optionList.get(i).addOption(options);
        }

        parser = new DefaultParser();

    }

    //Parses commands and runs with their required execute method.
    public void parseCommand(String[] args)  {
        try {
            result = parser.parse(options, args);
        } catch (Exception e) {
            e.printStackTrace();
            /* If there was an invalid argument, just print the help menu. */
            System.out.println("Bugger. Invalid args. Printing help menu.\n");
            new HelpFormatter().printHelp("disclostore", options); //TODO: Find a way to use help op instead (new design pattern?)

        }

        //Loop through option list to find the specified command and execute
        for (int i = 0; i < optionList.size(); i++) {
            if (result.hasOption(optionList.get(i).getName())) {
                try {
                    optionList.get(i).execute(result);
                } catch (Exception e) { //This is the catch-all error handler for any error in the request.
                    System.out.println("Error processing your request.");
                    e.printStackTrace();
                }
                return;
            }
        }

        //If the command specified was not found, just print the help menu.
        System.out.println("Invalid or empty command. Printing help menu.\n");
        new HelpFormatter().printHelp("disclostore", options);


    }

}

package opts;

import java.util.Scanner;

abstract class ExtraArgOption extends ActionableOption {

    //If an option requires an option past the single option that can be read by Apache Commons CLI, a new way
    //to get user input is required. This the class that will provide the infrastructure to take extra arguments
    //via Scanner

    String getExtraArg(String prompt) {
        Scanner input = new Scanner(System.in);
        System.out.println(prompt);
        String property = input.nextLine();
        input.close();

        return property.trim().toUpperCase();

    }

}

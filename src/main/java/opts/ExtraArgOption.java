package opts;

import java.util.Scanner;

public abstract class ExtraArgOption extends ActionableOption {

    protected String getExtraArg(String prompt) {
        Scanner input = new Scanner(System.in);
        System.out.println(prompt);
        String property = input.nextLine();
        input.close();

        return property.trim().toUpperCase(); //TODO: Add property validity checking

    }

}

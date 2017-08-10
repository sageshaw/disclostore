package opts;

import java.util.Scanner;

public abstract class PropertyOption extends ActionableOption {

    protected String getProperty() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter property name: ");
        String property = input.nextLine();

        return property.trim().toUpperCase(); //TODO: Add property validity checking
    }

}

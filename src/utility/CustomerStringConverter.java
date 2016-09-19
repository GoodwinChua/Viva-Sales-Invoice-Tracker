package utility;

import javafx.util.StringConverter;
import model.Customer;

/**
 * Created by Goodwin Chua on 14 Sep 2016.
 */
public class CustomerStringConverter extends StringConverter<Customer> {
    @Override
    public String toString(Customer object) {
        if ( object == null ) {
            return "";
        }
        return object.getName();
    }

    @Override
    public Customer fromString(String string) {
        return null;
    }
}

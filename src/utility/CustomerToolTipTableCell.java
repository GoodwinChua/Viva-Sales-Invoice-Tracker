package utility;

import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import model.Customer;

/**
 * Created by Goodwin Chua on 14 Sep 2016.
 */
public class CustomerToolTipTableCell extends TableCell<Customer, String> {
    @Override
    protected void updateItem(String customer, boolean empty) {
        super.updateItem(customer, empty);
        if ( customer == null || empty ) {
            setText(null);
            setTooltip(null);
            setStyle("");
        } else {
            if ( customer.length() <= 0 ) {
                setText(null);
                setTooltip(null);
                setStyle("");
            } else {
                Tooltip tip = new Tooltip(customer);
                setTooltip(tip);
                setText(customer);
            }
        }
    }
}

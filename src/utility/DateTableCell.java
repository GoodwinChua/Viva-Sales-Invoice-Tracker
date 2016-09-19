package utility;

import javafx.scene.control.TableCell;
import model.Invoice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by Goodwin Chua on 15 Sep 2016.
 */
public class DateTableCell extends TableCell<Invoice, LocalDate> {

    DateTimeFormatter myDateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Override
    protected void updateItem(LocalDate item, boolean empty) {
        super.updateItem(item, empty);
        if ( item == null || empty ) {
            setText(null);
            setStyle("");
        } else {
            // Format date.
            setText(myDateFormatter.format(item));
        }
    }
}

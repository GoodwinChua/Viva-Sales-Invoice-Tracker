package utility;

import javafx.scene.control.TableCell;
import model.Invoice;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by Goodwin Chua on 15 Sep 2016.
 */
public class AmountTableCell extends TableCell<Invoice, Number> {
    @Override
    protected void updateItem(Number item, boolean empty) {
        super.updateItem(item, empty);
        if ( item == null || empty ) {
            setText(null);
            setStyle("");
        } else {
            NumberFormat df = DecimalFormat.getInstance();
            df.setMinimumFractionDigits(2);
            setText(df.format(item.doubleValue()));
        }
    }
}

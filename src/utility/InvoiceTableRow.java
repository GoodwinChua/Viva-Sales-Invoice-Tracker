package utility;

import javafx.scene.control.TableRow;
import model.Invoice;

/**
 * Created by Goodwin Chua on 15 Sep 2016.
 */
public class InvoiceTableRow extends TableRow<Invoice> {
    @Override
    public void updateItem(Invoice item, boolean empty) {
        super.updateItem(item, empty);
        if ( item == null ) {
            setStyle("");
        } else if ( item.isCancelled() ) {
            setStyle("-fx-text-background-color: #3300cc;");
        } else if ( item.isPaid() ) {
            setStyle("-fx-text-background-color: #cc3300;");
        } else if ( !item.isPaid() ) {
            setStyle("-fx-text-background-color: black;");
        } else {
            setStyle("");
        }
    }
}

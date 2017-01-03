package utility;

import javafx.css.PseudoClass;
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
        } else if ( item.getPo().equals("Total") ) {
            setStyle("-fx-font-weight: bold;");
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

    @Override
    public void updateIndex(int index) {
        super.updateIndex(index);
        PseudoClass lastRow = PseudoClass.getPseudoClass("last-row");
        pseudoClassStateChanged(lastRow,
                index >= 0 && index == getTableView().getItems().size() - 1);
    }
}

package utility;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import model.Invoice;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by Goodwin Chua on 16 Sep 2016.
 */
public class BalanceAmountTableCell extends TableCell<Invoice, Number> {

    private TableColumn<Invoice, Number> param;

    public BalanceAmountTableCell(TableColumn<Invoice, Number> param) {
        this.param = param;
    }

    @Override
    protected void updateItem(Number item, boolean empty) {
        if ( item == null || empty ) {
            setText(null);
        } else {
            int currentIndex = indexProperty()
                    .getValue() < 0 ? 0
                    : indexProperty().getValue();
            Invoice type = param.getTableView().getItems().get(currentIndex);

            NumberFormat df = DecimalFormat.getInstance();
            df.setMinimumFractionDigits(2);
            if ( type.isPaid() ) {
                setText("(" + df.format(item) + ")");
            } else {
                setText(df.format(item));
            }
        }
    }
}

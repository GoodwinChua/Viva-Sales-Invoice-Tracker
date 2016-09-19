package utility;

import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import model.Invoice;

/**
 * Created by Goodwin Chua on 14 Sep 2016.
 */
public class ToolTipTableCell extends TableCell<Invoice, String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if ( item == null || empty ) {
            setText(null);
            setTooltip(null);
            setStyle("");
        } else {
            if ( item.length() > 0 ) {
                Tooltip tip = new Tooltip(item);
                setTooltip(tip);
                setText(item);
            } else {
                setText(null);
                setTooltip(null);
                setStyle("");
            }
        }
    }
}

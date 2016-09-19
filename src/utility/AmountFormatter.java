package utility;

import javafx.scene.control.TextFormatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.function.UnaryOperator;

/**
 * Created by Goodwin Chua on 14 Sep 2016.
 */
public class AmountFormatter implements UnaryOperator<TextFormatter.Change> {

    private NumberFormat format;

    public AmountFormatter() {
        format = DecimalFormat.getInstance();
        format.setMinimumFractionDigits(2);
    }

    @Override
    public TextFormatter.Change apply(TextFormatter.Change c) {
        if ( c.getControlNewText().isEmpty() ) {
            return c;
        }

        ParsePosition parsePosition = new ParsePosition(0);
        Object object = format.parse(c.getControlNewText(), parsePosition);

        if ( object == null || parsePosition.getIndex() < c.getControlNewText().length() ) {
            return null;
        } else {
            return c;
        }
    }
}
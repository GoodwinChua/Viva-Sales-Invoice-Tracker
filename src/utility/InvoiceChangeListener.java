package utility;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;


/**
 * Created by Goodwin Chua on 14 Sep 2016.
 */
public class InvoiceChangeListener implements ChangeListener<String> {

    public JFXTextField textField;

    public InvoiceChangeListener(JFXTextField textField){
        this.textField = textField;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if ( !newValue.matches("\\d") ) {
            textField.setText(newValue.replaceAll("[^\\d]", ""));
        }
        if ( newValue.length() > 5 ) {
            textField.setText(oldValue);
        }
    }
}

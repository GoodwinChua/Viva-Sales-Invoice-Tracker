package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import utility.Locator;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class WelcomeController implements Initializable {

    @FXML
    private BorderPane content;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void showEntries(ActionEvent actionEvent) throws IOException {
        Pane entriesPane = FXMLLoader.load(getClass().getResource(Locator.LOCATION + "invoice.fxml"));
        entriesPane.getStylesheets().add("resources/invoice.css");
        content.setCenter(entriesPane);
    }

    @FXML
    public void showCustomers(ActionEvent actionEvent) throws IOException {
        AnchorPane entriesPane = FXMLLoader.load(getClass().getResource(Locator.LOCATION + "customers.fxml"));
        entriesPane.getStylesheets().add("resources/invoice.css");
        content.setCenter(entriesPane);
    }

    @FXML
    public void showStatements(ActionEvent actionEvent) throws IOException {
        Pane entriesPane = FXMLLoader.load(getClass().getResource(Locator.LOCATION + "balance.fxml"));
        entriesPane.getStylesheets().add("resources/invoice.css");
        content.setCenter(entriesPane);
    }
}

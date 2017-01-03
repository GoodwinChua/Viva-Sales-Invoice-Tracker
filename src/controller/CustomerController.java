package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import db.CustomerDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import model.Customer;
import utility.CustomerToolTipTableCell;
import utility.MaskField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Goodwin Chua on 8 Sep 2016.
 */
public class CustomerController implements Initializable {

    @FXML
    private TableView<Customer> customerTable;
    @FXML
    public TableColumn<Customer, String> nameColumn;
    @FXML
    private TableColumn<Customer, String> telColumn;
    @FXML
    private TableColumn<Customer, String> addressColumn;
    @FXML
    private JFXTextField nameField;
    @FXML
    private MaskField telField;
    @FXML
    private JFXTextArea addressField;
    @FXML
    private JFXTextField searchField;
    @FXML
    private JFXButton btnAdd;
    @FXML
    private JFXButton btnDelete;
    @FXML
    private JFXButton btnClear;
    @FXML
    private BorderPane content;

    private final static int rowsPerPage = 500;

    private ObservableList<Customer> masterData = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredData;
    private SortedList<Customer> sortedData;
    private boolean isEdit = false;
    private Customer focusedCustomer = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        telColumn.setCellValueFactory(param -> param.getValue().telProperty());
        addressColumn.setCellValueFactory(param -> param.getValue().addressProperty());

        nameColumn.setCellFactory(column -> new CustomerToolTipTableCell());
        addressColumn.setCellFactory(column -> new CustomerToolTipTableCell());

        telColumn.getStyleClass().add("invoice-align");
        telColumn.setId("invoice");

        btnDelete.setDisable(true);

        addressField.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if ( keyEvent.getCode().equals(KeyCode.ENTER) ) {
                btnAdd.fire();
                keyEvent.consume(); // necessary to prevent event handlers for this event
            }
        });

        refreshTable();
    }

    @FXML
    public void onAdd(ActionEvent actionEvent) {
        String customerName = nameField.getText();
        String telephoneNumber = telField.getText();
        String address = addressField.getText();

        CustomerDAO dao = new CustomerDAO();
        Customer newCustomer = new Customer(customerName, telephoneNumber, address);

        if ( isEdit ) {
            dao.editCustomer(newCustomer, focusedCustomer.getName());
        } else {
            dao.addCustomer(newCustomer);
        }

        btnClear.fire();
        nameField.requestFocus();

        refreshTable();
    }

    @FXML
    public void onDelete(ActionEvent actionEvent) {
        if ( focusedCustomer != null ) {
            CustomerDAO dao = new CustomerDAO();
            dao.deleteCustomer(focusedCustomer);
            btnClear.fire();
            nameField.requestFocus();
            refreshTable();
        }
    }

    @FXML
    public void onClear(ActionEvent actionEvent) {
        nameField.clear();
        telField.clear();
        addressField.clear();
        btnAdd.setText("Add");
        isEdit = false;
    }

    @FXML
    public void onEnter(KeyEvent event) {
        if ( event.getCode() == KeyCode.ENTER ) {
            if ( event.getSource().equals(nameField) ) {
                telField.requestFocus();
            } else if ( event.getSource().equals(telField) ) {
                addressField.requestFocus();
            }
        }
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, masterData.size());

        customerTable.setItems(FXCollections.observableArrayList(masterData.subList(fromIndex, toIndex)));

        // 1. Wrap the ObservableList in a FilteredList (initially display all data).
        filteredData = new FilteredList<>(customerTable.getItems(), p -> true);

        // 2. Set the filter Predicate whenever the filter changes.
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(customer -> {
                // If filter text is empty, display all persons.
                if ( newValue == null || newValue.isEmpty() ) {
                    return true;
                }
                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();

                if ( customer.getName().toLowerCase().contains(lowerCaseFilter) ) {
                    return true; // Filter matches first name.
                } else if ( customer.getTel().toLowerCase().contains(lowerCaseFilter) ) {
                    return true; // Filter matches last name.
                } else if ( customer.getAddress().toLowerCase().contains(lowerCaseFilter) ) {
                    return true;
                }
                return false; // Does not match.
            });
        });

        // 3. Wrap the FilteredList in a SortedList.
        sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(customerTable.comparatorProperty());

        // 5. Add sorted (and filtered) data to the table.
        customerTable.setItems(sortedData);

        PseudoClass lastRow = PseudoClass.getPseudoClass("last-row");
        // 6. Add mouse click listener to table
        customerTable.setRowFactory(tv -> {
            TableRow<Customer> row =  new TableRow<Customer>() {
                @Override
                public void updateIndex(int index) {
                    super.updateIndex(index);
                    pseudoClassStateChanged(lastRow,
                            index >= 0 && index == customerTable.getItems().size() - 1);
                }
            };
            row.setOnMouseClicked(event -> {
                if ( event.getClickCount() == 1 && (!row.isEmpty()) ) {
                    focusedCustomer = row.getItem();
                    nameField.setText(focusedCustomer.getName());
                    if ( focusedCustomer.getTel() != null ) {
                        telField.setText(focusedCustomer.getTel());
                    }
                    if ( focusedCustomer.getAddress() != null ) {
                        addressField.setText(focusedCustomer.getAddress());
                    }
                    isEdit = true;
                    btnAdd.setText("Update");
                    btnDelete.setDisable(false);
                }
            });
            return row;
        });

        BorderPane bp = new BorderPane(customerTable);
        bp.setStyle("-fx-padding: 20px;");
        return bp;
    }

    private void refreshTable() {
        CustomerDAO dao = new CustomerDAO();
        masterData.clear();
        masterData.addAll(dao.queryCustomers());

        Pagination pagination = new Pagination((masterData.size() / rowsPerPage + 1), 0);
        pagination.setPageFactory(this::createPage);

        content.setCenter(pagination);
    }

}

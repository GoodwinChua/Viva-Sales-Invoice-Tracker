package controller;

import com.jfoenix.controls.*;
import db.CustomerDAO;
import db.InvoiceDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Customer;
import model.FilterBag;
import model.Invoice;
import utility.*;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by Goodwin Chua on 8 Sep 2016.
 */
public class InvoiceController implements Initializable {
    @FXML
    public JFXTextField invoiceFieldForm;
    @FXML
    public JFXDatePicker dateFieldForm;
    @FXML
    public AutoCompleteTextField nameFieldForm;
    @FXML
    public JFXTextField amountFieldForm;
    @FXML
    public JFXTextField poFieldForm;
    @FXML
    public JFXTextArea remarksFieldForm;
    @FXML
    public JFXButton btnOut;
    @FXML
    public JFXButton btnCancel;
    @FXML
    public JFXButton btnAdd;
    @FXML
    public JFXButton btnClear;
    @FXML
    public JFXTextField invoiceFilterFrom;
    @FXML
    public JFXTextField invoiceFilterTo;
    @FXML
    public AutoCompleteTextField nameFilter;
    @FXML
    public JFXTextField amountFilter;
    @FXML
    public JFXDatePicker dateFilterFrom;
    @FXML
    public JFXDatePicker dateFilterTo;
    @FXML
    public JFXRadioButton radioAll;
    @FXML
    public JFXRadioButton radioPaid;
    @FXML
    public JFXRadioButton radioCancelled;
    @FXML
    public JFXRadioButton radioUnpaid;
    @FXML
    public JFXButton btnShow;
    @FXML
    public TableView<Invoice> entryTable;
    @FXML
    public TableColumn<Invoice, LocalDate> dateColumn;
    @FXML
    public TableColumn<Invoice, Number> invoiceColumn;
    @FXML
    public TableColumn<Invoice, String> nameColumn;
    @FXML
    public TableColumn<Invoice, String> poColumn;
    @FXML
    public TableColumn<Invoice, Number> amountColumn;
    @FXML
    public TableColumn<Invoice, String> statusColumn;
    @FXML
    public TableColumn<Invoice, String> remarksColumn;
    @FXML
    public JFXButton btnClearFilter;
    @FXML
    public Label message;
    @FXML
    public JFXButton btnPrint;
    @FXML
    private BorderPane content;

    private final static int rowsPerPage = 1000;
    private ObservableList<Invoice> masterData = FXCollections.observableArrayList();
    private FilteredList<Invoice> filteredData;
    private SortedList<Invoice> sortedData;
    private ToggleGroup group;
    private ArrayList<String> customerNames;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeData();
        initializeColumns();
        initializeForm();
        initializeFilter();
        refreshTable();
    }

    @FXML
    public void onAdd(ActionEvent actionEvent) {
        Invoice invoice = generateInvoice();
        if ( invoice != null ) {
            InvoiceDAO dao = new InvoiceDAO();
            boolean flag = dao.addOrder(invoice);
            String context = "Added: ";
            if ( !flag ) {
                context = "Updated: ";
            }
            message.setText(context + invoice.getInvoice() + ", " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a MMM dd, yyyy")));
            clearForm();
            initializeData();
            refreshTable();
        }
    }

    @FXML
    public void onOut(ActionEvent event) {
        Invoice invoice = generateInvoice();
        if ( invoice != null ) {
            InvoiceDAO dao = new InvoiceDAO();
            dao.outOrder(invoice);
            message.setText("Out: " + invoice.getInvoice() + ", " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a MMM dd, yyyy")));
            clearForm();
            initializeData();
            refreshTable();
        }
    }

    @FXML
    public void onCancel(ActionEvent event) {
        Invoice invoice = generateInvoice();
        if ( invoice != null ) {
            InvoiceDAO dao = new InvoiceDAO();
            dao.cancelOrder(invoice);
            message.setText("Cancelled: " + invoice.getInvoice() + ", " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a MMM dd, yyyy")));
            clearForm();
            initializeData();
            refreshTable();
        }
    }

    @FXML
    public void onClear(ActionEvent actionEvent) {
        clearForm();
        message.setText("");
    }

    @FXML
    public void onEnter(Event e) {
        KeyEvent event = (KeyEvent) e;
        if ( event.getCode() == KeyCode.ENTER ) {
            if ( event.getSource().equals(invoiceFieldForm) ) {
                dateFieldForm.getChildrenUnmodifiable().get(1).requestFocus();
            } else if ( event.getSource().equals(dateFieldForm) ) {
                nameFieldForm.requestFocus();
            } else if ( event.getSource().equals(nameFieldForm) ) {
                amountFieldForm.requestFocus();
            } else if ( event.getSource().equals(amountFieldForm) ) {
                poFieldForm.requestFocus();
            } else if ( event.getSource().equals(poFieldForm) ) {
                remarksFieldForm.requestFocus();
            } else if ( event.getSource().equals(remarksFieldForm) ) {
                btnAdd.requestFocus();
            } else if ( event.getSource().equals(btnAdd) ) {
                btnAdd.fire();
                invoiceFieldForm.requestFocus();
            }
        }
    }

    @FXML
    public void onFilter(ActionEvent event) {
        String invoiceFrom = invoiceFilterFrom.getText();
        String invoiceTo = invoiceFilterTo.getText();
        LocalDate dateFrom = dateFilterFrom.getValue();
        LocalDate dateTo = dateFilterTo.getValue();
        String name = nameFilter.getText();
        String amount = amountFilter.getText();

        RadioButton isSelected = (RadioButton) group.getSelectedToggle();
        String filterMode = isSelected.getText();

        FilterBag filterBag = new FilterBag();
        filterBag.setFilterMode(filterMode);

        if ( invoiceFrom.length() > 0 ) {
            filterBag.setInvoiceFrom(invoiceFrom);
        }
        if ( invoiceTo.length() > 0 ) {
            filterBag.setInvoiceTo(invoiceTo);
        }
        if ( dateFrom != null ) {
            filterBag.setDateFrom(dateFrom);
        }
        if ( dateTo != null ) {
            filterBag.setDateTo(dateTo);
        }
        if ( name.length() > 0 ) {
            filterBag.setCustomer(name);
        }
        if ( amount.length() > 0 ) {
            filterBag.setAmount(amount);
        }

        InvoiceDAO dao = new InvoiceDAO();
        masterData.clear();
        masterData.addAll(dao.queryOrder(filterBag));

        refreshTable();
    }

    @FXML
    public void onClearFilter(ActionEvent actionEvent) {
        invoiceFilterFrom.clear();
        invoiceFilterTo.clear();
        dateFilterFrom.setValue(null);
        dateFilterTo.setValue(null);
        nameFilter.setText("");
        amountFilter.clear();
        radioAll.setSelected(true);
    }

    private void initializeColumns() {
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        invoiceColumn.setCellValueFactory(param -> param.getValue().invoiceProperty());
        nameColumn.setCellValueFactory(param -> param.getValue().customerProperty());
        poColumn.setCellValueFactory(param -> param.getValue().poProperty());
        amountColumn.setCellValueFactory(param -> param.getValue().amountProperty());
        statusColumn.setCellValueFactory(param -> {
            if ( param.getValue().isCancelled() )
                return new SimpleStringProperty("C");
            else if ( param.getValue().isPaid() ) {
                return new SimpleStringProperty("P");
            } else {
                return new SimpleStringProperty("NP");
            }
        });
        remarksColumn.setCellValueFactory(param -> param.getValue().remarksProperty());

        dateColumn.getStyleClass().add("text-align");
        invoiceColumn.getStyleClass().add("invoice-align");
        invoiceColumn.setId("invoice");
        nameColumn.getStyleClass().add("text-align");
        poColumn.getStyleClass().add("text-align");
        amountColumn.getStyleClass().add("number-align");
        amountColumn.setId("money");
        statusColumn.getStyleClass().add("text-align");
        remarksColumn.getStyleClass().add("text-align");

        dateColumn.setCellFactory(column -> new DateTableCell());
        amountColumn.setCellFactory(column -> new AmountTableCell());
        remarksColumn.setCellFactory(column -> new ToolTipTableCell());
        nameColumn.setCellFactory(column -> new ToolTipTableCell());
        poColumn.setCellFactory(column -> new ToolTipTableCell());

        entryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void initializeFilter() {
        group = new ToggleGroup();
        radioAll.setToggleGroup(group);
        radioCancelled.setToggleGroup(group);
        radioPaid.setToggleGroup(group);
        radioUnpaid.setToggleGroup(group);
        radioAll.setSelected(true);

        invoiceFilterFrom.textProperty().addListener(new InvoiceChangeListener(invoiceFilterFrom));
        invoiceFilterTo.textProperty().addListener(new InvoiceChangeListener(invoiceFilterTo));

        nameFilter.setStyle("-fx-font: 11px Arial;");
        nameFilter.getEntries().addAll(customerNames);

        amountFilter.setTextFormatter(new TextFormatter<>(new AmountFormatter()));
    }

    private void generateCustomerNames() {
        customerNames = new ArrayList<>();
        ArrayList<Customer> customers = new CustomerDAO().queryCustomers();
        customerNames.addAll(customers.stream().map(Customer::getName).collect(Collectors.toList()));
    }

    private void initializeForm() {
        invoiceFieldForm.textProperty().addListener(new InvoiceChangeListener(invoiceFieldForm));
        amountFieldForm.setTextFormatter(new TextFormatter<>(new AmountFormatter()));
        nameFieldForm.setStyle("-fx-font: 11px Arial;");
        nameFieldForm.getEntries().addAll(customerNames);

        btnOut.setDisable(true);
        btnCancel.setDisable(true);
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, masterData.size());

        entryTable.setItems(FXCollections.observableArrayList(masterData.subList(fromIndex, toIndex)));

        // 1. Wrap the ObservableList in a FilteredList (initially display all data).
        filteredData = new FilteredList<>(entryTable.getItems(), p -> true);

        // 2. Wrap the FilteredList in a SortedList.
        sortedData = new SortedList<>(filteredData);

        // 3. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(entryTable.comparatorProperty());

        // 4. Add sorted (and filtered) data to the table.
        entryTable.setItems(sortedData);

        // 5. Add mouse click listener to table
        entryTable.setRowFactory(tv -> {
            TableRow<Invoice> row = new InvoiceTableRow();
            row.setOnMouseClicked(event -> {
                if ( event.getClickCount() == 1 && (!row.isEmpty()) ) {
                    Invoice selected = row.getItem();
                    evaluateInvoice(selected);
                    invoiceFieldForm.setText(selected.getInvoice() + "");
                    dateFieldForm.setValue(selected.getDate());
                    NumberFormat amountFormat = new DecimalFormat("#.00");
                    amountFieldForm.setText(amountFormat.format(selected.getAmount()));
                    nameFieldForm.setText(selected.getCustomer());
                    poFieldForm.setText(selected.getPo());
                    remarksFieldForm.setText(selected.getRemarks());

                    btnAdd.setText("Update");
                    btnOut.setDisable(false);
                    btnCancel.setDisable(false);
                }
            });
            return row;
        });

        BorderPane bp = new BorderPane(entryTable);
        bp.setStyle("-fx-padding: 20px;");
        return bp;
    }

    private void refreshTable() {
        Pagination pagination = new Pagination((masterData.size() / rowsPerPage + 1), 0);
        pagination.setPageFactory(this::createPage);

        content.setCenter(pagination);
    }

    private void initializeData() {
        InvoiceDAO dao = new InvoiceDAO();
        masterData.clear();
        masterData.addAll(dao.queryOrder(null));
        generateCustomerNames();
    }

    private Invoice generateInvoice() {
        Invoice invoice = new Invoice();

        String invoiceNumber = invoiceFieldForm.getText();
        LocalDate date = dateFieldForm.getValue();
        String customer = nameFieldForm.getText();
        String amount = amountFieldForm.getText();
        String po = poFieldForm.getText();
        String remarks = remarksFieldForm.getText();

        CustomerDAO dao = new CustomerDAO();
        Customer existingCustomer = dao.searchCustomer(customer);

        boolean isOk = false;
        if ( invoiceNumber.length() < 5 ) {
            message.setText("Invalid invoice number");
        } else if ( date == null ) {
            message.setText("Invalid date");
        } else if ( customer.length() == 0 ) {
            message.setText("Invalid customer");
        } else if ( existingCustomer == null ) {
            message.setText("Invalid customer");
        } else if ( amount.length() == 0 ) {
            message.setText("Invalid amount");
        } else {
            isOk = true;
        }

        if ( isOk ) {
            invoice.setInvoice(Integer.parseInt(invoiceNumber));
            invoice.setDate(date);
            invoice.setCustomer(customer);
            invoice.setAmount(Double.parseDouble(amount));
            invoice.setPo(po);
            invoice.setRemarks(remarks);
            return invoice;
        }

        return null;
    }

    private void clearForm() {
        invoiceFieldForm.clear();
        dateFieldForm.setValue(null);
        amountFieldForm.clear();
        poFieldForm.clear();
        remarksFieldForm.clear();
        nameFieldForm.clear();
        btnOut.setDisable(true);
        btnCancel.setDisable(true);
        btnAdd.setText("Add");
    }

    public void onPrint(ActionEvent actionEvent) {
        previewPrintable();
    }

    private void previewPrintable() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(Locator.LOCATION + "printing.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.getIcons().add(new Image("resources/img/vse.png"));
            stage.setTitle("Print Preview");
            root.getStylesheets().add("resources/invoice.css");

            Printer printer = Printer.getDefaultPrinter();
            PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);

            double width = pageLayout.getPrintableWidth();
            double height = pageLayout.getPrintableHeight();

            stage.setScene(new Scene(root, height, width));
            PrintingController printingController = fxmlLoader.getController();
            printingController.setMasterData(masterData);

            stage.show();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onSearch(Event event) {
        String invoiceSearched = invoiceFieldForm.getText();

        if ( invoiceSearched.length() > 0 ) {
            Invoice selected = new InvoiceDAO().searchOrder(invoiceSearched);
            if ( selected != null ) {
                // color depending on status
                evaluateInvoice(selected);

                dateFieldForm.setValue(selected.getDate());
                NumberFormat amountFormat = new DecimalFormat("#.00");
                amountFieldForm.setText(amountFormat.format(selected.getAmount()));
                nameFieldForm.setText(selected.getCustomer());
                poFieldForm.setText(selected.getPo());
                remarksFieldForm.setText(selected.getRemarks());

                btnAdd.setText("Update");
                btnOut.setDisable(false);
                btnCancel.setDisable(false);
            } else {
                dateFieldForm.setValue(null);
                amountFieldForm.setText("");
                nameFieldForm.setText("");
                poFieldForm.setText("");
                remarksFieldForm.setText("");
                colorFieldForm("black");
            }
        }
    }

    private void evaluateInvoice(Invoice selected) {
        if ( selected.isPaidProperty() != null ) {
            if ( selected.isPaid() ) {
                colorFieldForm("#cc3300;");
            } else if ( !selected.isPaid() ) {
                colorFieldForm("black");
            }
        }

        if ( selected.isCancelled() ) {
            colorFieldForm("#3300CC;");
        }
    }

    private void colorFieldForm(String hexColor) {
        invoiceFieldForm.setStyle("-fx-text-fill: " + hexColor);
        amountFieldForm.setStyle("-fx-text-fill: " + hexColor);
        remarksFieldForm.setStyle("-fx-text-fill: " + hexColor);
        poFieldForm.setStyle("-fx-text-fill: " + hexColor);
    }
}

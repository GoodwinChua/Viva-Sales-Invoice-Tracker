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
import javafx.fxml.Initializable;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Customer;
import model.FilterBag;
import model.Invoice;
import utility.*;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by Goodwin Chua on 8 Sep 2016.
 */
public class InvoiceController implements Initializable {
    @FXML
    public JFXTextField invoiceFieldForm;
    @FXML
    public JFXDatePicker dateFieldForm;
    @FXML
    public JFXComboBox<Customer> nameFieldForm;
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
    public JFXComboBox<Customer> nameFilter;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeColumns();
        initializeForm();
        initializeFilter();
        initializeData();
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
            }
        }
    }

    @FXML
    public void onFilter(ActionEvent event) {
        String invoiceFrom = invoiceFilterFrom.getText();
        String invoiceTo = invoiceFilterTo.getText();
        LocalDate dateFrom = dateFilterFrom.getValue();
        LocalDate dateTo = dateFilterTo.getValue();
        String name = nameFilter.getValue().getName();
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
        nameFilter.getSelectionModel().selectLast();
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

        remarksColumn.prefWidthProperty().bind(
                entryTable.widthProperty()
                        .subtract(dateColumn.widthProperty())
                        .subtract(invoiceColumn.widthProperty())
                        .subtract(nameColumn.widthProperty())
                        .subtract(poColumn.widthProperty())
                        .subtract(amountColumn.widthProperty())
                        .subtract(statusColumn.widthProperty())
                        .subtract(2)  // a border stroke?
        );

        dateColumn.getStyleClass().add("text-align");
        invoiceColumn.getStyleClass().add("number-align");
        invoiceColumn.setId("money");
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

        nameFilter.setConverter(new CustomerStringConverter());
        ArrayList<Customer> customers = new CustomerDAO().queryCustomers();
        customers.add(new Customer("None", "", ""));
        nameFilter.setItems(FXCollections.observableArrayList(customers));
        nameFilter.setStyle("-fx-font: 12px Calibri; -fx-padding: 5 11 5 10");
        nameFilter.getSelectionModel().selectLast();

        amountFilter.setTextFormatter(new TextFormatter<>(new AmountFormatter()));
    }

    private void initializeForm() {
        invoiceFieldForm.textProperty().addListener(new InvoiceChangeListener(invoiceFieldForm));
        amountFieldForm.setTextFormatter(new TextFormatter<>(new AmountFormatter()));

        nameFieldForm.setConverter(new CustomerStringConverter());
        nameFieldForm.setItems(FXCollections.observableArrayList(new CustomerDAO().queryCustomers()));
        nameFieldForm.setStyle("-fx-font: 12px Calibri; -fx-padding: 5 11 5 10");

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
                    invoiceFieldForm.setText(selected.getInvoice() + "");
                    dateFieldForm.setValue(selected.getDate());
                    NumberFormat amountFormat = new DecimalFormat("#.00");
                    amountFieldForm.setText(amountFormat.format(selected.getAmount()));
                    Customer customer = new CustomerDAO().searchCustomer(selected.getCustomer());
                    nameFieldForm.getSelectionModel().select(customer);
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
    }

    private Invoice generateInvoice() {
        Invoice invoice = new Invoice();

        String invoiceNumber = invoiceFieldForm.getText();
        LocalDate date = dateFieldForm.getValue();
        Customer customer = nameFieldForm.getValue();
        String amount = amountFieldForm.getText();
        String po = poFieldForm.getText();
        String remarks = remarksFieldForm.getText();

        boolean isOk = false;
        if ( invoiceNumber.length() < 5 ) {
            message.setText("Invalid invoice number");
        } else if ( date == null ) {
            message.setText("Invalid date");
        } else if ( customer == null ) {
            message.setText("Invalid customer");
        } else if ( amount.length() == 0 ) {
            message.setText("Invalid amount");
        } else {
            isOk = true;
        }

        if ( isOk ) {
            invoice.setInvoice(Integer.parseInt(invoiceNumber));
            invoice.setDate(date);
            invoice.setCustomer(customer.getName());
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
        btnOut.setDisable(true);
        btnCancel.setDisable(true);
        btnAdd.setText("Add");
    }

    public void onPrint(ActionEvent actionEvent) {
        preparePrintable();
    }

    private void preparePrintable() {
        TableView<Invoice> printTable = generateTable();

        PrinterJob job = PrinterJob.createPrinterJob();

        Printer printer = Printer.getDefaultPrinter();
        PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.LANDSCAPE, Printer.MarginType.DEFAULT);

        if ( job != null ) {
            Stage stage = new Stage();
            if ( job.showPrintDialog(stage) ) {
                String jobName = "Viva Sales " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a MMM dd, yyyy"));
                job.getJobSettings().setJobName(jobName);

                // 1. Create Header
                BorderPane borderpane = generateHeader();

                int from = 0;
                int to = Math.min(18, masterData.size());
                printTable.setItems(FXCollections.observableList(masterData.subList(from, to)));
                borderpane.setCenter(printTable);

                // 3. Print Border Pane
                boolean success = job.printPage(pageLayout, borderpane);

                // 4. Print Successive Pages ( if there are any )
                if ( masterData.size() > 18 ) {
                    borderpane.setTop(null);

                    int printableRowsPerPage = 19; //18 first page, 27 the rest
                    int pageCount = (masterData.size() - 18) / printableRowsPerPage + 1;

                    // 5. print successive pages
                    int currentPage = 0;
                    while ( currentPage < pageCount && success ) {
                        int fromIndex = 18 + (currentPage * printableRowsPerPage);
                        int toIndex = Math.min(fromIndex + printableRowsPerPage, masterData.size());
                        printTable.setItems(FXCollections.observableList(masterData.subList(fromIndex, toIndex)));
                        success = job.printPage(pageLayout, printTable);
                        currentPage++;
                    }
                }

                // 5. End Job
                if ( success ) {
                    job.endJob();
                }
            }
        }

    }

    private String computeTotal() {
        BigDecimal bigDecimal = new BigDecimal(0.0);
        for ( Invoice item : masterData ) {
            bigDecimal = bigDecimal.add(new BigDecimal(item.getAmount()));
        }
        NumberFormat df = DecimalFormat.getInstance();
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        String total = df.format(bigDecimal.doubleValue());
        return total;
    }

    private BorderPane generateHeader() {
        VBox vbox = new VBox();
        Label total = new Label("Total: " + computeTotal());
        total.setStyle("-fx-font-weight: bold");
        Label headerBlank4 = new Label("");
        vbox.getChildren().addAll( total, headerBlank4);

        BorderPane borderpane = new BorderPane();
        // 2. Setup Border Pane
        borderpane.setTop(vbox);
        return borderpane;
    }

    private TableView generateTable() {
        Printer printer = Printer.getDefaultPrinter(); //get the default printer
        PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.LANDSCAPE, Printer.MarginType.DEFAULT);

        TableView<Invoice> printTable = new TableView<>();
        TableColumn<Invoice, LocalDate> printDate = new TableColumn<>("Date");
        TableColumn<Invoice, Number> printInvoice = new TableColumn<>("Invoice No.");
        TableColumn<Invoice, String> printCustomer = new TableColumn<>("Customer");
        TableColumn<Invoice, String> printPO = new TableColumn<>("PO No.");
        TableColumn<Invoice, Number> printAmount = new TableColumn<>("Amount");

        printDate.setCellValueFactory(param -> param.getValue().dateProperty());
        printInvoice.setCellValueFactory(param -> param.getValue().invoiceProperty());
        printCustomer.setCellValueFactory(param -> param.getValue().customerProperty());
        printPO.setCellValueFactory(param -> param.getValue().poProperty());
        printAmount.setCellValueFactory(param -> param.getValue().amountProperty());

        printDate.getStyleClass().add("text-align");
        printInvoice.getStyleClass().add("number-align");
        printInvoice.setId("money");
        printPO.getStyleClass().add("text-align");
        printCustomer.getStyleClass().add("text-align");
        printAmount.setId("money");
        printAmount.getStyleClass().add("number-align");
        printTable.setId("printing");
        printTable.getStylesheets().add("resources/printing.css");

        printDate.setCellFactory(column -> new DateTableCell());
        printAmount.setCellFactory(BalanceAmountTableCell::new);

        printTable.getColumns().add(printDate);
        printTable.getColumns().add(printInvoice);
        printTable.getColumns().add(printCustomer);
        printTable.getColumns().add(printPO);
        printTable.getColumns().add(printAmount);

        printTable.setPrefWidth(pageLayout.getPrintableWidth());
        printTable.setPrefHeight(pageLayout.getPrintableHeight());

        printDate.setPrefWidth(80);
        printInvoice.setPrefWidth(80);
        printCustomer.setPrefWidth(300);
        printPO.setPrefWidth(125);
        printAmount.prefWidthProperty().bind(
                printTable.widthProperty()
                        .subtract(printDate.widthProperty())
                        .subtract(printInvoice.widthProperty())
                        .subtract(printCustomer.widthProperty())
                        .subtract(printPO.widthProperty())
                        .subtract(2)  // a border stroke?
        );

        return printTable;
    }
}

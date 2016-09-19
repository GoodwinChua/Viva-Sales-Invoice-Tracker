package controller;

import com.jfoenix.controls.*;
import db.CustomerDAO;
import db.InvoiceDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.*;
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
public class BalanceController implements Initializable {
    @FXML
    public JFXTextField invoiceFilterFrom;
    @FXML
    public JFXTextField invoiceFilterTo;
    @FXML
    public JFXComboBox<Customer> nameFilter;
    @FXML
    public JFXDatePicker dateFilterFrom;
    @FXML
    public JFXDatePicker dateFilterTo;
    @FXML
    public JFXRadioButton radioAll;
    @FXML
    public JFXRadioButton radioPaid;
    @FXML
    public JFXRadioButton radioUnpaid;
    @FXML
    public TableView<Invoice> balanceTable;
    @FXML
    public TableColumn<Invoice, LocalDate> dateColumn;
    @FXML
    public TableColumn<Invoice, Number> invoiceColumn;
    @FXML
    public TableColumn<Invoice, String> poColumn;
    @FXML
    public TableColumn<Invoice, Number> amountColumn;
    @FXML
    public JFXButton btnTotal;
    @FXML
    public JFXButton btnClear;
    @FXML
    public JFXButton btnPrint;
    @FXML
    public Label totalLabel;

    @FXML
    private BorderPane content;

    private final static int rowsPerPage = 1000;
    private ObservableList<Invoice> masterData = FXCollections.observableArrayList();
    private ToggleGroup group;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeColumns();
        initializeFilter();
    }

    @FXML
    public void onClear(ActionEvent actionEvent) {
        invoiceFilterFrom.clear();
        invoiceFilterTo.clear();
        dateFilterFrom.setValue(null);
        dateFilterTo.setValue(null);
        nameFilter.getSelectionModel().selectLast();
        radioAll.setSelected(true);
    }

    @FXML
    public void onTotal(ActionEvent event) {
        String invoiceFrom = invoiceFilterFrom.getText();
        String invoiceTo = invoiceFilterTo.getText();
        LocalDate dateFrom = dateFilterFrom.getValue();
        LocalDate dateTo = dateFilterTo.getValue();
        String name = nameFilter.getValue().getName();

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

        InvoiceDAO dao = new InvoiceDAO();
        masterData.clear();
        masterData.addAll(dao.queryOrder(filterBag));

        refreshTable();
        computeTotal(filterMode);
    }

    private void initializeColumns() {
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        invoiceColumn.setCellValueFactory(param -> param.getValue().invoiceProperty());
        poColumn.setCellValueFactory(param -> param.getValue().poProperty());
        amountColumn.setCellValueFactory(param -> param.getValue().amountProperty());

        amountColumn.prefWidthProperty().bind(
                balanceTable.widthProperty()
                        .subtract(dateColumn.widthProperty())
                        .subtract(invoiceColumn.widthProperty())
                        .subtract(poColumn.widthProperty())
                        .subtract(2)  // a border stroke?
        );

        dateColumn.getStyleClass().add("text-align");
        invoiceColumn.getStyleClass().add("number-align");
        invoiceColumn.setId("money");
        poColumn.getStyleClass().add("text-align");
        amountColumn.getStyleClass().add("number-align");
        amountColumn.setId("money");

        dateColumn.setCellFactory(column -> new DateTableCell());
        poColumn.setCellFactory(column -> new ToolTipTableCell());

        amountColumn.setCellFactory(BalanceAmountTableCell::new);

    }

    private void initializeFilter() {
        group = new ToggleGroup();
        radioAll.setToggleGroup(group);
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
    }

    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, masterData.size());

        balanceTable.setItems(FXCollections.observableArrayList(masterData.subList(fromIndex, toIndex)));

        // 1. Wrap the ObservableList in a FilteredList (initially display all data).
        FilteredList<Invoice> filteredData = new FilteredList<>(balanceTable.getItems(), p -> true);

        // 2. Wrap the FilteredList in a SortedList.
        SortedList<Invoice> sortedData = new SortedList<>(filteredData);

        // 3. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(balanceTable.comparatorProperty());

        // 4. Add sorted (and filtered) data to the table.
        balanceTable.setItems(sortedData);

        BorderPane bp = new BorderPane(balanceTable);
        bp.setStyle("-fx-padding: 20px;");
        return bp;
    }

    private void refreshTable() {
        Pagination pagination = new Pagination((masterData.size() / rowsPerPage + 1), 0);
        pagination.setPageFactory(this::createPage);

        content.setCenter(pagination);
    }

    private void computeTotal(String filterMode) {
        BigDecimal bigDecimal = new BigDecimal(0.0);
        for ( Invoice item : masterData ) {
            switch ( filterMode ) {
                case "All":
                    if ( !item.isPaid() && !item.isCancelled() ) {
                        bigDecimal = bigDecimal.add(new BigDecimal(item.getAmount()));
                    }
                    break;
                case "Paid": //follow
                case "Not Paid":
                    bigDecimal = bigDecimal.add(new BigDecimal(item.getAmount()));
                    break;
            }
        }
        NumberFormat df = DecimalFormat.getInstance();
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        String total = df.format(bigDecimal.doubleValue());
        totalLabel.setText(total);
    }

    private void preparePrintable() {
        TableView<Invoice> printTable = generateTable();
        PrinterJob job = PrinterJob.createPrinterJob();

        Printer printer = Printer.getDefaultPrinter();
        PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);

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

                    int printableRowsPerPage = 27; //18 first page, 27 the rest
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

    private BorderPane generateHeader() {
        Customer customer = nameFilter.getValue();

        BorderPane borderpane = new BorderPane();
        VBox vbox = new VBox();
        Label headerSpacing0 = new Label("");
        Label headerSpacing1 = new Label("");
        Label headerSpacing2 = new Label("");
        Label headerSpacing3 = new Label("");
        Label headerSpacing4 = new Label("");
        Label headerSpacing5 = new Label("");
        Label headerDate = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        Label headerBlank = new Label("");
        Label headerCustomerName = new Label(customer.getName());
        Label headerTelephone = new Label("Telephone No.: " + customer.getTel());
        Label headerAddress = new Label("Address: " + customer.getAddress());
        Label headerBlank2 = new Label("");
        Label title = new Label("Statement of Account");
        Label headerBlank3 = new Label("");
        Label total = new Label("Total: " + totalLabel.getText());
        total.setStyle("-fx-font-weight: bold");
        Label headerBlank4 = new Label("");
        vbox.getChildren().addAll(headerSpacing0, headerSpacing1, headerSpacing2, headerSpacing3, headerSpacing4, headerSpacing5);
        vbox.getChildren().addAll(headerDate, headerBlank, headerCustomerName, headerTelephone, headerAddress, headerBlank2);
        vbox.getChildren().addAll(title, headerBlank3, total, headerBlank4);
        vbox.getStylesheets().add("resources/balance.css");

        // 2. Setup Border Pane
        borderpane.setTop(vbox);
        return borderpane;
    }

    private TableView generateTable() {
        Printer printer = Printer.getDefaultPrinter(); //get the default printer
        PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);

        TableView<Invoice> printTable = new TableView<>();
        TableColumn<Invoice, LocalDate> printDate = new TableColumn<>("Date");
        TableColumn<Invoice, Number> printInvoice = new TableColumn<>("Invoice No.");
        TableColumn<Invoice, String> printPO = new TableColumn<>("PO No.");
        TableColumn<Invoice, Number> printAmount = new TableColumn<>("Amount");

        printDate.setCellValueFactory(param -> param.getValue().dateProperty());
        printInvoice.setCellValueFactory(param -> param.getValue().invoiceProperty());
        printPO.setCellValueFactory(param -> param.getValue().poProperty());
        printAmount.setCellValueFactory(param -> param.getValue().amountProperty());

        printDate.getStyleClass().add("text-align");
        printInvoice.getStyleClass().add("number-align");
        printInvoice.setId("money");
        printPO.getStyleClass().add("text-align");
        printAmount.setId("money");
        printAmount.getStyleClass().add("number-align");
        printTable.setId("printing");
        printTable.getStylesheets().add("resources/printing.css");

        printDate.setCellFactory(column -> new DateTableCell());
        printAmount.setCellFactory(BalanceAmountTableCell::new);

        printTable.getColumns().add(printDate);
        printTable.getColumns().add(printInvoice);
        printTable.getColumns().add(printPO);
        printTable.getColumns().add(printAmount);

        printTable.setPrefWidth(pageLayout.getPrintableWidth());
        printTable.setPrefHeight(pageLayout.getPrintableHeight());

        printDate.setPrefWidth(80);
        printInvoice.setPrefWidth(110);
        printPO.setPrefWidth(160);
        printAmount.prefWidthProperty().bind(
                printTable.widthProperty()
                        .subtract(printDate.widthProperty())
                        .subtract(printInvoice.widthProperty())
                        .subtract(printPO.widthProperty())
                        .subtract(2)  // a border stroke?
        );

        return printTable;
    }

    public void onPrint(ActionEvent actionEvent) {
        preparePrintable();
    }
}

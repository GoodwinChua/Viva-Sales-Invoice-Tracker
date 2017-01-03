package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import db.CustomerDAO;
import db.InvoiceDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Customer;
import model.FilterBag;
import model.Invoice;
import utility.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Created by Goodwin Chua on 8 Sep 2016.
 */
public class BalanceController implements Initializable {
    @FXML
    public JFXTextField invoiceFilterFrom;
    @FXML
    public JFXTextField invoiceFilterTo;
    @FXML
    public AutoCompleteTextField nameFilter;
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
    private double balance = 0.0;

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
        nameFilter.clear();
        radioAll.setSelected(true);
    }

    @FXML
    public void onTotal(ActionEvent event) {
        String invoiceFrom = invoiceFilterFrom.getText();
        String invoiceTo = invoiceFilterTo.getText();
        LocalDate dateFrom = dateFilterFrom.getValue();
        LocalDate dateTo = dateFilterTo.getValue();
        String name = nameFilter.getText();

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
        balance = computeTotal(filterMode);

        NumberFormat df = DecimalFormat.getInstance();
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        String formatted = df.format(balance);
        totalLabel.setText(formatted);
    }

    private void initializeColumns() {
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        invoiceColumn.setCellValueFactory(param -> param.getValue().invoiceProperty());
        poColumn.setCellValueFactory(param -> param.getValue().poProperty());
        amountColumn.setCellValueFactory(param -> param.getValue().amountProperty());

        dateColumn.getStyleClass().add("text-align");
        invoiceColumn.getStyleClass().add("invoice-align");
        invoiceColumn.setId("invoice");
        poColumn.getStyleClass().add("text-align");
        amountColumn.getStyleClass().add("number-align");
        amountColumn.setId("money");

        dateColumn.setCellFactory(column -> new DateTableCell());
        poColumn.setCellFactory(column -> new ToolTipTableCell());
        amountColumn.setCellFactory(column -> new AmountTableCell());

        balanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        balanceTable.setRowFactory(tv -> new InvoiceTableRow());
    }

    private void initializeFilter() {
        group = new ToggleGroup();
        radioAll.setToggleGroup(group);
        radioPaid.setToggleGroup(group);
        radioUnpaid.setToggleGroup(group);
        radioAll.setSelected(true);

        invoiceFilterFrom.textProperty().addListener(new InvoiceChangeListener(invoiceFilterFrom));
        invoiceFilterTo.textProperty().addListener(new InvoiceChangeListener(invoiceFilterTo));

        ArrayList<Customer> customers = new CustomerDAO().queryCustomers();
        ArrayList<String> customerNames = new ArrayList<>();
        customerNames.addAll(customers.stream().map(Customer::getName).collect(Collectors.toList()));

        nameFilter.setStyle("-fx-font: 11px Arial;");
        nameFilter.getEntries().addAll(customerNames);
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

    private Double computeTotal(String filterMode) {
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
        return bigDecimal.doubleValue();
    }

    public void onPrint(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(Locator.LOCATION + "printingstatement.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.getIcons().add(new Image("resources/img/vse.png"));
            stage.setTitle("Print Preview");
            root.getStylesheets().add("resources/invoice.css");

            Printer printer = Printer.getDefaultPrinter();
            PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);

            double width = pageLayout.getPrintableWidth();
            double height = pageLayout.getPrintableHeight();

            stage.setScene(new Scene(root, width, height));
            PrintingStatementController printingController = fxmlLoader.getController();
            printingController.setMasterData(masterData, balance, nameFilter.getText());

            stage.show();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void onPrintAllUnpaid(ActionEvent actionEvent) {
        ArrayList<Customer> unpaidCustomers = new CustomerDAO().queryCustomers();
        for( Customer customer : unpaidCustomers ) {
            FilterBag filterBag = new FilterBag();
            filterBag.setFilterMode("Not Paid");
            filterBag.setCustomer(customer.getName());
            ArrayList<Invoice> unpaidOrders = new InvoiceDAO().queryOrder(filterBag);
            if ( !unpaidOrders.isEmpty() ) {
                ObservableList<Invoice> unpaidObservable = FXCollections.observableArrayList();
                unpaidObservable.addAll(unpaidOrders);

                BigDecimal bigDecimal = new BigDecimal(0.0);
                for ( Invoice item : unpaidObservable ) {
                    bigDecimal = bigDecimal.add(new BigDecimal(item.getAmount()));
                }

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(Locator.LOCATION + "printingstatement.fxml"));
                Parent root = null;
                try {
                    root = fxmlLoader.load();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                Stage stage = new Stage();
                stage.getIcons().add(new Image("resources/img/vse.png"));
                stage.setTitle("Print Preview");
                root.getStylesheets().add("resources/invoice.css");
                Printer printer = Printer.getDefaultPrinter();
                PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);

                double width = pageLayout.getPrintableWidth();
                double height = pageLayout.getPrintableHeight();

                stage.setScene(new Scene(root, width, height));
                PrintingStatementController printingController = fxmlLoader.getController();
                printingController.setMasterData(unpaidObservable, bigDecimal.doubleValue(), customer.getName());
                printingController.onPrint(null);
            }
        }

    }
}

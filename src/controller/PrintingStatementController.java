package controller;

import com.jfoenix.controls.JFXButton;
import db.CustomerDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Customer;
import model.Invoice;
import utility.AmountTableCell;
import utility.DateTableCell;
import utility.InvoiceTableRow;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Created by Goodwin Chua on 23 Sep 2016.
 */
public class PrintingStatementController implements Initializable {

    @FXML
    public TableView<Invoice> printTable;
    @FXML
    public TableColumn<Invoice, LocalDate> printDate;
    @FXML
    public TableColumn<Invoice, Number> printInvoice;
    @FXML
    public TableColumn<Invoice, String> printPO;
    @FXML
    public TableColumn<Invoice, Number> printAmount;
    @FXML
    public JFXButton btnPrint;
    @FXML
    public BorderPane content;

    private ObservableList<Invoice> masterData;
    private double balance;
    private String name;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        printDate.setCellValueFactory(param -> param.getValue().dateProperty());
        printInvoice.setCellValueFactory(param -> param.getValue().invoiceProperty());
        printPO.setCellValueFactory(param -> param.getValue().poProperty());
        printAmount.setCellValueFactory(param -> param.getValue().amountProperty());

        printDate.getStyleClass().add("text-align");
        printInvoice.getStyleClass().add("invoice-align");
        printInvoice.setId("invoice");
        printPO.getStyleClass().add("text-align");
        printAmount.setId("money");
        printAmount.getStyleClass().add("number-align");
        printTable.getStylesheets().add("resources/invoice.css");

        printDate.setCellFactory(column -> new DateTableCell());
        printAmount.setCellFactory(column -> new AmountTableCell());

        printTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        printTable.setRowFactory(rf -> new InvoiceTableRow());
    }

    public void onPrint(ActionEvent actionEvent) {
        print();

        Stage stage = (Stage) printTable.getScene().getWindow();
        stage.close();
    }

    private void print() {
        PrinterJob job = PrinterJob.createPrinterJob();
        Printer printer = Printer.getDefaultPrinter();
        PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);

        if ( job != null ) {
            Stage stage = new Stage();
            if ( job.showPrintDialog(stage) ) {
                String jobName = "Viva Sales " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a MMM dd, yyyy"));
                job.getJobSettings().setJobName(jobName);

                Invoice total = new Invoice();
                total.toNullInvoice();
                total.setAmount(balance);
                total.setCustomer("Total");
                total.setPo("Total");
                total.setPaid(false);
                masterData.add(total);

                int from = 0;
                int to = Math.min(24, masterData.size());
                printTable.setItems(FXCollections.observableList(masterData.subList(from, to)));

                content.setLeft(null);

                // 3. Print Border Pane
                boolean success = job.printPage(pageLayout, content);

                // 4. Print Successive Pages ( if there are any )
                if ( masterData.size() > 24 ) {
                    content.setTop(null);

                    int printableRowsPerPage = 38; //24 first page, 38 the rest
                    int pageCount = (masterData.size() - 24) / printableRowsPerPage + 1;

                    // 5. print successive pages
                    int currentPage = 0;
                    while ( currentPage < pageCount && success ) {
                        int fromIndex = 24 + (currentPage * printableRowsPerPage);
                        int toIndex = Math.min(fromIndex + printableRowsPerPage, masterData.size());
                        printTable.setItems(FXCollections.observableList(masterData.subList(fromIndex, toIndex)));
                        success = job.printPage(pageLayout, printTable);
                        currentPage++;
                    }
                }

                job.endJob();
                masterData.remove(total);

            }
        }

    }


    public void setMasterData(ObservableList<Invoice> masterData, double total, String customer) {
        this.masterData = masterData;
        this.balance = total;
        this.name = customer;

        printTable.setItems(masterData);
        generateHeader();
    }

    private void generateHeader() {
        Customer customer = new CustomerDAO().searchCustomer(name);
        VBox customerInformation = new VBox();
        Label headerSpacing0 = new Label("");
        Label headerSpacing1 = new Label("");
        Label headerSpacing2 = new Label("");
        Label headerSpacing3 = new Label("");
        Label headerSpacing4 = new Label("");
        Label headerSpacing5 = new Label("");
        Label headerDate = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        Label headerBlank = new Label("");
        if ( customer != null ) {
            Label headerCustomerName = new Label(customer.getName());
            Label headerTelephone = new Label("Telephone No.: " + customer.getTel());
            Label headerAddress = new Label("Address: " + customer.getAddress());
            Label headerBlank2 = new Label("");
            customerInformation.getChildren().addAll(headerCustomerName, headerTelephone, headerAddress, headerBlank2);
        }
        customerInformation.getChildren().addAll(headerSpacing0, headerSpacing1, headerSpacing2, headerSpacing3, headerSpacing4, headerSpacing5);
        customerInformation.getChildren().addAll(headerDate, headerBlank);


        VBox statementOfAccount = new VBox();
        Label title = new Label("Statement of Account");
        Label headerBlank3 = new Label("");
        title.setStyle("-fx-font-weight: bold");
        statementOfAccount.getChildren().addAll(title, headerBlank3);
        statementOfAccount.setAlignment(Pos.CENTER);

        VBox mother = new VBox();
        mother.getChildren().addAll(customerInformation, statementOfAccount);

        // 2. Setup Border Pane
        content.setTop(mother);
    }
}

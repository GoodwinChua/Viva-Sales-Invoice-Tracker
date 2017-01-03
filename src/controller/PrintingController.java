package controller;

import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Invoice;
import utility.AmountTableCell;
import utility.DateTableCell;
import utility.InvoiceTableRow;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Created by Goodwin Chua on 23 Sep 2016.
 */
public class PrintingController implements Initializable {

    @FXML
    public TableView<Invoice> printTable;
    @FXML
    public TableColumn<Invoice, LocalDate> printDate;
    @FXML
    public TableColumn<Invoice, Number> printInvoice;
    @FXML
    public TableColumn<Invoice, String> printCustomer;
    @FXML
    public TableColumn<Invoice, String> printPO;
    @FXML
    public TableColumn<Invoice, Number> printAmount;
    @FXML
    public JFXButton btnPrint;
    @FXML
    public BorderPane content;

    private ObservableList<Invoice> masterData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        printDate.setCellValueFactory(param -> param.getValue().dateProperty());
        printInvoice.setCellValueFactory(param -> param.getValue().invoiceProperty());
        printCustomer.setCellValueFactory(param -> param.getValue().customerProperty());
        printPO.setCellValueFactory(param -> param.getValue().poProperty());
        printAmount.setCellValueFactory(param -> param.getValue().amountProperty());

        printDate.getStyleClass().add("text-align");
        printInvoice.getStyleClass().add("invoice-align");
        printInvoice.setId("invoice");
        printPO.getStyleClass().add("text-align");
        printCustomer.getStyleClass().add("text-align");
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
        PageLayout pageLayout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.LANDSCAPE, Printer.MarginType.DEFAULT);

        if ( job != null ) {
            Stage stage = new Stage();
            if ( job.showPrintDialog(stage) ) {
                String jobName = "Viva Sales " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a MMM dd, yyyy"));
                job.getJobSettings().setJobName(jobName);

                Invoice total = new Invoice();
                total.toNullInvoice();
                total.setAmount(computeTotal());
                total.setPo("Total");
                total.setPaid(false);
                masterData.add(total);

                // 4. Print Successive Pages ( if there are any )
                int printableRowsPerPage = 27;
                int pageCount = masterData.size() / printableRowsPerPage + 1;
                printTable.setItems(FXCollections.observableList(masterData.subList(0, Math.min(27, masterData.size()))));

                content.setLeft(null);

                boolean success = true;

                // 5. print successive pages
                int currentPage = 0;
                while ( currentPage < pageCount && success ) {
                    int fromIndex = currentPage * printableRowsPerPage;
                    int toIndex = Math.min(fromIndex + printableRowsPerPage, masterData.size());
                    printTable.setItems(FXCollections.observableList(masterData.subList(fromIndex, toIndex)));
                    success = job.printPage(pageLayout, content);
                    currentPage++;
                }

                job.endJob();
                masterData.remove(masterData.size() - 1);
            }
        }

    }

    private Double computeTotal() {
        BigDecimal bigDecimal = new BigDecimal(0.0);
        for ( Invoice item : masterData ) {
            bigDecimal = bigDecimal.add(new BigDecimal(item.getAmount()));
        }
        return bigDecimal.doubleValue();
    }

    public void setMasterData(ObservableList<Invoice> masterData) {
        this.masterData = masterData;
        printTable.setItems(masterData);
        printDate.setComparator(printDate.getComparator().reversed());
    }
}

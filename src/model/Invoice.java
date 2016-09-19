package model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by Goodwin Chua on 8 Sep 2016.
 */

public class Invoice {

    private ObjectProperty<LocalDate> date;
    private SimpleIntegerProperty invoice;
    private SimpleStringProperty customer;
    private DoubleProperty amount;
    private SimpleStringProperty po;
    private SimpleStringProperty remarks;
    private SimpleBooleanProperty isPaid;
    private SimpleBooleanProperty isCancelled;

    public Invoice() {
        this.date = new SimpleObjectProperty<>(LocalDate.now());
        this.invoice = new SimpleIntegerProperty(0);
        this.customer = new SimpleStringProperty("none");
        this.amount = new SimpleDoubleProperty(0);
        this.po = new SimpleStringProperty("---");
        this.remarks = new SimpleStringProperty();
        this.isCancelled = new SimpleBooleanProperty(false);
    }

    public Invoice(LocalDate date, int invoice, String customer, double amount, String po) {
        this.date = new SimpleObjectProperty<>(date);
        this.invoice = new SimpleIntegerProperty(invoice);
        this.customer = new SimpleStringProperty(customer);
        this.amount = new SimpleDoubleProperty(amount);
        this.po = new SimpleStringProperty(po);
        this.isCancelled = new SimpleBooleanProperty(false);
    }

    public ObjectProperty<LocalDate> dateProperty(){
        return date;
    }

    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(String date) {
        DateTimeFormatter myDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.date = new SimpleObjectProperty<>(LocalDate.parse(date, myDateFormatter));
    }

    public void setDate(LocalDate date) {
        this.date = new SimpleObjectProperty<>(date);
    }

    public SimpleIntegerProperty invoiceProperty() {
        return invoice;
    }

    public int getInvoice() {
        return invoice.get();
    }

    public void setInvoice(int invoice) {
        this.invoice = new SimpleIntegerProperty(invoice);
    }

    public SimpleStringProperty customerProperty() {
        return customer;
    }

    public String getCustomer() {
        return customer.get();
    }

    public void setCustomer(String customer) {
        this.customer = new SimpleStringProperty(customer);
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount = new SimpleDoubleProperty(amount);
    }

    public SimpleStringProperty poProperty() {
        return po;
    }

    public String getPo() {
        return po.get();
    }

    public void setPo(String po) {
        this.po = new SimpleStringProperty(po);
    }

    public SimpleStringProperty remarksProperty() {
        return remarks;
    }

    public String getRemarks() {
        return remarks.get();
    }

    public void setRemarks(String remarks) {
        this.remarks = new SimpleStringProperty(remarks);
    }

    public SimpleBooleanProperty isPaidProperty() {
        return isPaid;
    }

    public boolean isPaid() {
        return isPaid.get();
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = new SimpleBooleanProperty(isPaid);
    }

    public SimpleBooleanProperty isCancelledProperty() {
        return isCancelled;
    }

    public boolean isCancelled() {
        return isCancelled.get();
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = new SimpleBooleanProperty(isCancelled);
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "date=" + getDate() +
                ", invoice=" + getInvoice() +
                ", customer=" + getCustomer() +
                ", amount=" + getAmount() +
                ", po=" + getPo() +
                ", remarks=" + getRemarks() +
                ", isPaid=" + isPaid +
                ", isCancelled=" + isCancelled +
                '}';
    }
}

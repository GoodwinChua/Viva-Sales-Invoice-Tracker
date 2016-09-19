package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by Goodwin Chua on 14 Sep 2016.
 */
public class FilterBag {

    private String invoiceFrom;
    private String invoiceTo;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String customer;
    private String amount;
    private String filterMode;

    public FilterBag() {
        invoiceFrom = null;
        invoiceTo = null;
        dateFrom = null;
        dateTo = null;
        customer = null;
        amount = null;
        filterMode = "Show All";
    }

    public String getInvoiceFrom() {
        return invoiceFrom;
    }

    public void setInvoiceFrom(String invoiceFrom) {
        this.invoiceFrom = invoiceFrom;
    }

    public String getInvoiceTo() {
        return invoiceTo;
    }

    public void setInvoiceTo(String invoiceTo) {
        this.invoiceTo = invoiceTo;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(String filterMode) {
        this.filterMode = filterMode;
    }

    @Override
    public String toString() {
        return "Filter Mode: " + filterMode + " \n" +
                "Invoice From: " + invoiceFrom + " To: " + invoiceTo + " \n" +
                "Date From: " + dateFrom + " To: " + dateTo + " \n" +
                "Customer: " + customer + " \n" +
                "Amount: " + amount;
    }

    public String constructInvoiceQuery() {
        String query = "";
        if ( invoiceFrom != null && invoiceTo != null ) {
            query += " and invoice between " + invoiceFrom + " and " + invoiceTo;
        }
        if ( invoiceFrom != null && invoiceTo == null ) {
            query += " and invoice >= " + invoiceFrom;
        }
        if ( invoiceFrom == null && invoiceTo != null ) {
            query += " and invoice <= " + invoiceTo;
        }
        return query;
    }

    public String constructDateQuery() {
        String query = "";
        if ( dateFrom != null && dateTo != null ) {
            DateTimeFormatter myDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            query += " and Orderdate between '" +
                    myDateFormatter.format(dateFrom) + "' and '" +
                    myDateFormatter.format(dateTo) + "'";
        }
        return query;
    }

    public String constructAmountQuery() {
        String query = "";
        if ( amount != null ) {
            query += " and amount >= " + amount;
        }
        return query;
    }

    public String constructCustomerQuery() {
        String query = "";
        if ( customer != null && !customer.equals("None")) {
            query += " and customer = \"" + customer + "\"";
        }
        return query;
    }

    public String constructFilterMode() {
        String query = "";
        switch ( filterMode ) {
            case "Show All": //default
                break;
            case "Show Paid":
                query += " and paid = 1";
                break;
            case "Show Unpaid":
                query += " and paid = 0";
                break;
            case "Show Cancelled": //special
                query += " and paid is null";
                break;
            case "All":
                query += " and paid is not null";
                break;
            case "Not Paid":
                query += " and paid = 0";
                break;
            case "Paid":
                query += " and paid = 1";
                break;
            default:
                break;
        }
        return query;
    }
}

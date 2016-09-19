package db;

import model.FilterBag;
import model.Invoice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Created by Goodwin Chua on 9 Sep 2016.
 */
public class InvoiceDAO extends DataAccessObject {

    DateTimeFormatter sqlDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public boolean addOrder(Invoice o) {
        try {
            String sql = "INSERT INTO ORDERS (Orderdate, invoice, customer, po, amount, paid, remarks) " +
                    "Values (?,?,?,?,?,?,?)";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, sqlDateFormat.format(o.getDate()));
            pst.setInt(2, o.getInvoice());
            pst.setString(3, o.getCustomer());
            pst.setString(4, o.getPo());
            pst.setDouble(5, o.getAmount());
            pst.setBoolean(6, false);
            pst.setString(7, o.getRemarks());
            pst.execute();
            pst.close();
            connection.close();
        } catch ( Exception e ) {
            if ( e.getMessage().contains("UNIQUE constraint failed") ) {
                editOrder(o);
                return false;
            } else
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }

    public void editOrder(Invoice o) {
        try {
            String sql = "UPDATE ORDERS set Orderdate = ?, customer = ?, po = ?, amount = ?, remarks = ?, paid = ? " +
                    "WHERE invoice = " + o.getInvoice();
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, sqlDateFormat.format(o.getDate()));
            pst.setString(2, o.getCustomer());
            pst.setString(3, o.getPo());
            pst.setDouble(4, o.getAmount());
            pst.setString(5, o.getRemarks());
            pst.setBoolean(6, false);
            pst.executeUpdate();
            pst.close();
            connection.close();
        } catch ( Exception e ) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public Invoice searchOrder(String text) {
        Invoice o = null;
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Orderdate, customer, po, amount, remarks, paid FROM ORDERS " +
                    "WHERE invoice = " + text);
            if ( rs.next() ) {
                o = new Invoice();
                o.setInvoice(Integer.parseInt(text));
                o.setAmount(rs.getDouble("amount"));
                o.setCustomer(rs.getString("customer"));
                o.setDate(rs.getString(1));
                o.setPo(rs.getString("po"));
                String status = rs.getString("paid");
                if ( status == null ) {
                    o.setCancelled(true);
                } else {
                    if ( status.equals("1") ) {
                        o.setPaid(true);
                    } else if ( status.equals("0") )
                        o.setPaid(false);
                }

                o.setRemarks(rs.getString("remarks"));
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch ( Exception e ) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return o;
    }

    public void outOrder(Invoice o) {
        try {
            String sql = "UPDATE ORDERS set PAID = ?, REMARKS = ? WHERE invoice = " + o.getInvoice();
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setBoolean(1, true);
            pst.setString(2, o.getRemarks());
            pst.executeUpdate();
            pst.close();
            connection.close();
        } catch ( Exception e ) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cancelOrder(Invoice o) {
        try {
            String sql = "UPDATE ORDERS set PAID = null, REMARKS = ? WHERE invoice = " + o.getInvoice();
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, o.getRemarks());
            pst.executeUpdate();
            pst.close();
            connection.close();
        } catch ( Exception e ) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

    }

    public ArrayList<Invoice> queryOrder(FilterBag filterBag) {
        String query = "SELECT Orderdate, invoice, customer, po, amount, paid, remarks FROM ORDERS where invoice = invoice ";
        ArrayList<Invoice> results = null;

        if ( filterBag != null ) {
            query += filterBag.constructFilterMode();
            query += filterBag.constructInvoiceQuery();
            query += filterBag.constructDateQuery();
            query += filterBag.constructAmountQuery();
            query += filterBag.constructCustomerQuery();

            if ( filterBag.getFilterMode().contains("Show") ) {
                query += " order by Orderdate DESC, invoice";
            } else {
                query += " order by paid, orderdate, invoice";
            }
        } else {
            query += " order by Orderdate DESC, invoice";
        }

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            results = new ArrayList<>();
            while ( rs.next() ) {
                Invoice temp = new Invoice();
                temp.setAmount(rs.getDouble("amount"));
                temp.setCustomer(rs.getString("customer"));
                temp.setDate(rs.getString(1));
                temp.setPo(rs.getString("po"));
                temp.setInvoice(rs.getInt("invoice"));
                temp.setRemarks(rs.getString("remarks"));
                temp.setPaid(rs.getBoolean("paid"));
                if ( rs.getObject("paid") == null ) {
                    temp.setCancelled(true);
                }
                results.add(temp);
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return results;
    }
}

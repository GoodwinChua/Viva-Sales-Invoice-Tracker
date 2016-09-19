package db;

import model.Customer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by Goodwin Chua on 9 Sep 2016.
 */
public class CustomerDAO extends DataAccessObject {

    public void addCustomer(Customer o){
        try {
            String sql = "INSERT INTO CUSTOMERS (name, address, tel) Values (?,?,?)";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, o.getName());
            pst.setString(2, o.getAddress() );
            pst.setString(3, o.getTel());
            pst.execute();
            System.out.println("Customer created successfully");
            pst.close();
            connection.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    public void editCustomer(Customer o, String oldname){
        try {
            String sql = "UPDATE CUSTOMERS SET tel = ?, address = ?, name = \"" + o.getName() + "\" WHERE name = \""+ oldname +"\"";
            String updateEntries = "UPDATE ORDERS SET CUSTOMER = \"" + o.getName() + "\" WHERE CUSTOMER = \""+ oldname +"\"";
            PreparedStatement pst = connection.prepareStatement(sql);
            PreparedStatement entries = connection.prepareStatement(updateEntries);
            pst.setString(1, o.getTel());
            pst.setString(2, o.getAddress() );
            pst.executeUpdate();
            entries.executeUpdate();
            System.out.println("Customer edited successfully");
            pst.close();
            entries.close();
            connection.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ":c " + e.getMessage() );
        }
    }

    public void deleteCustomer(Customer o){
        try {
            String sql = "DELETE FROM CUSTOMERS WHERE name = ?";
            PreparedStatement pst = connection.prepareStatement(sql);
            pst.setString(1, o.getName());
            pst.executeUpdate();
            System.out.println("Customer deleted successfully");
            pst.close();
            connection.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            System.err.println( e.getClass().getName() + ":c " + e.getMessage() );
        }
    }

    public Customer searchCustomer(String text) {
        Customer o = null;
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT name, address, tel FROM CUSTOMERS WHERE name = \""+ text +"\" ");
            if ( rs.next() ) {
                o = new Customer(rs.getString("name"), rs.getString("tel"), rs.getString("address"));
            }
            rs.close();
            stmt.close();
            connection.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            e.printStackTrace();
        }
        return o;
    }

    public ArrayList<Customer> queryCustomers(){
        String query = " SELECT name, address, tel FROM CUSTOMERS ORDER BY name";
        Statement stmt;
        ArrayList<Customer> results = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            results = new ArrayList<>();
            while(rs.next()) {
                Customer temp = new Customer(rs.getString("name"), rs.getString("tel"), rs.getString("address"));
                results.add(temp);
            }
            connection.close();
            rs.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}

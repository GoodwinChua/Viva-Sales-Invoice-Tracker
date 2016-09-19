package db;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by Goodwin Chua on 8 Sep 2016.
 */
public class SqliteConnection {

    public static Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:viva.db");
            return con;
        } catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }
}

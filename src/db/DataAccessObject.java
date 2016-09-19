package db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Goodwin Chua on 8 Sep 2016.
 */
public class DataAccessObject {

    protected Connection connection;

    public DataAccessObject(){
        openConnection();
    }

    public boolean isDatabaseConnected() {
        try {
            return !connection.isClosed();
        } catch ( SQLException e ) {
            e.printStackTrace();
            return false;
        }
    }

    public void openConnection(){
        this.connection = SqliteConnection.connect();
        if ( connection == null ) {
            System.exit(1);
        }
    }

}

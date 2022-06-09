package rs.etf.sab.DB;

import java.sql.*;
import java.util.logging.*;

public class DB {

	private static final String username = "sa";
    private static final String password = "diksjepro98";
    private static final String database = "TransportPaketa";
    private static final int port = 1433;
    private static final String serverName = "localhost";
    
    private static final String connectionString = "jdbc:sqlserver://"
            + serverName + ":" + port + ";"
            + "database=" + database + ";"
            +"user=" + username 
            + ";password=" + password;
    
    private Connection connection = null;  
    
    private DB() {
        try {
            connection = DriverManager.getConnection(connectionString);
        } catch (SQLException exception) {
            connection = null;
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, exception);
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
     
    private static DB db = null;
    
    public static DB getInstance() {
        if (db == null)
            db = new DB();
        return db;
    }

}

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSource {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String URL = "jdbc:mysql://localhost/salesa";
    static final String USER = "root";
    static final String PASSWORD = "root";

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(DataSource.JDBC_DRIVER);
        return  DriverManager.getConnection(DataSource.URL, DataSource.USER, DataSource.PASSWORD);
    }
}

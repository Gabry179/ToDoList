import org.xml.sax.InputSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.sql.*;

public class MySql {
    private static String url;
    private static String driverName;
    private static String username;
    private static String password;
    private static Connection con;

    public static Connection getConnection(){
        XPath configPath = XPathFactory.newInstance().newXPath();
        String dbUrl = "/settings/dbUrl";
        String dbDriverName = "/settings/dbDriver";
        String dbUsername = "/settings/dbUsername";
        String dbPassword = "/settings/dbPassword";
        InputSource inputSource = new InputSource("src\\config.xml");
        try {
            url = (String)configPath.compile(dbUrl).evaluate(inputSource, XPathConstants.STRING);
            driverName = (String)configPath.compile(dbDriverName).evaluate(inputSource, XPathConstants.STRING);
            username = (String)configPath.compile(dbUsername).evaluate(inputSource, XPathConstants.STRING);
            password = (String)configPath.compile(dbPassword).evaluate(inputSource, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        try {
            Class.forName(driverName);
            try {
                con = DriverManager.getConnection(url, username, password);
            } catch (SQLException ex) {
                System.out.println("Failed to create the database connection.");
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("Driver not found.");
        }
        return con;
    }
}
package backend;

import org.junit.jupiter.api.Test;
import java.sql.*;

public class MyDbTest {

    @Test
    public void testPostgresConnection() {
        String[] usernames = {"postgres", "akhi", "Akhi"};
        String[] passwords = {
            "Akhi 140808",
            "akhi 140808",
            "Akhi140808",
            "akhi140808",
            "Akhi @140808",
            "akhi @140808",
            "Akhi@140808",
            "akhi@140808",
            "Postgres@123",
            "postgres",
            "admin",
            "password",
            "123456",
            "root",
            ""
        };
        String url = "jdbc:postgresql://localhost:5432/sentinelcore";

        System.out.println("=================================================");
        System.out.println("STARTING POSTGRES MULTI-USER/PWD DIAGNOSTIC");
        System.out.println("=================================================");

        for (String user : usernames) {
            for (String pwd : passwords) {
                try {
                    Connection conn = DriverManager.getConnection(url, user, pwd);
                    System.out.println("SUCCESS: Connected with username: '" + user + "' and password: '" + pwd + "'");
                    
                    // Print table schema of incidents
                    DatabaseMetaData meta = conn.getMetaData();
                    ResultSet rs = meta.getColumns(null, null, "incidents", null);
                    System.out.println("Columns in 'incidents' table:");
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String columnName = rs.getString("COLUMN_NAME");
                        String columnType = rs.getString("TYPE_NAME");
                        System.out.println("  - " + columnName + " (" + columnType + ")");
                    }
                    if (!found) {
                        System.out.println("  - Table 'incidents' does NOT exist!");
                    }
                    
                    conn.close();
                    System.out.println("=================================================");
                    return;
                } catch (SQLException e) {
                    System.out.println("TRY FAILED: User: '" + user + "' | Pwd: '" + pwd + "' | Error: " + e.getMessage());
                }
            }
        }
        System.out.println("ALL USERNAME/PASSWORD COMBINATIONS FAILED!");
        System.out.println("=================================================");
    }
}

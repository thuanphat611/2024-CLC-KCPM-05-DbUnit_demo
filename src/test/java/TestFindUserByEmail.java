import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.sql.*;

        import static org.junit.Assert.*;

public class TestFindUserByEmail {

    private IDatabaseTester databaseTester;
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        // Kết nối tới cơ sở dữ liệu
        databaseTester = new JdbcDatabaseTester(
                "org.postgresql.Driver",
                "jdbc:postgresql://localhost:5432/MentorUS",
                "phat",
                "123"
        );

        // Thiết lập cấu hình DBUnit cho PostgreSQL
        databaseTester.setDataSet(getDataSet());
        databaseTester.setSetUpOperation(DatabaseOperation.REFRESH);
        databaseTester.setTearDownOperation(DatabaseOperation.DELETE);
        databaseTester.onSetup();

        // Thiết lập kết nối JDBC
        connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/MentorUS",
                "phat",
                "123"
        );
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
        databaseTester.onTearDown();
    }

    private IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(new FileInputStream("dataset.xml"));
    }

    @Test
    public void testQueryByEmail() throws SQLException {
        String testEmail = "john.doe2@example.com";

        String sql = "SELECT u.* FROM users u " +
                "JOIN user_additional_emails ua ON u.id = ua.user_id " +
                "WHERE u.email = ? OR ua.additional_emails = ?";

        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, testEmail);
        pstmt.setString(2, testEmail);

        ResultSet rs = pstmt.executeQuery();

        int count = 0;
        while (rs.next()) {
            count++;
            System.out.println("User ID: " + rs.getInt("id"));
            System.out.println("User Name: " + rs.getString("name"));
            System.out.println("User Email: " + rs.getString("email"));
        }
        assertEquals(1, count);
    }
}

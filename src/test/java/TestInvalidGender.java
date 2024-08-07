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

public class TestInvalidGender {

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
    public void testInsertInvalidGender() {
        String id = "3";
        String name = "Harry Johnson";
        String email = "harry.johnson@example.com";
        String gender = "INVALID"; // Giá trị không hợp lệ cho cột gender
        String provider = "local";

        // Câu lệnh SQL để chèn user mới
        String insertSql = "INSERT INTO users " +
                "(id, name, email, gender, provider, created_date, updated_date) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";


        try (PreparedStatement insertPstmt = connection.prepareStatement(insertSql)) {
            insertPstmt.setString(1, id);
            insertPstmt.setString(2, name);
            insertPstmt.setString(3, email);
            insertPstmt.setString(4, gender);
            insertPstmt.setString(5, provider);

            int rowsInserted = insertPstmt.executeUpdate();

            assertEquals(0, rowsInserted);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

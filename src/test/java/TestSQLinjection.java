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

public class TestSQLinjection {

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
    public void testSQLInjection() throws SQLException {
        String userId = "' OR '1'='1";

        // Câu truy vấn SQL với tham số
        String sql = "SELECT * FROM users WHERE id = '" + userId + "'";

        PreparedStatement pstmt = connection.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();

        // Kiểm tra kết quả
        int count = 0;
        while (rs.next()) {
            count++;
        }

        // Nếu có nhiều hơn một kết quả, thì ứng dụng của bạn có thể bị lỗ hổng SQL Injection
        assertEquals(0, count); // Kỳ vọng không có bản ghi nào được trả về
    }
}

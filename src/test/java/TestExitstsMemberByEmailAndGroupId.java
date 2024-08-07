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

public class TestExitstsMemberByEmailAndGroupId {

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
    public void testCountGroupUserByEmailAndGroupId() throws Exception {
        String sql = "SELECT COUNT(gu) > 0 " +
                "FROM group_user gu " +
                "JOIN users u ON gu.user_id = u.id " +
                "JOIN groups g ON gu.group_id = g.id " +
                "WHERE u.email = ? AND g.id = ?";

        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, "john.doe@example.com");
        pstmt.setString(2, "1");

        ResultSet rs = pstmt.executeQuery();

        assertTrue(rs.next());
        assertTrue(rs.getBoolean(1));
    }
}

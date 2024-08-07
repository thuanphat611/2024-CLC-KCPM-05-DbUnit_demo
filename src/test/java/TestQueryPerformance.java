import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestQueryPerformance {

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
        databaseTester.setSetUpOperation(DatabaseOperation.NONE);
        databaseTester.setTearDownOperation(DatabaseOperation.NONE);
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
    public void testQueryPerformance() throws Exception {
        String sql = """
            SELECT g.*, gu.*
            FROM groups g
            JOIN group_user gu ON gu.group_id = g.id
            JOIN users u ON gu.user_id = u.id
            WHERE u.id = ? 
            AND gu.is_mentor = ?
            AND g.status = ?;
        """;

        String userId = "59dea33f-5542-4603-a220-3395071f6ee0";
        boolean isMentor = false;
        String status = "ACTIVE";

        long startTime = System.nanoTime();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setBoolean(2, isMentor);
            preparedStatement.setString(3, status);
            preparedStatement.executeQuery();
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        // Chuyển đổi thời gian thực thi sang giây
        double durationInSeconds = duration / 1_000_000_000.0;

        // In thời gian thực thi
        System.out.printf("Query execution time: %.9f seconds%n", durationInSeconds);
    }
}

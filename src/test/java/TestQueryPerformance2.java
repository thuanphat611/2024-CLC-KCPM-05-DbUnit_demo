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

public class TestQueryPerformance2 {

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
        String query = """
            SELECT 
                CASE 
                    WHEN EXISTS (
                        SELECT 1
                        FROM GROUP_USER GU
                        INNER JOIN USERS U ON GU.USER_ID = U.ID
                        INNER JOIN GROUPS G ON GU.GROUP_ID = G.ID
                        WHERE U.EMAIL = ? 
                        AND G.ID = ?
                    ) 
                    THEN 1 
                    ELSE 0 
                END;
        """;

        String userEmail = "Wade_Cavanagh3269@evyvh.video";
        String groupId = "076d0d9d-d013-4cd2-9392-eb10a5b5cb4a";
        long startTime = System.nanoTime();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userEmail);
            preparedStatement.setString(2, groupId);
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

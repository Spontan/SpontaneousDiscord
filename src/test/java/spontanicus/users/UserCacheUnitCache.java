package spontanicus.users;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

public class UserCacheUnitCache {
    private static final String testFolder = "testDb/";
    private static final String dbPath =  testFolder + "users.db";
    private static final String dbUrl = "jdbc:sqlite:" + dbPath;

    @BeforeAll
    public static void createDbFolder() throws IOException {
        cleanup();
        Files.createDirectory(Paths.get(testFolder));
    }

    @BeforeEach
    public void clearDb() throws SQLException {
        try (Connection dbConnection = DriverManager.getConnection(dbUrl);
            Statement stmt = dbConnection.createStatement()){
            LinkedList<String> tableNames = new LinkedList<>();
            try (ResultSet tables = dbConnection.getMetaData().getTables(null, null, "%", null)) {
                while (tables.next()) {
                    tableNames.add(tables.getString(3));
                }
            }
            for(String tableName : tableNames){
                stmt.execute("DROP TABLE " + tableName);
            }
        }
    }

    @AfterAll
    public static void cleanup() throws IOException {
        Files.walkFileTree(Paths.get(testFolder), new FileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException
            {
                if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw e;
                }
            }
        });
    }

    @Test
    public void createUser() throws SQLException {
        long userId = 9876543210L;
        UserCache.switchDb(dbPath);
        UserCache users = UserCache.getInstance();

        User user = users.getUser(userId);
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getNotificationMessage()).isEqualTo("{name} hat einen Stream gestartet <3");
        assertThat(user.isNotifyAutomatically()).isEqualTo(false);

        checkUserInDb(user);
    }

    @Test
    public void updateExistingUser() throws SQLException {
        long userId = 9876543210L;
        UserCache.switchDb(dbPath);
        UserCache users = UserCache.getInstance();

        User user = users.getUser(userId);
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getNotificationMessage()).isEqualTo("{name} hat einen Stream gestartet <3");
        assertThat(user.isNotifyAutomatically()).isEqualTo(false);

        checkUserInDb(user);

        user.setNotificationMessage("test");
        user.setNotifyAutomatically(true);
        users.updateUser(user);

        checkUserInDb(user);
    }

    private void checkUserInDb(User user) throws SQLException {
        try (Connection dbConnection = DriverManager.getConnection(dbUrl);
             Statement stmt = dbConnection.createStatement();
             ResultSet result = stmt.executeQuery("SELECT * FROM USER WHERE ID=" + user.getId())){
            assertThat(result.next()).isTrue();

            String notificationMessage = result.getString("NOTIFICATION_MESSAGE");
            boolean notifyAutomatically = result.getBoolean("NOTIFY_AUTOMATICALLY");

            assertThat(notificationMessage).isEqualTo(user.getNotificationMessage());
            assertThat(notifyAutomatically).isEqualTo(user.isNotifyAutomatically());
        }
    }
}

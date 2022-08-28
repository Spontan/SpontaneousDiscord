package spontanicus.users;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class UserCache {
    private final static Logger logger = Logger.getLogger("UserCache");

    private static final String ID = "ID";
    private static final String NOTIFICATION_MESSAGE = "NOTIFICATION_MESSAGE";
    private static final String NOTIFY_AUTOMATICALLY = "NOTIFY_AUTOMATICALLY";
    private static final String INIT_USER_TABLE = "CREATE TABLE IF NOT EXISTS USER (" +
            ID + " LONG PRIMARY KEY," +
            NOTIFICATION_MESSAGE + " TEXT NOT NULL," +
            NOTIFY_AUTOMATICALLY + " BOOLEAN NOT NULL" +
            ")";
    private static final String SELECT_ALL_USERS = "SELECT * FROM USER";


    private static UserCache instance;
    private static final String dbPrefix = "jdbc:sqlite:";
    private static String dbPath = "db/notification.db";
    private final String dbUrl;


    private Map<Long, User> userData;
    private boolean isClosed = false;

    protected UserCache(String dbPath){
        dbUrl = dbPrefix + dbPath;
        try (Connection dbConnection = DriverManager.getConnection(dbUrl);
            Statement stmt = dbConnection.createStatement()){
            stmt.execute(INIT_USER_TABLE);
        } catch (SQLException e) {
            logger.severe("Error while creating user table");
        }
    }

    public static UserCache getInstance(){
        if(instance == null){
            instance = new UserCache(dbPrefix + dbPath);
        }
        return instance;
    }

    protected synchronized void close(){
        isClosed = true;
    }

    public boolean isClosed(){
        return isClosed;
    }

    public static void switchDb(String path){
        if(instance != null)
            instance.close();
        dbPath = path;
        instance = new UserCache(dbPath);
    }

    public synchronized Map<Long, User> getUserData(){
        if(isClosed)
            throw new IllegalStateException("Database connection has been closed");
        if(userData == null)
            reloadUserData();
        return userData;
    }

    private void reloadUserData(){
        userData = new HashMap<>();

        try (Connection dbConnection = DriverManager.getConnection(dbUrl);
             Statement sqlStatement = dbConnection.createStatement();
             ResultSet userResult = sqlStatement.executeQuery(SELECT_ALL_USERS)){
            while(userResult.next()){
                long userId = userResult.getLong(ID);
                String notificationMessage = userResult.getString(NOTIFICATION_MESSAGE);
                boolean notifyAutomatically = userResult.getBoolean(NOTIFY_AUTOMATICALLY);

                User newUser = new User(userId);
                newUser.setNotificationMessage(notificationMessage);
                newUser.setNotifyAutomatically(notifyAutomatically);

                userData.put(userId, newUser);
            }
        } catch (SQLException e) {
            logger.severe("Couldn't connect to user DB, only default settings will be available");
            userData = new HashMap<>();
        }
    }

    public synchronized User getUser(long id){
        if(isClosed)
            throw new IllegalStateException("Database connection has been closed");
        if(userData == null)
            reloadUserData();
        if(!userData.containsKey(id)){
            return insertUser(new User(id));
        }
        return userData.get(id);
    }

    private User insertUser(User user) {
        long id = user.getId();
        reloadUserData();
        if(userData.containsKey(id)) {
            logger.warning("User " + id + " already exists");
            return userData.get(id);
        }

        user.setNotificationMessage(removeQuotes(user.getNotificationMessage()));
        try (Connection dbConnection = DriverManager.getConnection(dbUrl);
            Statement sqlStatement = dbConnection.createStatement()){
            sqlStatement.execute("INSERT INTO USER (" + ID + "," + NOTIFICATION_MESSAGE + "," + NOTIFY_AUTOMATICALLY + ") " +
                    "VALUES (" + id + ",'" + user.getNotificationMessage() + "'," + user.isNotifyAutomatically() + ")");
            userData.put(id, user);
        } catch (SQLException e) {
            logger.severe("Error while persisting user " + id);
        }
        return user;
    }

    private String removeQuotes(String message) {
        return message.replaceAll("'", "\"");
    }

    public synchronized void updateUser(User user){
        if(isClosed)
            throw new IllegalStateException("Database connection has been closed");
        if(userData == null)
            reloadUserData();
        if(!userData.containsKey(user.getId())){
            insertUser(user);
        }else{
            user.setNotificationMessage(removeQuotes(user.getNotificationMessage()));
            try (Connection dbConnection = DriverManager.getConnection(dbUrl)){
                Statement sqlStatement = dbConnection.createStatement();
                sqlStatement.execute("UPDATE USER SET " + NOTIFICATION_MESSAGE + "='" + user.getNotificationMessage() + "'," +
                        NOTIFY_AUTOMATICALLY + "=" + user.isNotifyAutomatically() + " WHERE " + ID + "=" + user.getId());
            } catch (SQLException e) {
                logger.severe("Error while persisting user " + user.getId());
            }
        }
    }
}

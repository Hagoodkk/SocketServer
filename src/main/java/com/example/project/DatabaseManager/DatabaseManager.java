package com.example.project.DatabaseManager;

import com.example.project.Serializable.BuddyList;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private static DatabaseManager databaseManager;
    private Connection connection;

    private final String h2Folder = "jdbc:h2:~/ChaterDatabase";
    private final String h2Username = "hVwiUW4ujGAhmS6s";
    private final String h2Password = "2KNlRLJG0ZqRTRpd";

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
            databaseManager.createConnection();
        }
        return databaseManager;
    }

    private void createConnection() {
        try {
            this.connection = DriverManager.getConnection(h2Folder, h2Username, h2Password);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            System.out.println("Could not create database connection.");
            System.exit(1);
        }
    }

    public boolean createTables() {
        try {
            Statement statement = connection.createStatement();
            String createUsersTable = "CREATE TABLE Users (\n" +
                    "\tUserID Integer PRIMARY KEY AUTO_INCREMENT,\n" +
                    "    Username varchar(255) NOT NULL,\n" +
                    "    PasswordSaltedHash varchar(255) NOT NULL,\n" +
                    "    PasswordSalt varchar(255) NOT NULL\n" +
                    ");";
            statement.executeUpdate(createUsersTable);
            String createBuddyListTable = "CREATE TABLE BuddyList (\n" +
                    "\tUserID Integer NOT NULL,\n" +
                    "    BuddyID Integer NOT NULL,\n" +
                    "    CONSTRAINT FK_UserKey FOREIGN KEY (UserID)\n" +
                    "    REFERENCES Users(UserID),\n" +
                    "    CONSTRAINT FK_BuddyKey FOREIGN KEY (BuddyID)\n" +
                    "    REFERENCES Users(UserID)\n" +
                    ");";
            statement.executeUpdate(createBuddyListTable);
            return true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }

    public boolean addUser(String username, String passwordSaltedHash, String passwordSalt) {
        try {
            Statement statement = connection.createStatement();
            String addUser =
                    "INSERT INTO Users (Username, PasswordSaltedHash, PasswordSalt) " +
                            "VALUES ('" + username + "', '" + passwordSaltedHash + "', '" + passwordSalt + "')";
            statement.executeUpdate(addUser);
            return true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }

    public boolean removeUser(String username) {
        try {
            Statement statement = connection.createStatement();
            String deleteUserBuddies =
                    "DELETE FROM BuddyList WHERE UserID=(SELECT UserID FROM Users WHERE Username='" + username + "')";
            statement.executeUpdate(deleteUserBuddies);
            String deleteFromBuddies =
                    "DELETE FROM BuddyList WHERE BuddyID=(SELECT UserID FROM Users WHERE Username='" + username + "')";
            statement.executeUpdate(deleteFromBuddies);
            String deleteUser = "DELETE FROM Users WHERE Username='" + username + "'";
            statement.executeUpdate(deleteUser);
            return true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }

    public boolean addBuddyToUser(String username, String buddyName) {
        try {
            Statement statement = connection.createStatement();
            String addBuddy = "INSERT INTO BuddyList (UserID, BuddyID) VALUES " +
                    "((SELECT UserID FROM Users WHERE Username='" + username + "'), " +
                    "(SELECT UserID FROM Users WHERE Username='" + buddyName + "'))";
            statement.executeUpdate(addBuddy);
            return true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }

    public boolean removeBuddyFromUser(String username, String buddyName) {
        try {
            Statement statement = connection.createStatement();
            String removeBuddy = "DELETE FROM BuddyList WHERE " +
                    "UserID=(SELECT UserID FROM Users WHERE Username='" + username + "') AND " +
                    "BuddyID=(SELECT UserID FROM Users WHERE Username='" + buddyName + "')";
            statement.executeUpdate(removeBuddy);
            return true;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }

    public boolean comparePasswordSaltedHash(String username, String passwordSaltedHash) {
        try {
            Statement statement = connection.createStatement();
            String usernameSaltedHash = "SELECT PasswordSaltedHash FROM Users WHERE Username='" + username + "'";
            ResultSet rs = statement.executeQuery(usernameSaltedHash);
            if (rs.next()) {
                String storedPasswordSaltedHash = rs.getString("PasswordSaltedHash");
                if (storedPasswordSaltedHash.equals(passwordSaltedHash)) return true;
            }
            return false;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return false;
        }
    }

    public BuddyList getBuddyList(String username) {
        ArrayList<String> buddies = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            String getBuddies = "SELECT Username FROM Users\n" +
                    "INNER JOIN BuddyList ON Users.UserID=BuddyList.BuddyID\n" +
                    "WHERE BuddyList.UserID=(SELECT UserID FROM Users WHERE Username='" + username + "')";
            ResultSet rs = statement.executeQuery(getBuddies);
            while (rs.next()) {
                buddies.add(rs.getString("Username"));
            }
            return new BuddyList(buddies);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return null;
        }
    }

    public void dropAllTables() {
        try {
            Statement statement = connection.createStatement();
            String dropUsers = "DROP TABLE Users";
            String dropBuddies = "DROP TABLE BuddyList";
            statement.executeUpdate(dropBuddies);
            statement.executeUpdate(dropUsers);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}

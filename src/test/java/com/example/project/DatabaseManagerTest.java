package com.example.project;

import com.example.project.DatabaseManager.DatabaseManager;
import com.example.project.Serializable.BuddyList;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabaseManagerTest {
    private DatabaseManager databaseManager = DatabaseManager.getInstance();

    @Test
    public void stage1_createTablesTest() throws Exception {
        databaseManager.createTables();
    }
    @Test
    public void stage2_addUserTest() throws Exception {
        databaseManager.addUser("Alice", "TestSaltedHash", "TestSalt");
        databaseManager.addUser("Bob", "TestSaltedHash2", "TestSalt2");
        databaseManager.addUser("Charles", "TestSaltedHash3", "TestSalt3");
    }
    @Test
    public void stage3_addBuddyToUserTest() throws Exception {
        databaseManager.addBuddyToUser("Alice", "Bob");
        databaseManager.addBuddyToUser("Alice", "Charles");
        databaseManager.addBuddyToUser("Charles", "Alice");
    }
    @Test
    public void stage4_comparePasswordSaltedHashTest() throws Exception {
        assert(databaseManager.comparePasswordSaltedHash("Alice", "TestSaltedHash"));
        assert(!databaseManager.comparePasswordSaltedHash("Alice", "This"));
    }
    @Test
    public void stage5_getBuddyListTest() throws Exception {
        BuddyList buddyList = databaseManager.getBuddyList("Alice");
        ArrayList<String> buddies = buddyList.getBuddies();
        assert(buddies.size() == 2);
        assert(buddies.contains("Bob"));
        assert(buddies.contains("Charles"));
        buddyList = databaseManager.getBuddyList("Charles");
        buddies = buddyList.getBuddies();
        assert(buddies.size() == 1);
        assert(buddies.contains("Alice"));
    }
    @Test
    public void stage6_removeBuddyFromUserTest() throws Exception {
        databaseManager.removeBuddyFromUser("Alice", "Charles");
        BuddyList buddyList = databaseManager.getBuddyList("Alice");
        ArrayList<String> buddies = buddyList.getBuddies();
        assert(buddies.size() == 1);
        assert(buddies.contains("Bob"));
    }
    @Test
    public void stage7_removeUserTest() throws Exception {
        databaseManager.removeUser("Bob");
        BuddyList buddyList = databaseManager.getBuddyList("Alice");
        ArrayList<String> buddies = buddyList.getBuddies();
        assert(buddies.size() == 0);
    }
    @Test
    public void stage8_resetTests() throws Exception {
        databaseManager.dropAllTables();
    }
}

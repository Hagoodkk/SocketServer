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
        assert(databaseManager.addUser("Alice", "TestSaltedHash", "TestSalt"));
        assert(databaseManager.addUser("Bob", "TestSaltedHash2", "TestSalt2"));
        assert(databaseManager.addUser("Charles", "TestSaltedHash3", "TestSalt3"));
        assert(!databaseManager.addUser("Alice", "TestSaltedHash", "TestSalt"));
    }
    @Test
    public void stage3_addBuddyToUserTest() throws Exception {
        assert(databaseManager.addBuddyToUser("Alice", "Bob"));
        assert(databaseManager.addBuddyToUser("Alice", "Charles"));
        assert(databaseManager.addBuddyToUser("Charles", "Alice"));
        assert(!databaseManager.addBuddyToUser("Alice", "Bob"));
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
        assert(buddies.contains("bob"));
        assert(buddies.contains("charles"));
        buddyList = databaseManager.getBuddyList("Charles");
        buddies = buddyList.getBuddies();
        assert(buddies.size() == 1);
        assert(buddies.contains("alice"));
    }
    @Test
    public void stage6_removeBuddyFromUserTest() throws Exception {
        databaseManager.removeBuddyFromUser("Alice", "Charles");
        BuddyList buddyList = databaseManager.getBuddyList("Alice");
        ArrayList<String> buddies = buddyList.getBuddies();
        assert(buddies.size() == 1);
        assert(buddies.contains("bob"));
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

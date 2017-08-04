package com.example.project;

import com.example.project.DatabaseManager.DatabaseManager;
import com.example.project.Serializable.BuddyList;
import com.example.project.Serializable.Message;
import com.example.project.Serializable.ServerHello;
import com.example.project.Serializable.UserCredentials;
import com.example.project.SessionManager.SessionManager;
import org.h2.engine.User;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by Kyle on 7/4/2017.
 */

public class ChatThread implements Runnable {

    private Socket clientSocket;

    public ChatThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        int requestType = pingClient();
        if (requestType == 0) {
            accountCreation();
        } else if (requestType == 1){
            String verifiedUsername = verifyCredentials();
            if (verifiedUsername != null) establishedConnection(verifiedUsername);
            else System.out.println("Connection failed.");
        } else if (requestType == -1) {
            System.out.println("Connection failed.");
        }
    }

    private boolean accountCreation() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            UserCredentials userCredentials = (UserCredentials) ois.readObject();
            String username = userCredentials.getUsername().toLowerCase();
            String passwordSaltedHash = userCredentials.getPasswordSaltedHash();
            String passwordSalt = userCredentials.getPasswordSalt();

            System.out.println(passwordSalt);

            boolean accountCreated = false;

            if (isValid(username) && isValid(passwordSaltedHash) && isValid(passwordSalt)) {
                DatabaseManager databaseManager = DatabaseManager.getInstance();
                accountCreated = (databaseManager.addUser(username, passwordSaltedHash, passwordSalt));
            }

            if (accountCreated) {
                userCredentials.setRequestAccepted(true);
                oos.writeObject(userCredentials);
                oos.flush();
                return true;
            } else {
                userCredentials.setRequestAccepted(false);
                oos.writeObject(userCredentials);
                oos.flush();
                return false;
            }

        } catch (IOException ioe) {
            return false;
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return false;
        }
    }

    private boolean isValid(String entry) {
        if (entry.contains(" ")) return false;
        if (entry.length() == 0) return false;
        return true;
    }

    private int pingClient() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            ServerHello serverHello = (ServerHello) ois.readObject();
            oos.writeObject(new ServerHello());
            oos.flush();
            if (serverHello.isRequestUserCreation()) return 0;
            if (serverHello.isRequestLogin()) return 1;
            else return -1;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return -1;
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return -1;
        }
    }

    private String verifyCredentials() {
        UserCredentials userCredentials =  null;
        SessionManager sessionManager = SessionManager.getInstance();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            userCredentials = (UserCredentials) ois.readObject();
            String username = userCredentials.getUsername();

            DatabaseManager databaseManager = DatabaseManager.getInstance();
            String salt = databaseManager.getUserSalt(username);
            userCredentials.setPasswordSalt(salt);

            System.out.println(salt);

            oos.writeObject(userCredentials);
            oos.flush();

            userCredentials = (UserCredentials) ois.readObject();
            if (databaseManager.comparePasswordSaltedHash(userCredentials.getUsername(), userCredentials.getPasswordSaltedHash())) {
                userCredentials.setRequestAccepted(true);
                oos.writeObject(userCredentials);
                oos.flush();

                oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ois = new ObjectInputStream(clientSocket.getInputStream());

                BuddyList buddyList = (BuddyList) ois.readObject();

                oos.writeObject(buildBuddyList(username));

                sessionManager.addMember(username, clientSocket);
                sessionManager.printClientTracker();
                System.out.println(userCredentials.getUsername() + " connected.");
                return username;
            } else {
                userCredentials.setRequestAccepted(false);
                oos.writeObject(userCredentials);
                oos.flush();
                System.out.println("Connection refused.");
                return null;
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return null;
        }
    }

    private void establishedConnection(String username) {
        SessionManager sessionManager = SessionManager.getInstance();

        try {
            ObjectOutputStream toClient = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream fromClient = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                Message clientInbound = (Message) fromClient.readObject();
                if (!clientInbound.isNullMessage()) {
                    String recipient = clientInbound.getRecipient();
                    System.out.println("Inbound: " + clientInbound.getMessage());
                    if (sessionManager.isOnline(recipient)) {
                        sessionManager.addOutgoingMessage(recipient, clientInbound);
                        System.out.println(recipient);
                    }
                }
                Message clientOutbound = sessionManager.getNextOutgoing(username);
                if (!clientOutbound.isNullMessage()) System.out.println("Outbound: " + clientOutbound.getMessage());
                if (clientInbound.isBuddyListUpdate()) {
                    clientOutbound.setBuddyList(buildBuddyList(username));
                    clientOutbound.setBuddyListUpdate(true);
                }
                toClient.writeObject(clientOutbound);
                toClient.flush();
            }

        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } finally {
            try {
                System.out.println(username + " disconnected.");
                clientSocket.close();
                sessionManager.removeMember(username);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private BuddyList buildBuddyList(String username) {
        ArrayList<String> buddies = new ArrayList<>();
        buddies.add("Carl");
        buddies.add("Joan");
        buddies.add("Bob");
        BuddyList buddyList = new BuddyList(buddies);

        ArrayList<String> currentlyOffline = new ArrayList<>();
        ArrayList<String> currentlyOnline = new ArrayList<>();

        SessionManager sessionManager = SessionManager.getInstance();
        for (String buddy : buddyList.getBuddies()) {
            if (sessionManager.isOnline(buddy)) currentlyOnline.add(buddy);
            else currentlyOffline.add(buddy);
        }
        buddyList.setCurrentlyOffline(currentlyOffline);
        buddyList.setCurrentlyOnline(currentlyOnline);
        return buddyList;
    }
}

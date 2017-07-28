package com.example.project;

import com.example.project.Serializable.BuddyList;
import com.example.project.Serializable.Message;
import com.example.project.Serializable.UserCredentials;
import com.example.project.SessionManager.SessionManager;

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

        UserCredentials userCredentials = null;
        SessionManager sessionManager = SessionManager.getInstance();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            userCredentials = (UserCredentials) ois.readObject();
            String username = userCredentials.getUsername();
            sessionManager.addMember(username, clientSocket);

            System.out.println(userCredentials.getUsername() + " connected.");
            ArrayList<String> buddies = new ArrayList<>();
            buddies.add("Carl");
            buddies.add("Joan");
            buddies.add("Bob");
            BuddyList buddyList = new BuddyList(buddies);
            ArrayList<String> currentlyOffline = new ArrayList<>();
            ArrayList<String> currentlyOnline = new ArrayList<>();
            sessionManager.printClientTracker();
            for (String buddy : buddyList.getBuddies()) {
                if (sessionManager.isOnline(buddy)) currentlyOnline.add(buddy);
                else currentlyOffline.add(buddy);
            }
            buddyList.setCurrentlyOffline(currentlyOffline);
            buddyList.setCurrentlyOnline(currentlyOnline);
            oos.writeObject(buddyList);

            ObjectOutputStream toClient = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream fromClient = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                Message clientInbound = (Message) fromClient.readObject();
                if (!clientInbound.isNullMessage()) {
                    String recipient = clientInbound.getRecipient();
                    System.out.println(clientInbound.getMessage());
                    sessionManager.addOutgoingMessage(recipient, clientInbound);
                }
                Message clientOutbound = sessionManager.getNextOutgoing(username);
                toClient.writeObject(clientOutbound);
                toClient.flush();
            }

        } catch (SocketException se) {
            System.out.println(userCredentials.getUsername() + " disconnected.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                sessionManager.removeMember(userCredentials.getUsername());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}

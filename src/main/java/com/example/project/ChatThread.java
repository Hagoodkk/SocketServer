package com.example.project;

import com.example.project.Serializable.BuddyList;
import com.example.project.Serializable.UserCredentials;

import java.io.*;
import java.net.Socket;
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

        String username = "";
        ClientTracker clientTracker = ClientTracker.getInstance();

        try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {
            UserCredentials userCredentials = (UserCredentials) ois.readObject();
            clientTracker.addMember(userCredentials.getUsername(), clientSocket.getOutputStream());
            System.out.println(userCredentials.getUsername() + " connected.");
            oos.writeObject(null);
            BuddyList buddyList = (BuddyList) ois.readObject();
            ArrayList<String> currentlyOffline = new ArrayList<>();
            ArrayList<String> currentlyOnline = new ArrayList<>();
            for (String buddy : buddyList.getBuddyList()) {
                if (clientTracker.isOnline(buddy)) currentlyOnline.add(buddy);
                else currentlyOffline.add(buddy);
            }
            buddyList = new BuddyList(buddyList.getBuddyList());
            buddyList.setCurrentlyOffline(currentlyOffline);
            buddyList.setCurrentlyOnline(currentlyOnline);
            oos.writeObject(buddyList);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                clientTracker.removeMember(username);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}

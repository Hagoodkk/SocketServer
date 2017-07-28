package com.example.project;

import com.example.project.Serializable.BuddyList;
import com.example.project.Serializable.Message;
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

        UserCredentials userCredentials = null;
        ClientTracker clientTracker = ClientTracker.getInstance();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            userCredentials = (UserCredentials) ois.readObject();
            clientTracker.addMember(userCredentials.getUsername(), clientSocket.getOutputStream());

            System.out.println(userCredentials.getUsername() + " connected.");
            ArrayList<String> buddies = new ArrayList<>();
            buddies.add("Carl");
            buddies.add("Joan");
            buddies.add("Bob");
            BuddyList buddyList = new BuddyList(buddies);
            ArrayList<String> currentlyOffline = new ArrayList<>();
            ArrayList<String> currentlyOnline = new ArrayList<>();
            clientTracker.printClientTracker();
            for (String buddy : buddyList.getBuddies()) {
                if (clientTracker.isOnline(buddy)) currentlyOnline.add(buddy);
                else currentlyOffline.add(buddy);
            }
            buddyList.setCurrentlyOffline(currentlyOffline);
            buddyList.setCurrentlyOnline(currentlyOnline);
            oos.writeObject(buddyList);

            ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {

                Message incomingMessage = (Message) inputStream.readObject();
                Message responseMessage = new Message();
                responseMessage.setNullMessage(true);
                outputStream.writeObject(responseMessage);
                outputStream.flush();
                if (!incomingMessage.isNullMessage()) {
                    String sender = incomingMessage.getSender();
                    String recipient = incomingMessage.getRecipient();
                    String message = incomingMessage.getMessage();
                    if (clientTracker.isOnline(recipient)) {
                        outputStream.writeObject(new Message(sender, recipient, message));
                        outputStream.flush();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                clientTracker.removeMember(userCredentials.getUsername());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }


}

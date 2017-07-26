package com.example.project;

import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by Kyle on 7/4/2017.
 */

public class ClientTracker {
    private static ClientTracker clientTracker;
    private static HashMap<String, OutputStream> clients;

    private ClientTracker() {}

    public static ClientTracker getInstance() {
        if (clientTracker == null) {
            clientTracker = new ClientTracker();
            clients = new HashMap<>();
        }
        return clientTracker;
    }

    public boolean isOnline(String username) {
        if (clients.containsKey(username)) return true;
        return false;
    }


    public static void addMember(String username, OutputStream outputStream) {
        clients.put(username, outputStream);
    }

    public static void removeMember(String username) {
        clients.remove(username);
    }

    public static void printClientTracker() {
        System.out.println(clients.keySet());
    }

}

package com.rancho.web.admin.websocket;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHolder {

    private static Map<String, Session> clients = new ConcurrentHashMap<>();

    public static void put(String id,Session session){
        clients.put(id,session);
    }

    public static void remove(String id) throws IOException {
        Session session=clients.remove(id);
        session.close();
    }

    public static Map<String,Session> getClients(){
        return clients;
    }

    public static Session getClient(String key){
        return clients.get(key);
    }

    public static void sendAll(String msg){
        for (Map.Entry<String, Session> sessionEntry : clients.entrySet()) {
            Session session=sessionEntry.getValue();
            session.getAsyncRemote().sendText(msg);
        }
    }

    public static void send(String id,String msg){
        Session session=clients.get(id);
        session.getAsyncRemote().sendText(msg);
    }
}

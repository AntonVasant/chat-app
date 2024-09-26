package websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import services.UserService;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ServerEndpoint("/websocket")
public class WebSocketServer {
    private static  ConcurrentMap<Integer, Session> clients = new ConcurrentHashMap<>();
    private static  ConcurrentMap<String, String> sessionToUserMap = new ConcurrentHashMap<>();

    private static ConcurrentMap<Integer,Integer> activeStatusMap = new ConcurrentHashMap<>();

    private final UserService userService = UserService.getUserService();

    @OnOpen
    public void onOpen(Session session) {
        URI requestUri = session.getRequestURI();
        String query = requestUri.getQuery();
        String userId = null;
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "userId".equals(keyValue[0])) {
                    userId = keyValue[1];
                    break;
                }
            }
        }
        int n = Integer.parseInt(userId);
        System.out.println("Connected user ID: " + userId);
        sessionToUserMap.put(session.getId(),userId);
        clients.put(n,session);
    }


    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            String type = jsonMessage.get("type").getAsString();
            int sender = jsonMessage.get("sender").getAsInt();
            int receiver = jsonMessage.get("receiver").getAsInt();
            System.out.println("type "+type);
            if (type.equals("status")){
                String state = jsonMessage.get("state").getAsString();
                System.out.println("statys called "+state+sender+receiver);
                if (state.equals("activeChat")){
                    activeStatusMap.put(sender,receiver);
                }
                else{
                    if (activeStatusMap.containsKey(sender)){
                        System.out.println("removing "+sender);
                        activeStatusMap.remove(sender);
                    }
                }
            }
            else{
                String senderName = jsonMessage.get("senderName").getAsString();
                JsonObject jsonObject = new JsonObject();
                String content = jsonMessage.get("content").getAsString();
                jsonObject.addProperty("content", content);
                jsonObject.addProperty("sender", sender);
                jsonObject.addProperty("receiver", receiver);
                jsonObject.addProperty("senderName", senderName);
                jsonObject.addProperty("timestamp", String.valueOf(new Timestamp(System.currentTimeMillis())));
                Session recipientSession = clients.get(receiver);
                if (recipientSession != null && recipientSession.isOpen()) {
                    recipientSession.getBasicRemote().sendText(jsonObject.toString());
                }
                boolean b = activeStatusMap.containsKey(receiver);
                userService.saveMessageToDatabase(sender, receiver, content,b);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error here");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unexpected error in onMessage");
        }
    }


    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error in WebSocket: " + throwable.getMessage());
    }
    @OnClose
    public void onClose(Session session) {
        System.out.println("closing ");
        String senderId = sessionToUserMap.get(session.getId());
        if (senderId != null) {
            clients.remove(senderId);
            sessionToUserMap.remove(session.getId());
            System.out.println("Connection closed for user: " + senderId);
        }
    }
}

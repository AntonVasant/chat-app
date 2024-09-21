import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ServerEndpoint("/websocket")
public class WebSocketServer {
    private static  ConcurrentMap<String, Session> clients = new ConcurrentHashMap<>();
    private static  ConcurrentMap<String, String> sessionToUserMap = new ConcurrentHashMap<>();

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
        System.out.println("Connected user ID: " + userId);
        clients.put(userId,session);
    }
    @OnMessage
    public void onMessage(String message, Session session ) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            System.out.println(jsonMessage.toString());
            String sender = jsonMessage.get("sender").getAsString();
            String receiver = jsonMessage.get("receiver").getAsString();
            JsonObject jsonObject = new JsonObject();
            String content = jsonMessage.get("content").getAsString();
            jsonObject.addProperty("content", content);
            jsonObject.addProperty("senderName", sender);
            jsonObject.addProperty("receiverName", receiver);
            Session recipientSession = clients.get(receiver);
            if (recipientSession != null && recipientSession.isOpen()) {
                recipientSession.getBasicRemote().sendText(jsonObject.toString());
            }
            userService.saveMessageToDatabase(sender,receiver,content);
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("error here");
        }
    }
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error in WebSocket: " + throwable.getMessage());
    }
    @OnClose
    public void onClose(Session session) {
        String senderId = sessionToUserMap.get(session.getId());
        if (senderId != null) {
            clients.remove(senderId);
            sessionToUserMap.remove(session.getId());
            System.out.println("Connection closed for user: " + senderId);
        }
    }

    private Map<String, String> getQueryParams(URI uri) {
        Map<String, String> queryPairs = new HashMap<>();
        String query = uri.getQuery();
        System.out.println(query);
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0 && idx < pair.length() - 1) {
                    queryPairs.put(pair.substring(0, idx), pair.substring(idx + 1));
                }
            }
        }
        return queryPairs;
    }
}

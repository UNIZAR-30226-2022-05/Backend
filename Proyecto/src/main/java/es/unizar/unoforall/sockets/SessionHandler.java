package es.unizar.unoforall.sockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SessionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionHandler.class);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public static void register(WebSocketSession session) {
        sessionMap.put(session.getId(), session);
    }
    
    public static void logout(String sesionID) {
    	WebSocketSession session = sessionMap.remove(sesionID);
    	if (session != null && session.isOpen()) {
    		try {
				session.close();
			} catch (IOException e) {
			}
    	}
    }

}
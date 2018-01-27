package mm;

import mm.message.Message;
import mm.message.Topic;
import mm.model.GameSession;
import mm.network.Broker;
import mm.network.ConnectionPool;
import mm.storage.SessionStorage;
import mm.ticker.Action;
import mm.ticker.Ticker;
import mm.util.JsonHelper;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EventHandler extends TextWebSocketHandler implements WebSocketHandler {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EventHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        MultiValueMap<String, String> parameters =
                UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams();
        String idParam = parameters.get("gameId").toString();
        String name = parameters.get("name").toString();
        name = name.substring(1, name.length() - 1);
        long gameId = Long.parseLong(idParam.substring(1, idParam.length() - 1));
        GameSession gameSession = SessionStorage.getSessionById(gameId);

        if (!gameSession.isReady()) {
            SessionStorage.addByGameId(gameId, session);
            ConnectionPool.getInstance().add(session, name);
            int data = SessionStorage.getId(gameId);
            Broker.getInstance().send(session, Topic.POSSESS, data);
            gameSession.addPlayer(data);
            SessionStorage.putGirlToSocket(session, gameSession.getById(gameSession.getLastId()));
            Broker.getInstance().send(session, Topic.REPLICA,
                    SessionStorage.getSessionById(gameId).getGameObjects());
            if (gameSession.getPlayerCount()
                    == SessionStorage.getWebsocketsByGameSession(gameSession).size()) {
                Ticker ticker = new Ticker(gameSession);
                SessionStorage.putTicker(ticker, gameSession);
                ticker.setName("gameId : " + gameId);
                ticker.begin();
                ticker.start();
            }
        } else {
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (SessionStorage.getByWebsocket(session).isReady()) {
            Message msg = JsonHelper.fromJson(message.getPayload(), Message.class);
            Action action = new Action(msg.getTopic(),
                    SessionStorage.getPlayerBySocket(session), msg.getData());
            SessionStorage.putAction(SessionStorage.getByWebsocket(session), action);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Socket Closed: [" +
                closeStatus.getCode() + "] " + closeStatus.getReason());
        SessionStorage.removeWebsocket(session);
        super.afterConnectionClosed(session, closeStatus);
    }

}

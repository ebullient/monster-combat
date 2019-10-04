/*******************************************************************************
 * Copyright (c) 2017,2018 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package org.gameontext.sample;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;

import javax.websocket.CloseReason;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Component
public class SocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    @Autowired
    private RoomImplementation roomImplementation;

    private final HashMap<String, WebSocketSession> sessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        session.sendMessage(new TextMessage(Message.ACK_MSG.toString()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        logger.info("WebSocketSession with Id (" + session.getId() + ") closed with reason: " + status.getReason());
    }

    /**
     * The hook into the interesting room stuff
     *
     * @param session WebSocketSession we received the Message from
     * @param message Message to handle
     * @throws IOException On error
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        roomImplementation.handleMessage(new Message(message.getPayload().toString()), this);
    }

    /**
     * Simple broadcast: loop over all mentioned sessions to send the message
     * <p>
     * We are effectively always broadcasting: a player could be connected
     * to more than one device, and that could correspond to more than one connected
     * session. Allow topic filtering on the receiving side (Mediator and browser)
     * to filter out and display messages.
     *
     * @param message Message to send
     */
    public void sendMessage(Message message) {
        for (WebSocketSession s : sessions.values()) {
            sendMessageToSession(s, message);
        }
    }

    /**
     * Send a {@link Message} to a WebSocketSession
     *
     * @param session WebSocketSession to send the message to
     * @param message Message to send
     * @return True on success, else false. Closes session on failure.
     */
    private boolean sendMessageToSession(WebSocketSession session, Message message) {
        try {
            session.sendMessage(new TextMessage(message.toString()));
            return true;
        } catch (IOException e) {
            logger.info("Exception occurred while sending message: " + e.getLocalizedMessage());
            tryToClose(session, new CloseStatus(CloseReason.CloseCodes.UNEXPECTED_CONDITION.getCode(), trimReason(e.getLocalizedMessage())));
            return false;
        }
    }

    /**
     * @param reason String to trim
     * @return A String no longer than 123 characters (limit of value length for {@code CloseReason})
     */
    private String trimReason(String reason) {
        return reason.length() > 123 ? reason.substring(0, 123) : reason;
    }

    /**
     * Close a WebSocket session with a CloseStatus
     *
     * @param session WebSocketSession to close
     * @param status  {@link CloseStatus} of the WebSocketSession
     */
    private void tryToClose(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException e) {
            tryToClose(session);
        }
    }

    /**
     * Close a {@code Closeable} (usually once an error has already occurred).
     *
     * @param c Closable to close
     */
    private void tryToClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }
}

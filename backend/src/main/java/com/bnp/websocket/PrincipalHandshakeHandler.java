package com.bnp.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/** Turns the email stashed in handshake attributes by {@link JwtHandshakeInterceptor}
 *  into the STOMP session Principal, so convertAndSendToUser(email, ...) can target it. */
public class PrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                       Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        return () -> email;
    }
}

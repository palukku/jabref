package org.jabref.logic.remote.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javafx.util.Pair;

import org.jabref.logic.remote.Protocol;
import org.jabref.logic.remote.RemoteMessage;
import org.jabref.logic.remote.RemotePreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteListenerServer implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteListenerServer.class);

    private static final int BACKLOG = 1;

    private static final int TIMEOUT = 1000;

    private final RemoteMessageHandler messageHandler;
    private final ServerSocket serverSocket;

    public RemoteListenerServer(RemoteMessageHandler messageHandler, int port) throws IOException {
        this.serverSocket = new ServerSocket(port, BACKLOG, RemotePreferences.getIpAddress());
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                try (Socket socket = serverSocket.accept()) {
                    socket.setSoTimeout(TIMEOUT);
                    try (Protocol protocol = new Protocol(socket)) {
                        Pair<RemoteMessage, Object> input = protocol.receiveMessage();
                        handleMessage(protocol, input.getKey(), input.getValue());
                    }
                } catch (SocketException ex) {
                    return;
                } catch (IOException e) {
                    LOGGER.warn("RemoteListenerServer crashed", e);
                }
            }
        } finally {
            closeServerSocket();
        }
    }

    private void handleMessage(Protocol protocol, RemoteMessage type, Object argument) throws IOException {
        switch (type) {
            case PING:
                protocol.sendMessage(RemoteMessage.PONG, Protocol.IDENTIFIER);
                break;
            case SEND_COMMAND_LINE_ARGUMENTS:
                if (argument instanceof String[] strings) {
                    messageHandler.handleCommandLineArguments(strings);
                    protocol.sendMessage(RemoteMessage.OK);
                } else {
                    throw new IOException("Argument for 'SEND_COMMAND_LINE_ARGUMENTS' is not of type String[]. Got " + argument);
                }
                break;
            case FOCUS:
                messageHandler.handleFocus();
                protocol.sendMessage(RemoteMessage.OK);
                break;
            default:
                throw new IOException("Unhandled message to server " + type);
        }
    }

    public void closeServerSocket() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.warn("Unable to close server socket", e);
        }
    }
}

package org.jabref.logic.lsp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;

public class LSPLauncher {

    public LSPLauncher() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("BibtexLSPServer listening on port 12345...");

            ExecutorService threadPool = Executors.newCachedThreadPool();

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected!");

                threadPool.submit(() -> {
                    try (InputStream in = socket.getInputStream();
                         OutputStream out = socket.getOutputStream()) {

                        LSPServer server = new LSPServer();

                        Launcher<LanguageClient> launcher = Launcher.createLauncher(
                                server,
                                LanguageClient.class,
                                in,
                                out,
                                Executors.newCachedThreadPool(),
                                Function.identity()
                        );

                        server.connect(launcher.getRemoteProxy());

                        launcher.startListening().get(); // Wartet bis Client trennt
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException ignored) {}
                        System.out.println("Client disconnected.");
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

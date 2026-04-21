package server;

import com.sun.net.httpserver.HttpServer;
import config.AppConfig;

import java.io.IOException;
import java.net.InetSocketAddress;

public class AppServer {

    public static void start() {
        try {
            int port = AppConfig.getServerPort();
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            Router.configure(server);

            server.setExecutor(null);
            server.start();

            System.out.println("[SERVER] Server running on http://localhost:" + port);
        } catch (IOException e) {
            System.err.println("[SERVER] Failed to start: " + e.getMessage());
        }
    }
}
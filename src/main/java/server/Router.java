package server;

import com.sun.net.httpserver.HttpServer;
import handlers.DepartmentHandler;

public class Router {
    public static void configure(HttpServer server) {
        server.createContext("/departments", new DepartmentHandler());
    }
}
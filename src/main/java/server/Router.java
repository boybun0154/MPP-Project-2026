package server;

import com.sun.net.httpserver.HttpServer;
import handlers.ClientHandler;
import handlers.DepartmentHandler;
import handlers.EmployeeHandler;
import handlers.ProjectHandler;

public class Router {
    public static void configure(HttpServer server) {
        server.createContext("/clients", new ClientHandler());
        server.createContext("/departments", new DepartmentHandler());
        server.createContext("/employees", new EmployeeHandler());
        server.createContext("/projects", new ProjectHandler());
    }
}
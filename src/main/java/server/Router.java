package server;

import com.sun.net.httpserver.HttpServer;
import handler.ClientHandler;
import handler.DepartmentHandler;
import handler.EmployeeHandler;
import handler.ProjectHandler;

public class Router {
    public static void configure(HttpServer server) {
        server.createContext("/clients", new ClientHandler());
        server.createContext("/departments", new DepartmentHandler());
        server.createContext("/employees", new EmployeeHandler());
        server.createContext("/projects", new ProjectHandler());
    }
}
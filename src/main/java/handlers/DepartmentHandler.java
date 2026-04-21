package handlers;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import controller.DepartmentController;

import java.io.IOException;

public class DepartmentHandler implements HttpHandler {
    private final DepartmentController controller = new DepartmentController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        switch (method) {
            case "GET":
//                controller.getAll(exchange);
                break;
            case "POST":
//                controller.create(exchange);
                break;
            default:
                exchange.sendResponseHeaders(405, -1);
        }
    }
}

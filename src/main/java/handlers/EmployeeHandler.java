package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.EmployeeController;

import java.io.IOException;
import utils.HttpUtils;

public class EmployeeHandler implements HttpHandler {
    private final EmployeeController controller = new EmployeeController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // POST /employees/{id}/transfer?departmentId=...
        if ("POST".equals(method) && path.matches("/employees/[^/]+/transfer")) {
            String id = path.split("/")[2];
            controller.transfer(exchange, id);
            return;
        }

        String id = HttpUtils.extractId(path, "/employees");

        switch (method) {
            case "GET":
                if (id == null) controller.getAll(exchange);
                else controller.getById(exchange, id);
                break;
            case "POST":
                controller.create(exchange);
                break;
            case "PUT":
                if (id == null) exchange.sendResponseHeaders(400, -1);
                else controller.update(exchange, id);
                break;
            case "DELETE":
                if (id == null) exchange.sendResponseHeaders(400, -1);
                else controller.delete(exchange, id);
                break;
            default:
                exchange.sendResponseHeaders(405, -1);
        }
    }
}

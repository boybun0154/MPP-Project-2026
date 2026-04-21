package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ProjectController;

import java.io.IOException;
import utils.HttpUtils;

public class ProjectHandler implements HttpHandler {
    private final ProjectController controller = new ProjectController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // GET /projects/{id}/hr-cost
        if ("GET".equals(method) && path.matches("/projects/[^/]+/hr-cost")) {
            String id = path.split("/")[2];
            controller.hrCost(exchange, id);
            return;
        }

        String id = HttpUtils.extractId(path, "/projects");

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

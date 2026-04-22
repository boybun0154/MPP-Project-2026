package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ProjectController;
import config.ServiceRegistry;
import model.Project;
import utils.HttpUtils;
import utils.Json;
import java.io.IOException;
import java.math.BigDecimal;

public class ProjectHandler implements HttpHandler {
    private final ProjectController controller = new ProjectController(ServiceRegistry.get().projects());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(method) && path.endsWith("/hr-cost")) {
                handleHrCost(exchange, path);
                return;
            }

            String idStr = HttpUtils.extractId(path, "/projects");

            switch (method) {
                case "GET" -> {
                    if (idStr == null) HttpUtils.safeSendJson(exchange, 200, Json.ofList(controller.getAll()));
                    else controller.getById(Integer.parseInt(idStr))
                            .ifPresentOrElse(p -> HttpUtils.safeSendJson(exchange, 200, Json.of(p)),
                                    () -> HttpUtils.safeSendError(exchange, 404, "Project not found"));
                }
                case "POST" -> {
                    Project p = Json.fromJson(HttpUtils.readBody(exchange), Project.class);
                    HttpUtils.safeSendJson(exchange, 201, Json.of(controller.create(p)));
                }
                case "PUT" -> {
                    if (idStr == null) { HttpUtils.safeSendError(exchange, 400, "ID Required"); return; }
                    Project p = Json.fromJson(HttpUtils.readBody(exchange), Project.class);
                    controller.update(Integer.parseInt(idStr), p)
                            .ifPresentOrElse(updated -> HttpUtils.safeSendJson(exchange, 200, Json.of(updated)),
                                    () -> HttpUtils.safeSendError(exchange, 404, "Project not found"));
                }
                case "DELETE" -> {
                    if (idStr == null) { HttpUtils.safeSendError(exchange, 400, "ID Required"); return; }
                    controller.delete(Integer.parseInt(idStr));
                    HttpUtils.safeSendJson(exchange, 200, "true");
                }
                default -> exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            HttpUtils.safeSendError(exchange, 500, e.getMessage());
        }
    }

    private void handleHrCost(HttpExchange exchange, String path) throws IOException {
        try {
            String[] parts = path.split("/");
            int projectId = Integer.parseInt(parts[2]);

            Double cost = controller.calculateHrCost(projectId);

            String response = String.format("{\"projectId\":%d, \"hrCost\":%s}",
                    projectId, cost);
            HttpUtils.safeSendJson(exchange, 200, response);

        } catch (Exception e) {
            HttpUtils.safeSendError(exchange, 400, "Invalid Project ID or calculation error");
        }
    }
}
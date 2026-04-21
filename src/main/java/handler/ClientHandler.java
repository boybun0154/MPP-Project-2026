package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ClientController;
import config.ServiceRegistry;
import model.Client;
import utils.HttpUtils;
import utils.Json;
import java.io.IOException;

public class ClientHandler implements HttpHandler {
    private final ClientController controller = new ClientController(ServiceRegistry.get().clients());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            // Task 3 Route: GET /clients/upcoming-deadlines?days=30
            if ("GET".equals(method) && path.endsWith("/upcoming-deadlines")) {
                int days = Integer.parseInt(HttpUtils.queryParams(exchange).getOrDefault("days", "30"));
                HttpUtils.safeSendJson(exchange, 200, Json.ofList(controller.findByDeadline(days)));
                return;
            }

            String idStr = HttpUtils.extractId(path, "/clients");

            switch (method) {
                case "GET" -> {
                    if (idStr == null) HttpUtils.safeSendJson(exchange, 200, Json.ofList(controller.getAll()));
                    else controller.getById(Integer.parseInt(idStr))
                            .ifPresentOrElse(c -> HttpUtils.safeSendJson(exchange, 200, Json.of(c)),
                                    () -> HttpUtils.safeSendError(exchange, 404, "Client not found"));
                }
                case "POST" -> {
                    Client c = Json.fromJson(HttpUtils.readBody(exchange), Client.class);
                    HttpUtils.safeSendJson(exchange, 201, Json.of(controller.create(c)));
                }
                case "PUT" -> {
                    if (idStr == null) { HttpUtils.safeSendError(exchange, 400, "ID Required"); return; }
                    Client c = Json.fromJson(HttpUtils.readBody(exchange), Client.class);
                    controller.update(Integer.parseInt(idStr), c)
                            .ifPresentOrElse(updated -> HttpUtils.safeSendJson(exchange, 200, Json.of(updated)),
                                    () -> HttpUtils.safeSendError(exchange, 404, "Client not found"));
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
}
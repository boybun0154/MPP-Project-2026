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
        String idStr = HttpUtils.extractId(path, "/clients");

        try {
            if ("GET".equals(method) && path.endsWith("/upcoming-deadlines")) {
                int days = Integer.parseInt(HttpUtils.queryParams(exchange).getOrDefault("days", "30"));
                HttpUtils.sendJson(exchange, 200, Json.ofList(controller.findByDeadline(days)));
                return;
            }

            switch (method) {
                case "GET" -> {
                    if (idStr == null) HttpUtils.sendJson(exchange, 200, Json.ofList(controller.getAll()));
                    else controller.getById(Integer.parseInt(idStr))
                            .ifPresentOrElse(c -> {
                                        try {
                                            HttpUtils.sendJson(exchange, 200, Json.of(c));
                                        } catch (Exception e) {
                                        }
                                    },
                                    () -> {
                                        try {
                                            HttpUtils.sendError(exchange, 404, "Not Found");
                                        } catch (Exception e) {
                                        }
                                    });
                }
                case "POST" -> {
                    Client c = Json.fromJson(HttpUtils.readBody(exchange), Client.class);
                    HttpUtils.sendJson(exchange, 201, Json.of(controller.create(c)));
                }
                case "PUT" -> {
                    if (idStr == null) {
                        HttpUtils.sendError(exchange, 400, "ID Required");
                        return;
                    }

                    int id = Integer.parseInt(idStr);
                    Client client = Json.fromJson(HttpUtils.readBody(exchange), Client.class);

                    controller.update(id, client).ifPresentOrElse(
                            updated -> HttpUtils.safeSendJson(exchange, 200, Json.of(updated)),
                            () -> HttpUtils.safeSendError(exchange, 404, "Not Found")
                    );
                }
                case "DELETE" -> {
                    controller.delete(Integer.parseInt(idStr));
                    HttpUtils.sendJson(exchange, 200, "{\"deleted\":true}");
                }
                default -> exchange.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            HttpUtils.sendError(exchange, 500, e.getMessage());
        }
    }
}
package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ClientController;

import java.io.IOException;
import utils.HttpUtils;

public class ClientHandler implements HttpHandler {
    private final ClientController controller = new ClientController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String id = HttpUtils.extractId(exchange.getRequestURI().getPath(), "/clients");

        switch (method) {
            case "GET":
                if (id == null) {
                    controller.getAll(exchange);
                } else {
                    controller.getById(exchange, id);
                }
                break;
            case "POST":
                controller.create(exchange);
                break;
            case "PUT":
                if (id == null) {
                    exchange.sendResponseHeaders(400, -1);
                } else {
                    controller.update(exchange, id);
                }
                break;
            case "DELETE":
                if (id == null) {
                    exchange.sendResponseHeaders(400, -1);
                } else {
                    controller.delete(exchange, id);
                }
                break;
            default:
                exchange.sendResponseHeaders(405, -1);
        }
    }
}

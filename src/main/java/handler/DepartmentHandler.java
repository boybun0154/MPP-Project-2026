package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.DepartmentController;
import config.ServiceRegistry;
import model.Department;
import utils.HttpUtils;
import utils.Json;

public class DepartmentHandler implements HttpHandler {
    private final DepartmentController controller = new DepartmentController(
            ServiceRegistry.get().departments(),
            ServiceRegistry.get().projects()
    );

    @Override
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(method) && path.matches("/departments/[^/]+/projects")) {
                handleListProjects(exchange, path);
                return;
            }

            String idStr = HttpUtils.extractId(path, "/departments");

            switch (method) {
                case "GET" -> {
                    if (idStr == null) HttpUtils.safeSendJson(exchange, 200, Json.ofList(controller.getAll()));
                    else controller.getById(Integer.parseInt(idStr))
                            .ifPresentOrElse(d -> HttpUtils.safeSendJson(exchange, 200, Json.of(d)),
                                    () -> HttpUtils.safeSendError(exchange, 404, "Department not found"));
                }
                case "POST" -> {
                    Department d = Json.fromJson(HttpUtils.readBody(exchange), Department.class);
                    HttpUtils.safeSendJson(exchange, 201, Json.of(controller.create(d)));
                }
                case "PUT" -> {
                    if (idStr == null) { HttpUtils.safeSendError(exchange, 400, "ID Required"); return; }
                    Department d = Json.fromJson(HttpUtils.readBody(exchange), Department.class);
                    controller.update(Integer.parseInt(idStr), d)
                            .ifPresentOrElse(updated -> HttpUtils.safeSendJson(exchange, 200, Json.of(updated)),
                                    () -> HttpUtils.safeSendError(exchange, 404, "Department not found"));
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

    private void handleListProjects(HttpExchange exchange, String path) {
        try {
            String[] parts = path.split("/");

            int departmentId = Integer.parseInt(parts[2]);
            String sortBy = HttpUtils.queryParams(exchange).getOrDefault("sortBy", "id");

            var projects = controller.getProjectsByDepartment(departmentId, sortBy);
            HttpUtils.safeSendJson(exchange, 200, Json.ofList(projects));
        } catch (Exception e) {
            HttpUtils.safeSendError(exchange, 400, "Invalid Department ID");
        }
    }
}
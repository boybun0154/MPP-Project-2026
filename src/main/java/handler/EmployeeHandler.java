package handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.EmployeeController;
import config.ServiceRegistry;
import model.Employee;
import utils.HttpUtils;
import utils.Json;
import java.io.IOException;
import java.util.Map;

public class EmployeeHandler implements HttpHandler {
    private final EmployeeController controller = new EmployeeController(ServiceRegistry.get().employees());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("POST".equals(method) && path.endsWith("/transfer")) {
                handleTransfer(exchange, path);
                return;
            }

            String idStr = HttpUtils.extractId(path, "/employees");

            switch (method) {
                case "GET" -> {
                    if (idStr == null) HttpUtils.safeSendJson(exchange, 200, Json.ofList(controller.getAll()));
                    else controller.getById(Integer.parseInt(idStr))
                            .ifPresentOrElse(e -> HttpUtils.safeSendJson(exchange, 200, Json.of(e)),
                                    () -> HttpUtils.safeSendError(exchange, 404, "Employee not found"));
                }
                case "POST" -> {
                    Employee e = Json.fromJson(HttpUtils.readBody(exchange), Employee.class);
                    HttpUtils.safeSendJson(exchange, 201, Json.of(controller.create(e)));
                }
                case "PUT" -> {
                    if (idStr == null) { HttpUtils.safeSendError(exchange, 400, "ID Required"); return; }
                    Employee e = Json.fromJson(HttpUtils.readBody(exchange), Employee.class);
                    controller.update(Integer.parseInt(idStr), e)
                            .ifPresentOrElse(updated -> HttpUtils.safeSendJson(exchange, 200, Json.of(updated)),
                                    () -> HttpUtils.safeSendError(exchange, 404, "Employee not found"));
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

    private void handleTransfer(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        int empId = Integer.parseInt(parts[2]);

        Map<String, String> body = Json.parse(HttpUtils.readBody(exchange));
        String deptIdStr = body.getOrDefault("departmentId", HttpUtils.queryParams(exchange).get("departmentId"));

        if (deptIdStr == null) {
            HttpUtils.safeSendError(exchange, 400, "departmentId is required");
            return;
        }

        int newDeptId = Integer.parseInt(deptIdStr);
        controller.transfer(empId, newDeptId).ifPresentOrElse(
                updated -> HttpUtils.safeSendJson(exchange, 200, Json.of(updated)),
                () -> HttpUtils.safeSendError(exchange, 404, "Employee or Department not found")
        );
    }
}
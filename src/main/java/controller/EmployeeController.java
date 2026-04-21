package controller;

import com.sun.net.httpserver.HttpExchange;
import config.ServiceRegistry;
import model.Department;
import model.Employee;
import service.interfaces.IDepartmentService;
import service.interfaces.IEmployeeService;
import utils.HttpUtils;
import utils.Json;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmployeeController {
    private final IEmployeeService service;
    private final IDepartmentService departmentService;

    public EmployeeController() {
        this(ServiceRegistry.get().employees(), ServiceRegistry.get().departments());
    }

    public EmployeeController(IEmployeeService service, IDepartmentService departmentService) {
        this.service = service;
        this.departmentService = departmentService;
    }

    public void create(HttpExchange exchange) throws IOException {
        Map<String, String> body = Json.parse(HttpUtils.readBody(exchange));
        Employee e = new Employee();
        e.setFullName(body.get("fullName"));
        e.setTitle(body.get("title"));
        if (body.get("hireDate") != null) e.setHireDate(LocalDate.parse(body.get("hireDate")));
        if (body.get("salary") != null) e.setSalary(new BigDecimal(body.get("salary")));
        if (body.get("departmentId") != null) {
            Optional<Department> dep = departmentService.getById(Long.parseLong(body.get("departmentId")));
            dep.ifPresent(d -> { e.setDepartment(d); d.getEmployees().add(e); });
        }
        service.create(e);
        HttpUtils.sendJson(exchange, 201, Json.of(e));
    }

    public void getAll(HttpExchange exchange) throws IOException {
        List<String> items = service.getAll().stream().map(Json::of).collect(Collectors.toList());
        HttpUtils.sendJson(exchange, 200, Json.array(items));
    }

    public void getById(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        Optional<Employee> found = service.getById(parsed);
        if (found.isEmpty()) {
            HttpUtils.sendError(exchange, 404, "Employee not found: " + id);
            return;
        }
        HttpUtils.sendJson(exchange, 200, Json.of(found.get()));
    }

    public void update(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        Optional<Employee> existing = service.getById(parsed);
        if (existing.isEmpty()) {
            HttpUtils.sendError(exchange, 404, "Employee not found: " + id);
            return;
        }
        Map<String, String> body = Json.parse(HttpUtils.readBody(exchange));
        Employee e = existing.get();
        if (body.containsKey("fullName")) e.setFullName(body.get("fullName"));
        if (body.containsKey("title")) e.setTitle(body.get("title"));
        if (body.containsKey("hireDate") && body.get("hireDate") != null)
            e.setHireDate(LocalDate.parse(body.get("hireDate")));
        if (body.containsKey("salary") && body.get("salary") != null)
            e.setSalary(new BigDecimal(body.get("salary")));
        service.update(parsed, e);
        HttpUtils.sendJson(exchange, 200, Json.of(e));
    }

    public void delete(HttpExchange exchange, String id) throws IOException {
        Long parsed = parseId(exchange, id);
        if (parsed == null) return;
        service.delete(parsed);
        HttpUtils.sendJson(exchange, 200, "{\"id\":" + parsed + ",\"deleted\":true}");
    }

    public void transfer(HttpExchange exchange, String id) throws IOException {
        Map<String, String> q = HttpUtils.queryParams(exchange);
        String dept = q.get("departmentId");
        if (dept == null) {
            Map<String, String> body = Json.parse(HttpUtils.readBody(exchange));
            dept = body.get("departmentId");
        }
        if (dept == null) {
            HttpUtils.sendError(exchange, 400, "Missing departmentId");
            return;
        }
        try {
            int empId = Integer.parseInt(id);
            int newDeptId = Integer.parseInt(dept);
            service.transferEmployeeToDepartment(empId, newDeptId);
            Optional<Employee> updated = service.getById((long) empId);
            HttpUtils.sendJson(exchange, 200, updated.map(Json::of)
                    .orElse("{\"id\":" + id + ",\"transferred\":true}"));
        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Invalid id/departmentId");
        } catch (IllegalArgumentException e) {
            HttpUtils.sendError(exchange, 404, e.getMessage());
        } catch (IllegalStateException e) {
            HttpUtils.sendError(exchange, 409, e.getMessage());
        }
    }

    private Long parseId(HttpExchange exchange, String id) throws IOException {
        try { return Long.parseLong(id); }
        catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "Invalid id: " + id);
            return null;
        }
    }
}

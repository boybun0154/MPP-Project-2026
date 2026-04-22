package repository.jdbc;

import model.Department;
import model.Employee;
import model.Project;
import model.ProjectStatus;
import repository.interfaces.IEmployeeRepository;
import repository.jdbc.core.DbClient;
import repository.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EmployeeRepository implements IEmployeeRepository {

    // Task 4: Employee Transfer Transaction
    public void updateDepartment(Connection conn, int employeeId, int newDepartmentId) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE employees SET department_id = ? WHERE id = ?")) {

            stmt.setInt(1, newDepartmentId);
            stmt.setInt(2, employeeId);

            int updated = stmt.executeUpdate();

            if (updated == 0) {
                throw new IllegalArgumentException("Employee not found: " + employeeId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating employee department: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Employee> findById(Integer id) {
        Optional<Employee> employee = DbClient.fetchOne(
                "SELECT * FROM employees WHERE id = ?",
                EMPLOYEE_MAPPER,
                id
        );

        employee.ifPresent(this::hydrate);

        return employee;
    }

    @Override
    public List<Employee> findAll() {
        List<Employee> employees = DbClient.query("SELECT * FROM employees", EMPLOYEE_MAPPER);

        employees.forEach(this::hydrate);

        return employees;
    }

    @Override
    public void save(Employee entity) {
        if (entity.getId() == null || entity.getId() == 0) {
            int generatedId = DbClient.insert(
                    "INSERT INTO employees (full_name, title, hire_date, salary, department_id) VALUES (?, ?, ?, ?, ?)",
                    entity.getFullName(), entity.getTitle(), entity.getHireDate(),
                    entity.getSalary(), entity.getDepartment().getId()
            );
            entity.setId(generatedId);
        } else {
            DbClient.execute(
                    "UPDATE employees SET full_name = ?, title = ?, hire_date = ?, salary = ?, department_id = ? WHERE id = ?",
                    entity.getFullName(), entity.getTitle(), entity.getHireDate(),
                    entity.getSalary(), entity.getDepartment().getId(), entity.getId()
            );
        }

        hydrate(entity);
    }

    @Override
    public void delete(Integer id) {
        DbClient.execute("DELETE FROM employees WHERE id = ?", id);
    }

    private void hydrate(Employee employee) {
        if (employee.getDepartment() != null) {
            DbClient.fetchOne("SELECT * FROM departments WHERE id = ?", rs -> {
                Department department = employee.getDepartment();
                department.setName(rs.getString("name"));
                department.setLocation(rs.getString("location"));
                department.setAnnualBudget(rs.getDouble("annual_budget"));

                return department;
            }, employee.getDepartment().getId());
        }

        String sqlProjects = "SELECT p.*, ep.allocation_percentage " +
                "FROM projects p " +
                "JOIN employee_projects ep ON p.id = ep.project_id " +
                "WHERE ep.employee_id = ?";

        employee.getProjectAllocationPercentages().clear();

        DbClient.query(sqlProjects, rs -> {
            Project project = new Project();
            project.setId(rs.getInt("id"));
            project.setName(rs.getString("name"));
            project.setStatus(rs.getString("status") != null ?
                    ProjectStatus.valueOf(rs.getString("status").toUpperCase()) : null);
            project.setBudget(rs.getDouble("budget"));
            project.setStartDate(rs.getObject("start_date", LocalDate.class));
            project.setEndDate(rs.getObject("end_date", LocalDate.class));

            int percentage = rs.getInt("allocation_percentage");

            employee.getProjectAllocationPercentages().put(project, percentage);

            return project;
        }, employee.getId());
    }

    private static final RowMapper<Employee> EMPLOYEE_MAPPER = rs -> {
        Employee employee = new Employee();

        employee.setId(rs.getInt("id"));
        employee.setFullName(rs.getString("full_name"));
        employee.setTitle(rs.getString("title"));
        employee.setHireDate(rs.getObject("hire_date", LocalDate.class));
        employee.setSalary(rs.getDouble("salary"));

        Department department = new Department();

        department.setId(rs.getInt("department_id"));
        employee.setDepartment(department);

        return employee;
    };
}
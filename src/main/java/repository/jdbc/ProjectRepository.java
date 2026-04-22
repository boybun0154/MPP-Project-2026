package repository.jdbc;

import model.*;
import repository.interfaces.IProjectRepository;
import repository.jdbc.core.DbClient;
import repository.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProjectRepository implements IProjectRepository {

    @Override
    public Optional<Project> findById(Integer id) {
        Optional<Project> project = DbClient.fetchOne(
                "SELECT * FROM projects WHERE id = ?",
                PROJECT_MAPPER, id
        );

        project.ifPresent(this::hydrate);

        return project;
    }

    @Override
    public List<Project> findAll() {
        List<Project> projects = DbClient.query("SELECT * FROM projects", PROJECT_MAPPER);

        projects.forEach(this::hydrate);

        return projects;
    }

    @Override
    public void save(Project entity) {
        if (entity.getId() == null || entity.getId() == 0) {
            int generatedId = DbClient.insert(
                    "INSERT INTO projects " +
                            "(name, description, start_date, end_date, budget, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    entity.getName(), entity.getDescription(), entity.getStartDate(),
                    entity.getEndDate(), entity.getBudget(), entity.getStatus()
            );
            entity.setId(generatedId);
        } else {
            DbClient.execute(
                    "UPDATE projects SET name = ?, description = ?, start_date = ?, end_date = ?, budget = ?, status = ? WHERE id = ?",
                    entity.getName(), entity.getDescription(), entity.getStartDate(),
                    entity.getEndDate(), entity.getBudget(), entity.getStatus(), entity.getId()
            );
        }

        hydrate(entity);
    }

    @Override
    public void delete(Integer id) {
        DbClient.execute("DELETE FROM projects WHERE id = ?", id);
    }

    private void hydrate(Project project) {
        String sqlDepartments =
                "SELECT d.* " +
                        "FROM departments d " +
                        "JOIN department_projects dp ON d.id = dp.department_id " +
                        "WHERE dp.project_id = ?";

        List<Department> departments = DbClient.query(sqlDepartments, rs -> {
            Department department = new Department();

            department.setId(rs.getInt("id"));
            department.setName(rs.getString("name"));

            return department;
        }, project.getId());

        project.setDepartments(new HashSet<>(departments));

        String sqlClients =
                "SELECT c.* " +
                        "FROM clients c " +
                        "JOIN client_projects cp ON c.id = cp.client_id " +
                        "WHERE cp.project_id = ?";

        List<Client> clients = DbClient.query(sqlClients, rs -> {
            Client client = new Client();

            client.setId(rs.getInt("id"));
            client.setName(rs.getString("name"));

            return client;
        }, project.getId());

        project.setClients(new HashSet<>(clients));

        String sqlEmployees =
                "SELECT e.*, " +
                        "ep.allocation_percentage " +
                        "FROM employees e " +
                        "JOIN employee_projects ep ON e.id = ep.employee_id " +
                        "WHERE ep.project_id = ?";

        Map<Employee, Integer> allocations = new HashMap<>();

        DbClient.query(sqlEmployees, rs -> {
            Employee employee = new Employee();

            employee.setId(rs.getInt("id"));
            employee.setFullName(rs.getString("full_name"));
            employee.setSalary(rs.getDouble("salary"));

            allocations.put(employee, rs.getInt("allocation_percentage"));

            return null;
        }, project.getId());
        project.setEmployeeAllocations(allocations);
    }

    private static final RowMapper<Project> PROJECT_MAPPER = rs -> {
        Project project = new Project();

        project.setId(rs.getInt("id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        project.setStartDate(rs.getObject("start_date", LocalDate.class));
        project.setEndDate(rs.getObject("end_date", LocalDate.class));
        project.setBudget(rs.getDouble("budget"));
        project.setStatus(rs.getString("status") != null ?
                ProjectStatus.valueOf(rs.getString("status").toUpperCase()) : null);

        return project;
    };
}
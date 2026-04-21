package repository.jdbc;

import model.Department;
import model.Employee;
import model.Project;
import model.Client;
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
        Optional<Project> projOpt = DbClient.fetchOne(
                "SELECT * FROM projects WHERE id = ?",
                PROJECT_MAPPER,
                id
        );
        projOpt.ifPresent(this::hydrate);
        return projOpt;
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
            DbClient.execute(
                    "INSERT INTO projects (name, description, start_date, end_date, budget, status) VALUES (?, ?, ?, ?, ?, ?)",
                    entity.getName(), entity.getDescription(), entity.getStartDate(),
                    entity.getEndDate(), entity.getBudget(), entity.getStatus()
            );
        } else {
            DbClient.execute(
                    "UPDATE projects SET name = ?, description = ?, start_date = ?, end_date = ?, budget = ?, status = ? WHERE id = ?",
                    entity.getName(), entity.getDescription(), entity.getStartDate(),
                    entity.getEndDate(), entity.getBudget(), entity.getStatus(), entity.getId()
            );
        }
    }

    @Override
    public void delete(Integer id) {
        DbClient.execute("DELETE FROM projects WHERE id = ?", id);
    }

    private static final RowMapper<Project> PROJECT_MAPPER = rs -> {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        project.setStartDate(rs.getObject("start_date", LocalDate.class));
        project.setEndDate(rs.getObject("end_date", LocalDate.class));
        project.setBudget(rs.getDouble("budget"));
        project.setStatus(rs.getString("status"));
        return project;
    };

    private void hydrate(Project project) {
        String sqlDepts = "SELECT d.* FROM departments d " +
                "JOIN department_projects dp ON d.id = dp.department_id " +
                "WHERE dp.project_id = ?";
        List<Department> depts = DbClient.query(sqlDepts, rs -> {
            Department d = new Department();
            d.setId(rs.getInt("id"));
            d.setName(rs.getString("name"));
            return d;
        }, project.getId());
        project.setDepartments(new HashSet<>(depts));

        String sqlClients = "SELECT c.* FROM clients c " +
                "JOIN client_projects cp ON c.id = cp.client_id " +
                "WHERE cp.project_id = ?";
        List<Client> clients = DbClient.query(sqlClients, rs -> {
            Client c = new Client();
            c.setId(rs.getInt("id"));
            c.setName(rs.getString("name"));
            return c;
        }, project.getId());
        project.setClients(new HashSet<>(clients));

        String sqlEmps = "SELECT e.*, ep.allocation_percentage FROM employees e " +
                "JOIN employee_projects ep ON e.id = ep.employee_id " +
                "WHERE ep.project_id = ?";

        Map<Employee, Integer> allocations = new HashMap<>();
        DbClient.query(sqlEmps, rs -> {
            Employee e = new Employee();
            e.setId(rs.getInt("id"));
            e.setFullName(rs.getString("full_name"));
            e.setSalary(rs.getDouble("salary"));

            int percentage = rs.getInt("allocation_percentage");
            allocations.put(e, percentage);
            return null;
        }, project.getId());

        project.setEmployeeAllocations(allocations);
    }
}
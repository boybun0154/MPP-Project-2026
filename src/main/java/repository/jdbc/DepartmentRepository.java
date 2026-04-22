package repository.jdbc;

import model.Department;
import model.Employee;
import model.Project;
import model.ProjectStatus;
import repository.interfaces.IDepartmentRepository;
import repository.jdbc.core.DbClient;
import repository.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DepartmentRepository implements IDepartmentRepository {

    @Override
    public Optional<Department> findById(Integer id) {
        Optional<Department> department = DbClient.fetchOne(
                "SELECT * FROM departments WHERE id = ?",
                DEPARTMENT_MAPPER,
                id
        );

        department.ifPresent(this::hydrate);

        return department;
    }

    @Override
    public List<Department> findAll() {
        List<Department> departments = DbClient.query("SELECT * FROM departments", DEPARTMENT_MAPPER);

        departments.forEach(this::hydrate);

        return departments;
    }

    @Override
    public void save(Department entity) {
        if (entity.getId() == null || entity.getId() == 0) {
            int generatedId = DbClient.insert(
                    "INSERT INTO departments (name, location, annual_budget) VALUES (?, ?, ?)",
                    entity.getName(), entity.getLocation(), entity.getAnnualBudget()
            );
            entity.setId(generatedId);
        } else {
            DbClient.execute(
                    "UPDATE departments SET name = ?, location = ?, annual_budget = ? WHERE id = ?",
                    entity.getName(), entity.getLocation(), entity.getAnnualBudget(), entity.getId()
            );
        }

        hydrate(entity);
    }

    @Override
    public void delete(Integer id) {
        DbClient.execute("DELETE FROM departments WHERE id = ?", id);
    }

    private static final RowMapper<Department> DEPARTMENT_MAPPER = rs -> {
        Department department = new Department();
        department.setId(rs.getInt("id"));
        department.setName(rs.getString("name"));
        department.setLocation(rs.getString("location"));
        department.setAnnualBudget(rs.getDouble("annual_budget"));
        return department;
    };


    private void hydrate(Department department) {
        String sqlEmployees = "SELECT * FROM employees WHERE department_id = ?";

        List<Employee> employees = DbClient.query(sqlEmployees, rs -> {
            Employee employee = new Employee();
            employee.setId(rs.getInt("id"));
            employee.setFullName(rs.getString("full_name"));
            employee.setHireDate(rs.getObject("hire_date", LocalDate.class));
            employee.setSalary(rs.getDouble("salary"));
            return employee;
        }, department.getId());

        department.setEmployees(employees);

        String sqlProjects = "SELECT p.* FROM projects p " +
                "JOIN department_projects dp ON p.id = dp.project_id " +
                "WHERE dp.department_id = ?";

        List<Project> projects = DbClient.query(sqlProjects, rs -> {
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
        }, department.getId());

        department.setProjects(projects);
    }
}
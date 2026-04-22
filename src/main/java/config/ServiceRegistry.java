package config;

import repository.interfaces.*;
import repository.jdbc.ClientRepository;
import repository.jdbc.DepartmentRepository;
import repository.jdbc.EmployeeRepository;
import repository.jdbc.ProjectRepository;
import service.ClientService;
import service.DepartmentService;
import service.EmployeeService;
import service.ProjectService;
import service.interfaces.*;

/**
 * A central registry for managing services and repositories.
 * It implements the **Singleton** pattern, ensuring only one instance exists
 * so that all controllers share a single, consistent state across the application.
 */
public final class ServiceRegistry {
    private static final ServiceRegistry INSTANCE = new ServiceRegistry();

    public static ServiceRegistry get() {
        return INSTANCE;
    }

    private final IClientRepository clientRepo = new ClientRepository();
    private final IDepartmentRepository departmentRepo = new DepartmentRepository();
    private final IEmployeeRepository employeeRepo = new EmployeeRepository();
    private final IProjectRepository projectRepo = new ProjectRepository();

    private final IClientService clientService = new ClientService(clientRepo);
    private final IDepartmentService departmentService = new DepartmentService(departmentRepo, employeeRepo);
    private final IEmployeeService employeeService = new EmployeeService(employeeRepo, departmentRepo);
    private final IProjectService projectService = new ProjectService(projectRepo);

    public IClientService clients() { return clientService; }
    public IDepartmentService departments() { return departmentService; }
    public IEmployeeService employees() { return employeeService; }
    public IProjectService projects() { return projectService; }

    // Using Memory instead of Database
//    private final IRepository<Client> clientRepo =
//            new InMemoryRepository<>(Client::getId, Client::setId);
//    private final IRepository<Department> departmentRepo =
//            new InMemoryRepository<>(Department::getId, Department::setId);
//    private final IRepository<Employee> employeeRepo =
//            new InMemoryRepository<>(Employee::getId, Employee::setId);
//    private final IRepository<Project> projectRepo =
//            new InMemoryRepository<>(Project::getId, Project::setId);
}

package config;

import model.Client;
import model.Department;
import model.Employee;
import model.Project;
import repository.interfaces.IRepository;
import service.ClientService;
import service.DepartmentService;
import service.EmployeeService;
import service.ProjectService;
import service.interfaces.IClientService;
import service.interfaces.IDepartmentService;
import service.interfaces.IEmployeeService;
import service.interfaces.IProjectService;
import utils.InMemoryRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Shared wiring so every controller talks to the same repositories and
 * services. Uses in-memory repositories today; swap to JDBC once the
 * repository layer is ready.
 */
public final class ServiceRegistry {
    private static final ServiceRegistry INSTANCE = new ServiceRegistry();

    public static ServiceRegistry get() {
        return INSTANCE;
    }

    private final IRepository<Client> clientRepo =
            new InMemoryRepository<>(Client::getId, Client::setId);
    private final IRepository<Department> departmentRepo =
            new InMemoryRepository<>(Department::getId, Department::setId);
    private final IRepository<Employee> employeeRepo =
            new InMemoryRepository<>(Employee::getId, Employee::setId);
    private final IRepository<Project> projectRepo =
            new InMemoryRepository<>(Project::getId, Project::setId);

    private final IClientService clientService = new ClientService(clientRepo);
    private final IDepartmentService departmentService = new DepartmentService(departmentRepo, employeeRepo);
    private final IEmployeeService employeeService = new EmployeeService(employeeRepo, departmentRepo);
    private final IProjectService projectService = new ProjectService(projectRepo);

    private ServiceRegistry() {
        seed();
    }

    public IClientService clients() { return clientService; }
    public IDepartmentService departments() { return departmentService; }
    public IEmployeeService employees() { return employeeService; }
    public IProjectService projects() { return projectService; }

    /** Minimal demo data so the business-logic endpoints have something to return. */
    private void seed() {
        Department eng = new Department();
        eng.setName("Engineering");
        eng.setLocation("HQ");
        eng.setAnnualBudget(new BigDecimal("1000000"));
        departmentService.create(eng);

        Department ops = new Department();
        ops.setName("Operations");
        ops.setLocation("Remote");
        ops.setAnnualBudget(new BigDecimal("500000"));
        departmentService.create(ops);

        Employee alice = new Employee();
        alice.setFullName("Alice Johnson");
        alice.setTitle("Senior Engineer");
        alice.setHireDate(LocalDate.of(2021, 3, 1));
        alice.setSalary(new BigDecimal("120000"));
        alice.setDepartment(eng);
        eng.getEmployees().add(alice);
        employeeService.create(alice);

        Employee bob = new Employee();
        bob.setFullName("Bob Smith");
        bob.setTitle("Analyst");
        bob.setHireDate(LocalDate.of(2022, 6, 15));
        bob.setSalary(new BigDecimal("80000"));
        bob.setDepartment(ops);
        ops.getEmployees().add(bob);
        employeeService.create(bob);

        Client acme = new Client();
        acme.setName("Acme Corp");
        acme.setIndustry("Manufacturing");
        acme.setPrimaryContactName("Jane Doe");
        acme.setPrimaryContactPhone("555-0100");
        acme.setPrimaryContactEmail("jane@acme.example");
        clientService.create(acme);

        Project apollo = new Project();
        apollo.setName("Apollo");
        apollo.setDescription("Platform migration");
        apollo.setStartDate(LocalDate.now().minusMonths(2));
        apollo.setEndDate(LocalDate.now().plusDays(20));
        apollo.setBudget(new BigDecimal("250000"));
        apollo.setStatus("Active");
        apollo.getDepartments().add(eng);
        apollo.getEmployeeAllocations().put(alice, 60);
        apollo.getEmployeeAllocations().put(bob, 40);
        apollo.getClients().add(acme);
        acme.getProjects().add(apollo);
        projectService.create(apollo);

        Project zephyr = new Project();
        zephyr.setName("Zephyr");
        zephyr.setDescription("Reporting overhaul");
        zephyr.setStartDate(LocalDate.now().minusMonths(1));
        zephyr.setEndDate(LocalDate.now().plusMonths(3));
        zephyr.setBudget(new BigDecimal("120000"));
        zephyr.setStatus("Active");
        zephyr.getDepartments().add(eng);
        zephyr.getEmployeeAllocations().put(alice, 20);
        projectService.create(zephyr);
    }
}

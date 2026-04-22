import config.ServiceRegistry;
import model.Client;
import model.Department;
import model.Employee;
import model.Project;
import model.ProjectStatus;
import repository.jdbc.core.DbClient;
import service.interfaces.IClientService;
import service.interfaces.IDepartmentService;
import service.interfaces.IEmployeeService;
import service.interfaces.IProjectService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lightweight, library-free smoke test. Run the main method from the IDE or with
 *   mvn -q test-compile exec:java -Dexec.mainClass=SimpleTest -Dexec.classpathScope=test
 *
 * Two flavors of tests:
 *   1. CRUD smoke per entity (Client, Department, Employee, Project).
 *   2. The four business-logic tasks from the assignment:
 *        Task 1: calculateProjectHRCost
 *        Task 2: getProjectsByDepartment
 *        Task 3: findClientsByUpcomingProjectDeadline
 *        Task 4: transferEmployeeToDepartment
 *
 * All rows created during the run are removed in the finally block so
 * database.db is left clean even when an assertion fails midway. Junction rows
 * are cleaned by ON DELETE CASCADE on their foreign keys.
 */
public class SimpleTest {

    private static int passed = 0;
    private static int failed = 0;

    // Runs in reverse order (LIFO) so children are deleted before parents.
    private static final List<Runnable> cleanups = new ArrayList<>();

    // Unique suffix so re-runs don't collide on any UNIQUE columns.
    private static final String TAG = "SimpleTest-" + System.currentTimeMillis();

    public static void main(String[] args) {
        ServiceRegistry reg = ServiceRegistry.get();
        try {
            // ---- CRUD smoke ----
            run("Client CRUD",
                    () -> testClient(reg.clients()));
            run("Department CRUD",
                    () -> testDepartment(reg.departments()));
            run("Employee CRUD",
                    () -> testEmployee(reg.employees(), reg.departments()));
            run("Project CRUD",
                    () -> testProject(reg.projects()));

            // ---- Business tasks ----
            run("Task 1: calculateProjectHRCost",
                    () -> testCalculateProjectHRCost(reg.projects(), reg.employees(), reg.departments()));
            run("Task 2: getProjectsByDepartment",
                    () -> testGetProjectsByDepartment(reg.projects(), reg.departments()));
            run("Task 3: findClientsByUpcomingProjectDeadline",
                    () -> testFindClientsByUpcomingProjectDeadline(reg.clients(), reg.projects()));
            run("Task 4: transferEmployeeToDepartment",
                    () -> testTransferEmployeeToDepartment(reg.employees(), reg.departments()));
        } finally {
            System.out.println();
            System.out.println("--- Cleanup ---");
            runCleanups();
            System.out.printf("%nResults: %d passed, %d failed%n", passed, failed);
            System.exit(failed == 0 ? 0 : 1);
        }
    }

    // ------------------------------------------------------------------
    // Tiny test harness
    // ------------------------------------------------------------------

    private static void run(String name, Runnable test) {
        System.out.println();
        System.out.println("=== " + name + " ===");
        try {
            test.run();
            System.out.println("[PASS] " + name);
            passed++;
        } catch (Throwable t) {
            System.out.println("[FAIL] " + name + ": " + t);
            failed++;
        }
    }

    private static void step(String section, String message) {
        System.out.println("  [" + section + "] " + message);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void runCleanups() {
        int count = cleanups.size();
        for (int i = cleanups.size() - 1; i >= 0; i--) {
            try {
                cleanups.get(i).run();
            } catch (Throwable t) {
                System.out.println("  [cleanup warning] " + t);
            }
        }
        System.out.println("  [CLEANUP] removed " + count + " row(s) created during this run");
    }

    // ------------------------------------------------------------------
    // Client CRUD
    // ------------------------------------------------------------------

    private static void testClient(IClientService service) {
        // CREATE
        Client c = new Client();
        c.setName("Acme " + TAG);
        c.setIndustry("Manufacturing");
        c.setPrimaryContactName("Jane Doe");
        c.setPhone("555-0100");
        c.setEmail("jane@" + TAG + ".example");

        Client created = service.create(c);
        check(created.getId() != null && created.getId() > 0,
                "client id should be populated after create");
        Integer id = created.getId();
        cleanups.add(() -> service.delete(id));
        step("CREATE", "Client inserted (id=" + id + ", name=\"" + created.getName() + "\")");

        // READ (getById + getAll)
        Optional<Client> fetched = service.getById(id);
        check(fetched.isPresent(), "client should be findable by id");
        check(("Acme " + TAG).equals(fetched.get().getName()),
                "client name mismatch after read");
        step("READ", "getById returned client with matching name");

        check(service.getAll().stream().anyMatch(x -> id.equals(x.getId())),
                "getAll should contain the created client");
        step("READ", "getAll contains id=" + id);

        // UPDATE
        Client modified = fetched.get();
        String oldIndustry = modified.getIndustry();
        modified.setIndustry("Retail");
        Optional<Client> updated = service.update(id, modified);
        check(updated.isPresent(), "update should return updated client");
        String newIndustry = service.getById(id).orElseThrow().getIndustry();
        check("Retail".equals(newIndustry), "client industry should be updated");
        step("UPDATE", "industry changed: \"" + oldIndustry + "\" -> \"" + newIndustry + "\"");

        // DELETE
        service.delete(id);
        check(service.getById(id).isEmpty(), "client should be gone after delete");
        step("DELETE", "client id=" + id + " removed; getById returns empty");
    }

    // ------------------------------------------------------------------
    // Department CRUD
    // ------------------------------------------------------------------

    private static void testDepartment(IDepartmentService service) {
        // CREATE
        Department d = new Department();
        d.setName("Engineering " + TAG);
        d.setLocation("HQ");
        d.setAnnualBudget(1_000_000d);

        Department created = service.create(d);
        check(created.getId() != null && created.getId() > 0,
                "department id should be populated after create");
        Integer id = created.getId();
        cleanups.add(() -> service.delete(id));
        step("CREATE", "Department inserted (id=" + id + ", name=\"" + created.getName() + "\")");

        // READ
        Optional<Department> fetched = service.getById(id);
        check(fetched.isPresent(), "department should be findable by id");
        check(("Engineering " + TAG).equals(fetched.get().getName()),
                "department name mismatch after read");
        step("READ", "getById returned department with matching name");

        check(service.getAll().stream().anyMatch(x -> id.equals(x.getId())),
                "getAll should contain the created department");
        step("READ", "getAll contains id=" + id);

        // UPDATE
        Department modified = fetched.get();
        String oldLocation = modified.getLocation();
        modified.setLocation("Remote");
        Optional<Department> updated = service.update(id, modified);
        check(updated.isPresent(), "update should return updated department");
        String newLocation = service.getById(id).orElseThrow().getLocation();
        check("Remote".equals(newLocation), "department location should be updated");
        step("UPDATE", "location changed: \"" + oldLocation + "\" -> \"" + newLocation + "\"");

        // DELETE
        service.delete(id);
        check(service.getById(id).isEmpty(), "department should be gone after delete");
        step("DELETE", "department id=" + id + " removed; getById returns empty");
    }

    // ------------------------------------------------------------------
    // Employee CRUD
    // ------------------------------------------------------------------

    private static void testEmployee(IEmployeeService empService, IDepartmentService deptService) {
        // SETUP: department the employee can belong to (FK NOT NULL)
        Department engineering = new Department();
        engineering.setName("Eng " + TAG);
        engineering.setLocation("HQ");
        engineering.setAnnualBudget(500_000d);
        deptService.create(engineering);
        final Integer engId = engineering.getId();
        check(engId != null, "engineering department id must exist for employee test");
        cleanups.add(() -> deptService.delete(engId));
        step("SETUP", "created parent department (id=" + engId + ")");

        // CREATE
        Employee e = new Employee();
        e.setFullName("Alice " + TAG);
        e.setTitle("Senior Engineer");
        e.setHireDate(LocalDate.of(2021, 3, 1));
        e.setSalary(120_000d);
        e.setDepartment(engineering);

        Employee created = empService.create(e);
        check(created.getId() != null && created.getId() > 0,
                "employee id should be populated after create");
        Integer empId = created.getId();
        cleanups.add(() -> empService.delete(empId));
        step("CREATE", "Employee inserted (id=" + empId + ", name=\"" + created.getFullName()
                + "\", department=" + engId + ")");

        // READ
        Optional<Employee> fetched = empService.getById(empId);
        check(fetched.isPresent(), "employee should be findable by id");
        check(("Alice " + TAG).equals(fetched.get().getFullName()),
                "employee full name mismatch after read");
        step("READ", "getById returned employee with matching full name");

        // UPDATE
        Employee modified = fetched.get();
        String oldTitle = modified.getTitle();
        modified.setTitle("Staff Engineer");
        Optional<Employee> updated = empService.update(empId, modified);
        check(updated.isPresent(), "update should return updated employee");
        String newTitle = empService.getById(empId).orElseThrow().getTitle();
        check("Staff Engineer".equals(newTitle), "employee title should be updated");
        step("UPDATE", "title changed: \"" + oldTitle + "\" -> \"" + newTitle + "\"");

        // DELETE
        empService.delete(empId);
        check(empService.getById(empId).isEmpty(), "employee should be gone after delete");
        step("DELETE", "employee id=" + empId + " removed; getById returns empty");
    }

    // ------------------------------------------------------------------
    // Project CRUD
    // ------------------------------------------------------------------

    private static void testProject(IProjectService service) {
        // CREATE
        Project p = new Project();
        p.setName("Apollo " + TAG);
        p.setDescription("Platform migration");
        p.setStartDate(LocalDate.now().minusMonths(2));
        p.setEndDate(LocalDate.now().plusMonths(1));
        p.setBudget(250_000d);
        p.setStatus(ProjectStatus.ACTIVE);

        Project created = service.create(p);
        check(created.getId() != null && created.getId() > 0,
                "project id should be populated after create");
        Integer id = created.getId();
        cleanups.add(() -> service.delete(id));
        step("CREATE", "Project inserted (id=" + id + ", name=\"" + created.getName()
                + "\", status=" + created.getStatus() + ")");

        // READ
        Optional<Project> fetched = service.getById(id);
        check(fetched.isPresent(), "project should be findable by id");
        check(("Apollo " + TAG).equals(fetched.get().getName()),
                "project name mismatch after read");
        step("READ", "getById returned project with matching name");

        // UPDATE
        Project modified = fetched.get();
        ProjectStatus oldStatus = modified.getStatus();
        modified.setStatus(ProjectStatus.COMPLETED);
        Optional<Project> updated = service.update(id, modified);
        check(updated.isPresent(), "update should return updated project");
        ProjectStatus newStatus = service.getById(id).orElseThrow().getStatus();
        check(ProjectStatus.COMPLETED == newStatus, "project status should be updated");
        step("UPDATE", "status changed: " + oldStatus + " -> " + newStatus);

        // DELETE
        service.delete(id);
        check(service.getById(id).isEmpty(), "project should be gone after delete");
        step("DELETE", "project id=" + id + " removed; getById returns empty");
    }

    // ------------------------------------------------------------------
    // Task 1: calculateProjectHRCost
    //   cost = sum over allocated employees of
    //          (annualSalary / 12) * monthsBetweenRoundedUp(start, end) * (pct / 100)
    // ------------------------------------------------------------------

    private static void testCalculateProjectHRCost(
            IProjectService projService, IEmployeeService empService, IDepartmentService deptService) {

        // SETUP: department + employee + project
        Department dept = new Department();
        dept.setName("HRCost Dept " + TAG);
        dept.setLocation("HQ");
        dept.setAnnualBudget(500_000d);
        deptService.create(dept);
        final Integer deptId = dept.getId();
        cleanups.add(() -> deptService.delete(deptId));

        Employee emp = new Employee();
        emp.setFullName("Cost Tester " + TAG);
        emp.setTitle("Engineer");
        emp.setHireDate(LocalDate.of(2022, 1, 1));
        emp.setSalary(120_000d);
        emp.setDepartment(dept);
        empService.create(emp);
        final Integer empId = emp.getId();
        cleanups.add(() -> empService.delete(empId));

        LocalDate start = LocalDate.now().minusMonths(2);
        LocalDate end = LocalDate.now().plusMonths(2);
        Project proj = new Project();
        proj.setName("HRCost Project " + TAG);
        proj.setDescription("HR cost test");
        proj.setStartDate(start);
        proj.setEndDate(end);
        proj.setBudget(100_000d);
        proj.setStatus(ProjectStatus.ACTIVE);
        projService.create(proj);
        final Integer projId = proj.getId();
        cleanups.add(() -> projService.delete(projId));
        step("SETUP", "dept=" + deptId + ", employee=" + empId + " (salary=$120,000), project=" + projId
                + " (" + start + " -> " + end + ")");

        // WIRE RELATIONSHIP: employee is 50% allocated to project
        int allocation = 50;
        DbClient.execute(
                "INSERT INTO employee_projects (employee_id, project_id, allocation_percentage) VALUES (?, ?, ?)",
                empId, projId, allocation);
        step("SETUP", "allocated employee " + empId + " at " + allocation + "% to project " + projId);

        // EXECUTE
        Double cost = projService.calculateProjectHRCost(projId);
        step("EXECUTE", "calculateProjectHRCost(" + projId + ") = $" + cost);

        // VERIFY: must be positive. Lower bound: at least one month of half-salary.
        double minExpected = (120_000d / 12d) * (allocation / 100d); // one month minimum
        check(cost != null && cost > 0, "HR cost should be positive");
        check(cost >= minExpected - 0.01,
                "HR cost should be at least one month of half salary ($" + minExpected + "), got $" + cost);
        step("ASSERT", "cost > 0 and >= $" + minExpected + " (one-month floor)");
    }

    // ------------------------------------------------------------------
    // Task 2: getProjectsByDepartment
    //   filters to status = ACTIVE only, sorted by the given key
    // ------------------------------------------------------------------

    private static void testGetProjectsByDepartment(
            IProjectService projService, IDepartmentService deptService) {

        // SETUP: one department
        Department dept = new Department();
        dept.setName("PBD Dept " + TAG);
        dept.setLocation("HQ");
        dept.setAnnualBudget(400_000d);
        deptService.create(dept);
        final Integer deptId = dept.getId();
        cleanups.add(() -> deptService.delete(deptId));

        // Three projects: two ACTIVE, one COMPLETED (should be filtered out)
        Integer activeAId = createProject(projService, "PBD-Alpha " + TAG, ProjectStatus.ACTIVE);
        Integer activeBId = createProject(projService, "PBD-Bravo " + TAG, ProjectStatus.ACTIVE);
        Integer completedId = createProject(projService, "PBD-Charlie " + TAG, ProjectStatus.COMPLETED);
        step("SETUP", "projects: ACTIVE=" + activeAId + "," + activeBId + " COMPLETED=" + completedId);

        // WIRE all three to the department
        for (Integer pid : List.of(activeAId, activeBId, completedId)) {
            DbClient.execute(
                    "INSERT INTO department_projects (department_id, project_id) VALUES (?, ?)",
                    deptId, pid);
        }
        step("SETUP", "wired all 3 projects to department " + deptId);

        // EXECUTE
        List<Project> results = projService.getProjectsByDepartment(deptId, "name");
        step("EXECUTE", "getProjectsByDepartment(" + deptId + ", \"name\") returned "
                + results.size() + " project(s): "
                + results.stream().map(Project::getName).toList());

        // VERIFY: only the two ACTIVE ones, and sorted by name
        check(results.size() == 2, "should return only the 2 ACTIVE projects, got " + results.size());
        step("ASSERT", "exactly 2 projects returned (COMPLETED filtered out)");

        List<Integer> ids = results.stream().map(Project::getId).toList();
        check(ids.contains(activeAId) && ids.contains(activeBId),
                "both active projects should be in the result");
        check(!ids.contains(completedId),
                "completed project should NOT be in the result");
        step("ASSERT", "COMPLETED project " + completedId + " correctly excluded");

        check("PBD-Alpha ".concat(TAG).equals(results.get(0).getName())
                && "PBD-Bravo ".concat(TAG).equals(results.get(1).getName()),
                "results should be sorted alphabetically by name");
        step("ASSERT", "results sorted alphabetically: Alpha before Bravo");
    }

    private static Integer createProject(IProjectService service, String name, ProjectStatus status) {
        Project p = new Project();
        p.setName(name);
        p.setDescription("PBD test");
        p.setStartDate(LocalDate.now().minusMonths(1));
        p.setEndDate(LocalDate.now().plusMonths(1));
        p.setBudget(50_000d);
        p.setStatus(status);
        service.create(p);
        final Integer id = p.getId();
        cleanups.add(() -> service.delete(id));
        return id;
    }

    // ------------------------------------------------------------------
    // Task 3: findClientsByUpcomingProjectDeadline
    //   returns clients whose ANY project has endDate in [today, today+days]
    // ------------------------------------------------------------------

    private static void testFindClientsByUpcomingProjectDeadline(
            IClientService clientService, IProjectService projService) {

        // SETUP: one client with a soon-ending project and a far-future project
        Client client = new Client();
        client.setName("Deadline Client " + TAG);
        client.setIndustry("Tech");
        client.setPrimaryContactName("Tester");
        client.setPhone("555-0199");
        client.setEmail("test@" + TAG + ".example");
        clientService.create(client);
        final Integer clientId = client.getId();
        cleanups.add(() -> clientService.delete(clientId));

        // Soon-ending project (end = today + 10 days)
        Project soon = new Project();
        soon.setName("Soon-" + TAG);
        soon.setDescription("ends soon");
        soon.setStartDate(LocalDate.now().minusDays(30));
        soon.setEndDate(LocalDate.now().plusDays(10));
        soon.setBudget(10_000d);
        soon.setStatus(ProjectStatus.ACTIVE);
        projService.create(soon);
        final Integer soonId = soon.getId();
        cleanups.add(() -> projService.delete(soonId));

        // Far-future project (end = today + 200 days)
        Project far = new Project();
        far.setName("Far-" + TAG);
        far.setDescription("ends far out");
        far.setStartDate(LocalDate.now());
        far.setEndDate(LocalDate.now().plusDays(200));
        far.setBudget(20_000d);
        far.setStatus(ProjectStatus.ACTIVE);
        projService.create(far);
        final Integer farId = far.getId();
        cleanups.add(() -> projService.delete(farId));

        // WIRE: client -> both projects
        DbClient.execute("INSERT INTO client_projects (client_id, project_id) VALUES (?, ?)",
                clientId, soonId);
        DbClient.execute("INSERT INTO client_projects (client_id, project_id) VALUES (?, ?)",
                clientId, farId);
        step("SETUP", "client=" + clientId + " linked to soon(end=+10d, id=" + soonId
                + ") and far(end=+200d, id=" + farId + ")");

        // EXECUTE with a threshold that includes the 'soon' project
        List<Client> withinThirty = clientService.findClientsByUpcomingProjectDeadline(30);
        boolean foundAtThirty = withinThirty.stream().anyMatch(c -> clientId.equals(c.getId()));
        step("EXECUTE", "findClientsByUpcomingProjectDeadline(30) returned "
                + withinThirty.size() + " client(s); contains our client = " + foundAtThirty);
        check(foundAtThirty, "client should be returned when threshold covers the soon-ending project");
        step("ASSERT", "client found with threshold=30 (soon project ends within window)");

        // EXECUTE with a threshold that excludes both projects
        List<Client> withinFive = clientService.findClientsByUpcomingProjectDeadline(5);
        boolean foundAtFive = withinFive.stream().anyMatch(c -> clientId.equals(c.getId()));
        step("EXECUTE", "findClientsByUpcomingProjectDeadline(5) contains our client = " + foundAtFive);
        check(!foundAtFive, "client should NOT be returned when all deadlines are outside the window");
        step("ASSERT", "client correctly excluded with threshold=5 (no project ends within window)");
    }

    // ------------------------------------------------------------------
    // Task 4: transferEmployeeToDepartment (transactional reassignment)
    // ------------------------------------------------------------------

    private static void testTransferEmployeeToDepartment(
            IEmployeeService empService, IDepartmentService deptService) {

        // SETUP: two departments, employee starts in the first
        Department engineering = new Department();
        engineering.setName("Xfer-Eng " + TAG);
        engineering.setLocation("HQ");
        engineering.setAnnualBudget(500_000d);
        deptService.create(engineering);
        final Integer engId = engineering.getId();
        cleanups.add(() -> deptService.delete(engId));

        Department operations = new Department();
        operations.setName("Xfer-Ops " + TAG);
        operations.setLocation("Remote");
        operations.setAnnualBudget(300_000d);
        deptService.create(operations);
        final Integer opsId = operations.getId();
        cleanups.add(() -> deptService.delete(opsId));
        step("SETUP", "source dept=" + engId + ", target dept=" + opsId);

        Employee employee = new Employee();
        employee.setFullName("Mover " + TAG);
        employee.setTitle("Engineer");
        employee.setHireDate(LocalDate.of(2023, 1, 1));
        employee.setSalary(90_000d);
        employee.setDepartment(engineering);
        empService.create(employee);
        final Integer empId = employee.getId();
        cleanups.add(() -> empService.delete(empId));
        step("SETUP", "employee=" + empId + " currently in dept=" + engId);

        // EXECUTE
        Optional<Employee> transferred = empService.transferEmployeeToDepartment(empId, opsId);
        check(transferred.isPresent(), "transfer should return the employee");
        Department afterTransfer = transferred.get().getDepartment();
        check(afterTransfer != null && opsId.equals(afterTransfer.getId()),
                "returned employee should report the target department");
        step("EXECUTE", "transferEmployeeToDepartment(" + empId + ", " + opsId
                + ") returned employee with department=" + afterTransfer.getId());

        // VERIFY persistence with a fresh read
        Employee refetched = empService.getById(empId).orElseThrow();
        check(refetched.getDepartment() != null && opsId.equals(refetched.getDepartment().getId()),
                "persisted department_id should be the target after transfer");
        step("ASSERT", "fresh getById confirms dept=" + refetched.getDepartment().getId());
    }
}

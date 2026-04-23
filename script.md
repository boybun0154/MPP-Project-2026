# EEMS — Employment Management System

### Project Presentation Script (20–30 minutes)

**Team:** H and JP
**Course:** CS401-2026-04A-04D-01 (MPP)

---

## Section 1 — Introduction & Context  *(≈ 3 min)*

**Speakers: H & JP (together)**

### H (opens)

> Good morning everyone. My name is **H**, and together with my teammate **JP**, we are pleased to present our MPP course project: the **EEMS — Employment Management System**.
>
> EEMS is a Java N-Tier application that helps an organization keep track of its **departments, employees, projects, and external clients** in one consistent, reliable place. Our goal was to show that a clean object-oriented design — without using any framework like Spring, JPA or Hibernate — can still deliver a fully working system backed by a real relational database.

### JP (continues)

> To respect the course guidelines, the system is built on **pure JDBC** for persistence and a lightweight custom HTTP server for the presentation layer. The architecture is organized into four strict tiers:
>
> 1. **Presentation Layer** — `controller/` and `handler/` packages that expose REST-like endpoints.
> 2. **Business Logic Layer** — the `service/` package, where all validation rules and the four mandatory business tasks live.
> 3. **Domain Model Layer** — the `model/` package, containing the pure entities and their behavior.
> 4. **Data Access Layer** — the `repository/jdbc/` package, which executes SQL and maps result sets to Java objects.

### H (agenda)

> Here is the agenda for the next 25 minutes:
>
> 1. First, I will walk you through the **Domain Classes, attributes, and associations** — the conceptual heart of the system.
> 2. Then JP will map those classes to the **Database tables and foreign keys**.
> 3. After that, we will demonstrate the **four mandatory tasks** — two each — showing the application logic and the CRUD operations they trigger.
> 4. Finally, we will close with a short conclusion.
>
> Let’s begin.

---

## Section 2 — Domain Classes, Attributes, and Associations  *(≈ 5–6 min)*

**Speaker: H**

> *(Show the UML Class Diagram — `diagrams/diagram.png`)*
>
> This is our UML class diagram. We identified **four core domain classes** that drive the entire business: **Department**, **Employee**, **Project**, and **Client**. Let me go through each of them briefly, then I’ll explain how they relate.

### The Classes and Key Attributes

**1. Department** — the organizational unit.

> Key attributes are `id`, `name`, `location`, and `annualBudget`. A department hosts employees and collaborates on projects, so it also exposes a list of `employees` and a set of `projects`.

**2. Employee** — the workforce member.

> Key attributes are `id`, `fullName`, `title`, `hireDate`, and `salary`. Every employee **must** belong to exactly one `Department`, and carries a `Map<Project, Integer>` called `projectAllocationPercentages` — the percentage of their time assigned to each project. That map is the key to our first task, the HR cost calculation.

**3. Project** — a structured operational task.

> Key attributes are `id`, `name`, `startDate`, `endDate`, `budget`, and `status` (either `ACTIVE` or `COMPLETED`). A project also keeps `employeeAllocations`, `departments`, and `clients` — because a project is where all three other entities come together.

**4. Client** — the external sponsor.

> Key attributes are `id`, `name`, `industry`, `primaryContactName`, `phone`, and `email`. Clients are associated with one or more projects.

### Associations and Cardinality

> The relationships are the most important part of the model, because most of our business rules depend on them:
>
> - **Department `1` — `N` Employee**: one department has many employees; each employee belongs to exactly one department. This is a classic **one-to-many**.
> - **Department `N` — `M` Project**: a project can be hosted by several departments, and a department can host several projects. **Many-to-many**.
> - **Employee `N` — `M` Project**: employees are allocated to multiple projects, with an **additional attribute on the relationship** — the `allocation_percentage`. We modeled this as `Map<Project, Integer>`.
> - **Client `N` — `M` Project**: a project can have several sponsoring clients, and a client can sponsor multiple projects. **Many-to-many**.
>
> Notice the recurring pattern: **Project is the hub entity** — it sits at  the center of three many-to-many associations. This is intentional: it reflects the narrative of the problem statement, where employees from different departments collaborate on projects that are funded by clients.

> With this conceptual model in mind, I’ll hand it over to JP to show how these classes are persisted to the database.

---

## Section 3 — Database Tables and Associations  *(≈ 5–6 min)*

**Speaker: JP**

> *(Show the ERD / schema — open `src/main/resources/database/sqlite/seed.sql`)*
>
> Thank you H. As you saw, we have four domain classes and four relationships. Let me now show how each of these maps to our physical database schema. We use **SQLite** for development and **PostgreSQL** is also supported; both share the same seed script structure.

### Entity Tables (one per domain class)

> Every domain class maps to one physical table:
>
>
> | Domain Class | Table         | Primary Key | Notable Columns                                                       |
> | ------------ | ------------- | ----------- | --------------------------------------------------------------------- |
> | Department   | `departments` | `id`        | `name`, `location`, `annual_budget`                                   |
> | Employee     | `employees`   | `id`        | `full_name`, `title`, `hire_date`, `salary`, `**department_id` (FK)** |
> | Project      | `projects`    | `id`        | `name`, `start_date`, `end_date`, `budget`, `status`                  |
> | Client       | `clients`     | `id`        | `name`, `industry`, `primary_contact_name`, `phone`, `email`          |
>

> Every primary key is an auto-increment integer — it is the unique identifier of a row and what other tables reference when they want to point at it.

### The One-to-Many Relationship — Foreign Key in the Child

> The Department-to-Employee relationship is the simplest one. Because one department has many employees, we place a **foreign key** `department_id` directly in the `employees` table:
>
> ```sql
> FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE RESTRICT
> ```
>
> The `ON DELETE RESTRICT` clause enforces referential integrity: we cannot delete a department that still has employees assigned to it. This rule is also re-checked in the business layer, inside `DepartmentService.delete()`.

### The Three Many-to-Many Relationships — Junction Tables

> Many-to-many relationships cannot live inside a single table, so we created three **junction (link) tables**, each containing only foreign keys:
>
> - `**department_projects(department_id, project_id)`** — links departments to projects.
> - `**client_projects(client_id, project_id)**` — links sponsoring clients to projects.
> - `**employee_projects(employee_id, project_id, allocation_percentage)**` — links employees to projects, and **carries the relationship attribute** `allocation_percentage`. This is an excellent illustration of a relationship that itself has data on it.
>
> Each junction table uses a **composite primary key** made of the two foreign keys, which guarantees that the same pair cannot appear twice. We also declared `ON DELETE CASCADE` on these tables, so that if a project is removed, its allocations disappear automatically — the relational engine takes care of the cleanup.

### Summary — Mapping the Model to the Schema

> In short:
>
> - **Classes → Tables**, one to one.
> - **1:N relationships → Foreign Key column** on the “many” side.
> - **N:M relationships → Junction tables** with composite primary keys.
> - **Relationship attributes (`allocation_percentage`) → columns inside the junction table.**
>
> With the schema in place, let me hand it back to H to walk through the first two tasks.

---

## Section 4 — Flow of the Four Tasks  *(≈ 10–12 min)*

### Task 1 — Calculate Project HR Cost  *(≈ 2.5 min)*

**Speaker: H** — READ operation

> **Goal:** Given a project, compute how much it costs the company in **human resources** — that is, the sum of each allocated employee’s salary weighted by their allocation percentage and the duration of the project in months.
>
> **Application Logic** — implemented in `ProjectService.calculateProjectHRCost(int projectId)`:
>
> 1. Load the project by id; throw if it does not exist.
> 2. Compute the number of months between `startDate` and `endDate`, rounded up (so a project of “1 month and 3 days” counts as 2 months).
> 3. Walk through the `Map<Employee, Integer>` of employee allocations. For each entry:
>   - take the annual `salary`, divide by 12 to get the monthly salary,
>   - multiply by the number of months and by `allocation_percentage / 100`,
>   - accumulate into the total.
> 4. Round the final number to 2 decimals and return it.
>
> **Database Interaction — READ:**
>
> - `SELECT` the project row from `projects`.
> - `SELECT` the related allocations from `employee_projects` joined with `employees`.
> - No writes — this task is a pure aggregation read.
>
> **Why it matters:** it demonstrates the Employee–Project many-to-many relationship **with its attribute**, which is where a lot of real-world business logic actually lives.

---

### Task 2 — List Active Projects by Department, Sorted  *(≈ 2.5 min)*

**Speaker: H** — READ operation

> **Goal:** Given a department id, return all of its **active** projects, sorted by a user-chosen criterion (`budget`, `end_date`, `start_date`, or `name`).
>
> **Application Logic** — implemented in `ProjectService.getProjectsByDepartment(int departmentId, String sortBy)`:
>
> 1. Fetch all projects from the repository.
> 2. Filter to keep only those with `status == ACTIVE`.
> 3. Filter again to keep only those whose `departments` set contains the given department id.
> 4. Sort using a `Comparator` chosen at runtime based on the `sortBy` parameter — the `null`-safe switch expression in `comparatorFor(...)` does this cleanly.
> 5. Return the result as a list.
>
> **Database Interaction — READ:**
>
> - `SELECT` from `projects` joined with `department_projects` to resolve the many-to-many link.
> - The `status` filter and the sort happen in the service layer, keeping the repository simple.
>
> **Why it matters:** it shows how a **many-to-many relationship** (Department ↔ Project) is traversed in the service layer, combined with a status filter and flexible sorting.

---

### Task 3 — Find Clients with Upcoming Project Deadlines  *(≈ 2.5 min)*

**Speaker: JP** — READ operation

> **Goal:** Given a number of days `N`, return all clients who sponsor **at least one project** whose `end_date` is within the next `N` days from today. This is a reminder report — “who should we call this week?”.
>
> **Application Logic** — implemented in `ClientService.findClientsByUpcomingProjectDeadline(int daysUntilDeadline)`:
>
> 1. Validate the input — negative days are rejected with an `IllegalArgumentException`.
> 2. Compute `today` and the `limit = today + N days`.
> 3. Fetch all clients from the repository (each already hydrated with its set of projects).
> 4. Keep only those whose project set contains at least one project with `today ≤ endDate ≤ limit`.
> 5. Return the filtered list.
>
> **Database Interaction — READ:**
>
> - `SELECT` from `clients` joined with `client_projects` and `projects` to hydrate each client with its sponsored projects.
> - All date filtering happens in Java using `LocalDate`, keeping SQL simple.
>
> **Why it matters:** it combines the Client ↔ Project many-to-many with a **temporal filter**, and is a good example of business logic that would be painful to express purely in SQL but is crystal clear in the service layer.

---

### Task 4 — Transfer an Employee to Another Department  *(≈ 2.5 min)*

**Speaker: JP** — UPDATE operation (with transaction)

> **Goal:** Move an existing employee to a different department, keeping referential integrity intact.
>
> **Application Logic** — implemented in `EmployeeService.transferEmployeeToDepartment(int employeeId, int newDepartmentId)`:
>
> 1. Load the employee and throw if it does not exist.
> 2. Load the target department and throw if it does not exist — this is how we prevent pointing at a ghost department.
> 3. If the employee is **already in that department**, short-circuit and return — no SQL needed.
> 4. Otherwise, open a **JDBC transaction** with `DbClient.transaction(...)` and call `EmployeeRepository.updateDepartment(conn, employeeId, newDepartmentId)`, which runs a single targeted `UPDATE` statement.
> 5. Re-read the employee to return the fresh state to the caller.
>
> **Database Interaction — UPDATE (inside a transaction):**
>
> ```sql
> UPDATE employees SET department_id = ? WHERE id = ?
> ```
>
> - The database engine enforces the `FOREIGN KEY (department_id)` constraint, so an invalid department would be rejected.
> - Wrapping the call in `DbClient.transaction` guarantees atomicity — either the update commits entirely, or the connection is rolled back.
>
> **Why it matters:** it demonstrates the full **CRUD = Update** path end-to-end, including transaction management and the defensive checks we do in the service layer on top of the constraints already enforced by the database.

---

## Closing  *(≈ 1–2 min)*

**Speakers: H & JP (together)**

### JP

> To wrap up: we built EEMS as a clean, framework-free **N-Tier Java application**. We designed four domain classes tied together by one `1:N` and three `N:M` relationships, mapped them faithfully to a normalized relational schema, and delivered the four mandatory tasks across READ and UPDATE operations.

### H

> Along the way we practiced the skills this course emphasizes: **object-oriented modeling**, clean **separation of concerns** between presentation, service, domain, and persistence layers, and **pure JDBC** data access with proper transaction handling.
>
> Thank you for listening. We are now happy to take your questions.


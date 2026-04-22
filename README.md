## EEMS — Employment Management System (MPP Course Project)

This repository implements the EEMS (Employment Management System) as a Java N-Tier application. 
This project is built without frameworks (no Spring, JPA, or Hibernate), 
utilizing pure JDBC for data persistence as required by the course guidelines.

### Architecture (N-Tier Structure)

The system strictly adheres to the following four-tier architecture:

- **Presentation Layer (`src/main/java/controller`, `src/main/java/handler`)**: Provides CRUD operations and task endpoints via HTTP handlers (Rest-like API).
- **Domain Model Layer (`src/main/java/model`)**: Contains only data and business logic related to the entity itself.
- **Business Logic Layer (`src/main/java/service`)**: Contains core application logic, validation rules, and complex calculations (including the 4 mandatory business tasks).
- **Data Access Layer (`src/main/java/repository/jdbc`)**: Handles all persistence logic using JDBC, SQL execution, and mapping ResultSets to Java objects.

---

# Domain Class Definition (Design Deliverable)

These classes form the Domain Model Layer, representing the structural blueprint of the system.

### Client
| Attribute | Data Type | Justification |
| :--- | :--- | :--- |
| `id` | `Integer` | Unique primary key for the external registry. |
| `name` | `String` | Name of the external organization or associated client. |
| `industry` | `String` | The specific market segment the client operates in. |
| `primaryContactName`| `String` | The name of the primary contact person as explicitly required by the problem statement. |
| `phone` | `String` | Primary contact phone number for relationship management. |
| `email` | `String` | Primary contact email address for communication tracking. |
| `projects` | `Set<Project>` | Represents the **N:M** relationship; clients can sponsor multiple projects. |

### Department
| Attribute | Data Type | Justification |
| :--- | :--- | :--- |
| `id` | `Integer` | Unique primary key for identifying organizational units and enforcing referential integrity. |
| `name` | `String` | Identifies the department (e.g., "Engineering"), as required by the company structure. |
| `location` | `String` | Records the fixed physical site (building or city) where the unit operates. |
| `annualBudget` | `Double` | Tracks the total yearly financial capacity allocated to the department. |
| `projects` | `List<Project>` | Represents the **N:M** relationship; departments collaboratively host operational tasks. |
| `employees` | `List<Employee>` | Represents the **1:N** relationship; a department hosts multiple workforce members. |

### Employee
| Attribute | Data Type | Justification |
| :--- | :--- | :--- |
| `id` | `Integer` | Unique primary key for identification within the workforce. |
| `fullName` | `String` | Full name record of the individual as required by the narrative. |
| `title` | `String` | Current professional role (e.g., Manager, Specialist, Senior Developer). |
| `hireDate` | `LocalDate` | Records when the worker joined; used for seniority and project duration calculations. |
| `salary` | `Double` | The worker's current salary; essential for **Task 1 (HR Cost Calculation)**. |
| `department` | `Department` | Enforces the **N:1** relationship; every worker must be assigned to exactly one unit. |
| `projectAllocationPercentages` | `Map<Project, Int>`| Tracks the percentage of time allocated to multiple projects (N:M with attribute). |

### Project
| Attribute | Data Type | Justification |
| :--- | :--- | :--- |
| `id` | `Integer` | Unique primary key for project tracking. |
| `name` | `String` | Descriptive name of the structured operational task. |
| `description` | `String` | Provides the scope and detailed context of the project. |
| `startDate` | `LocalDate` | Defines the project beginning; used for duration logic in **Task 1**. |
| `endDate` | `LocalDate` | Defines the project conclusion; used for **Task 1** and **Task 3 (Deadlines)**. |
| `budget` | `Double` | Total financial budget defined for the project. |
| `status` | `String` | Tracks the project stage (e.g., 'Active', 'Completed'); used for filtering in **Task 2**. |
| `departments` | `Set<Department>` | Stores the multiple organizational units hosting the project (N:M). |
| `employeeAllocations` | `Map<Employee, Int>` | Tracks workforce members and their time allocation for **Task 1 (Cost Calculation)**. |
| `clients` | `Set<Client>` | Maintains links to external sponsors or associated organizations (N:M). |

# Relationships (Multiplicity)

- **Department (1) — (N) Employee**
  -Enforced by `department_id` Foreign Key in the `employees` table.
- **Department (N) — (M) Project**
  - Handled via junction table: `department_projects(department_id, project_id)`.
- **Employee (N) — (M) Project**
  - Handled via junction table: `employee_projects(employee_id, project_id, allocation_percentage)`.
- **Client (N) — (M) Project**
  - Handled via junction table: `client_projects(client_id, project_id)`.

---

## Database & Setup

- SQLite: `src/main/resources/database/sqlite/seed.sql`
- PostgreSQL: `src/main/resources/database/postgresql/seed.sql`


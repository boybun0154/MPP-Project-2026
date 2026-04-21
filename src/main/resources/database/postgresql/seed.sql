-- 1. RESET DATABASE
DROP TABLE IF EXISTS project_clients CASCADE;
DROP TABLE IF EXISTS employee_projects CASCADE;
DROP TABLE IF EXISTS project_departments CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS employees CASCADE;
DROP TABLE IF EXISTS departments CASCADE;

-- 2. CREATE TABLES

CREATE TABLE departments
(
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(100)     NOT NULL,
    location      VARCHAR(100)     NOT NULL,
    annual_budget DOUBLE PRECISION NOT NULL
);

CREATE TABLE employees
(
    id            SERIAL PRIMARY KEY,
    full_name     VARCHAR(150)     NOT NULL,
    title         VARCHAR(100)     NOT NULL,
    hire_date     DATE             NOT NULL,
    salary        DOUBLE PRECISION NOT NULL,
    department_id INTEGER          NOT NULL,
    CONSTRAINT fk_department
        FOREIGN KEY (department_id)
            REFERENCES departments (id)
            ON DELETE RESTRICT
);

CREATE TABLE projects
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(150)     NOT NULL,
    description TEXT,
    start_date  DATE             NOT NULL,
    end_date    DATE             NOT NULL,
    budget      DOUBLE PRECISION NOT NULL,
    status      VARCHAR(20) CHECK (status IN ('Active', 'Completed')) DEFAULT 'Active'
);

CREATE TABLE clients
(
    id                     SERIAL PRIMARY KEY,
    name                   VARCHAR(150) NOT NULL,
    industry               VARCHAR(100) NOT NULL,
    primary_contact_person VARCHAR(150),
    phone                  VARCHAR(20),
    email                  VARCHAR(100)
);

-- Junction Table: Project <-> Department (Many-to-Many)
CREATE TABLE project_departments
(
    project_id    INTEGER NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    department_id INTEGER NOT NULL REFERENCES departments (id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, department_id)
);

-- Junction Table: Employee <-> Project (Many-to-Many)
CREATE TABLE employee_projects
(
    employee_id           INTEGER NOT NULL REFERENCES employees (id) ON DELETE CASCADE,
    project_id            INTEGER NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    allocation_percentage INTEGER NOT NULL CHECK (allocation_percentage > 0 AND allocation_percentage <= 100),
    PRIMARY KEY (employee_id, project_id)
);

-- Junction Table: Project <-> Client (Many-to-Many)
CREATE TABLE project_clients
(
    project_id INTEGER NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    client_id  INTEGER NOT NULL REFERENCES clients (id) ON DELETE CASCADE,
    PRIMARY KEY (project_id, client_id)
);

-- INSERT SAMPLE DATA
INSERT INTO departments (name, location, annual_budget)
VALUES ('Software Engineering', 'Fairfield - Building A', 500000.00),
       ('Human Resources', 'Chicago', 150000.00),
       ('Digital Marketing', 'Fairfield - Building B', 200000.00),
       ('External Sales', 'New York', 300000.00),
       ('Research & Development', 'Fairfield - Lab 1', 450000.00);

INSERT INTO employees (full_name, title, hire_date, salary, department_id)
VALUES ('Shadow Aris', 'Senior Developer', '2023-01-15', 8500.00, 1),
       ('Alice Johnson', 'HR Specialist', '2022-06-01', 5000.00, 2),
       ('Robert Miller', 'Marketing Manager', '2021-03-20', 7200.00, 3),
       ('Sarah Davis', 'Sales Executive', '2024-02-10', 4500.00, 4),
       ('James Wilson', 'Junior Researcher', '2023-11-05', 3800.00, 5);

INSERT INTO projects (name, description, start_date, end_date, budget, status)
VALUES ('EEMS Portal', 'Management portal development', '2026-04-01', '2026-07-30', 50000.00, 'Active'),
       ('Summer Campaign', 'Marketing campaign for Gen-Z audience', '2026-05-01', '2026-08-15', 15000.00, 'Active'),
       ('NY Expansion', 'Opening a new operational headquarters', '2025-10-01', '2026-04-25', 120000.00, 'Active'),
       ('AI Support', 'Intelligent customer service chatbot', '2026-01-10', '2026-12-20', 80000.00, 'Active'),
       ('Tech Workshop 2026', 'New technologies training session', '2026-03-01', '2026-03-15', 5000.00, 'Completed');

INSERT INTO clients (name, industry, primary_contact_person, phone, email)
VALUES ('TechCorp', 'Technology', 'John Wick', '641-555-0101', 'contact@techcorp.com'),
       ('EduCloud', 'Education', 'Sarah Connor', '641-555-0102', 'sarah@educloud.org'),
       ('GreenEnergy', 'Energy', 'Bruce Wayne', '641-555-0103', 'wayne@green.com'),
       ('FastShip', 'Logistics', 'Tony Stark', '641-555-0104', 'tony@stark.com'),
       ('BankZero', 'Finance', 'Diana Prince', '641-555-0105', 'diana@bankzero.com');

-- INSERT RELATIONSHIPS
INSERT INTO project_departments (project_id, department_id)
VALUES (1, 1),
       (1, 2),
       (2, 3),
       (3, 4),
       (4, 1),
       (4, 5);

INSERT INTO employee_projects (employee_id, project_id, allocation_percentage)
VALUES (1, 1, 50),
       (1, 4, 30),
       (2, 1, 100),
       (3, 2, 80),
       (5, 4, 100);

INSERT INTO project_clients (project_id, client_id)
VALUES (1, 1),
       (3, 4),
       (4, 1),
       (4, 3),
       (5, 2);
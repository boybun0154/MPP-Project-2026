package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * These organizational units (departments) have a specific name,
 * a fixed location (building or city), and an annual budget.
 * These units are responsible for hosting and executing multiple operational tasks.
 */
public class Department {
    private Integer id;
    private String name;
    private String location;
    private Double annualBudget;
    private List<Project> projects = new ArrayList<>();
    private List<Employee> employees = new ArrayList<>();

    public Department() {

    }

    // region Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getAnnualBudget() {
        return annualBudget;
    }

    public void setAnnualBudget(Double annualBudget) {
        this.annualBudget = annualBudget;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    // endregion Getters and Setters

    // region Overridden Methods

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Department department = (Department) o;
        return Objects.equals(id, department.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // endregion Overridden Methods
}

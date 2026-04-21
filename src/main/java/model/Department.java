package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Department {
    private Integer id;
    private String name;
    private String location;
    private BigDecimal annualBudget;
    private final List<Project> projects = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>();

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

    public BigDecimal getAnnualBudget() {
        return annualBudget;
    }

    public void setAnnualBudget(BigDecimal annualBudget) {
        this.annualBudget = annualBudget;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public List<Employee> getEmployees() {
        return employees;
    }
}

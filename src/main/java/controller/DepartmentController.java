package controller;

import model.Department;
import model.Project;
import service.interfaces.IDepartmentService;
import service.interfaces.IProjectService;

import java.util.List;
import java.util.Optional;

public class DepartmentController {
    private final IDepartmentService service;
    private final IProjectService projectService;

    public DepartmentController(IDepartmentService service, IProjectService projectService) {
        this.service = service;
        this.projectService = projectService;
    }

    public Department create(Department department) {
        return service.create(department);
    }

    public List<Department> getAll() {
        return service.getAll();
    }

    public Optional<Department> getById(Integer id) {
        return service.getById(id);
    }

    public Optional<Department> update(Integer id, Department department) {
        return service.update(id, department);
    }

    public void delete(Integer id) {
        service.delete(id);
    }

    public List<Project> getProjectsByDepartment(Integer departmentId, String sortBy) {
        return projectService.getProjectsByDepartment(departmentId, sortBy);
    }
}
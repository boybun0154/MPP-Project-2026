package controller;

import model.Project;
import service.interfaces.IProjectService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProjectController {
    private final IProjectService service;

    public ProjectController(IProjectService service) {
        this.service = service;
    }

    public Project create(Project project) {
        return service.create(project);
    }

    public List<Project> getAll() {
        return service.getAll();
    }

    public Optional<Project> getById(Integer id) {
        return service.getById(id);
    }

    public Optional<Project> update(Integer id, Project project) {
        return service.update(id, project);
    }

    public void delete(Integer id) {
        service.delete(id);
    }

    public BigDecimal calculateHrCost(Integer projectId) {
        return service.calculateProjectHRCost(projectId);
    }
}
package service;

import model.Project;
import repository.interfaces.IRepository;
import service.interfaces.IProjectService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProjectService implements IProjectService {
    private final IRepository<Project, Long> projectRepository;

    public ProjectService(IRepository<Project, Long> projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public Project create(Project entity) {
        projectRepository.save(entity);
        return entity;
    }

    @Override
    public Optional<Project> getById(Long id) {
        return projectRepository.findById(id);
    }

    @Override
    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    @Override
    public Project update(Long id, Project entity) {
        // Placeholder: production version should validate id existence before save.
        projectRepository.save(entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        // Placeholder: IRepository currently has no delete contract.
    }

    @Override
    public BigDecimal calculateProjectHRCost(int projectId) {
        // Placeholder for page-2 task:
        // project duration in months * weighted salary allocation per assigned employee.
        throw new UnsupportedOperationException("calculateProjectHRCost is not implemented yet.");
    }

    @Override
    public List<Project> getProjectsByDepartment(int departmentId, String sortBy) {
        // Placeholder for page-2 task:
        // return Active projects for department sorted by sortBy field.
        throw new UnsupportedOperationException("getProjectsByDepartment is not implemented yet.");
    }
}

package service;

import model.Project;
import model.Employee;
import repository.interfaces.IRepository;
import service.interfaces.IProjectService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectService implements IProjectService {
    private final IRepository<Project> projectRepository;

    public ProjectService(IRepository<Project> projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public Project create(Project entity) {
        projectRepository.save(entity);
        return entity;
    }

    @Override
    public Optional<Project> getById(Integer id) {
        return projectRepository.findById(id);
    }

    @Override
    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    @Override
    public Optional<Project> update(Integer id, Project entity) {
        return projectRepository.findById(id).map(existing -> {
            entity.setId(id);
            projectRepository.save(entity);
            return entity;
        });
    }

    @Override
    public void delete(Integer id) {
        projectRepository.delete(id);
    }


    // Task 1: Cost Calculation
    @Override
    public Double calculateProjectHRCost(int projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        long months = monthsBetweenRoundedUp(project.getStartDate(), project.getEndDate());
        double total = 0.0;

        Map<Employee, Integer> allocations = project.getEmployeeAllocations();
        if (allocations == null || allocations.isEmpty()) return 0.0;

        for (Map.Entry<Employee, Integer> entry : allocations.entrySet()) {
            double annualSalary = (entry.getKey().getSalary() != null) ? entry.getKey().getSalary() : 60000.0;
            double monthlySalary = annualSalary / 12.0;
            double allocationPct = entry.getValue() / 100.0;

            total += (monthlySalary * months * allocationPct);
        }

        return Math.round(total * 100.0) / 100.0;
    }


    // Department delegates to Project to search the projects
    @Override
    public List<Project> getProjectsByDepartment(int departmentId, String sortBy) {
        return projectRepository.findAll().stream()
                .filter(p -> "Active".equalsIgnoreCase(p.getStatus()))
                .filter(p -> p.getDepartments().stream()
                        .anyMatch(d -> d.getId() != null && d.getId() == departmentId))
                .sorted(comparatorFor(sortBy))
                .collect(Collectors.toList());
    }

    private static long monthsBetweenRoundedUp(LocalDate start, LocalDate end) {
        if (start == null || end == null || !end.isAfter(start)) return 1L;
        long fullMonths = ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1));
        LocalDate anchor = start.plusMonths(fullMonths);
        if (end.isAfter(anchor)) fullMonths += 1;
        return Math.max(1L, fullMonths);
    }

    private static Comparator<Project> comparatorFor(String sortBy) {
        if (sortBy == null) return Comparator.comparing(Project::getId);
        return switch (sortBy.toLowerCase()) {
            case "budget", "project_budget" -> Comparator.comparing(Project::getBudget, Comparator.nullsLast(Double::compareTo));
            case "end_date" -> Comparator.comparing(Project::getEndDate, Comparator.nullsLast(LocalDate::compareTo));
            case "start_date" -> Comparator.comparing(Project::getStartDate, Comparator.nullsLast(LocalDate::compareTo));
            case "name" -> Comparator.comparing(Project::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(Project::getId);
        };
    }
}
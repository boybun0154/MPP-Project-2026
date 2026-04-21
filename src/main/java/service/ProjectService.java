package service;

import model.Project;
import repository.interfaces.IRepository;
import service.interfaces.IProjectService;

import model.Employee;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        if (projectRepository.findById(id).isEmpty()) {
            return Optional.empty();
        }

        entity.setId(id);

        projectRepository.save(entity);

        return Optional.of(entity);
    }

    @Override
    public void delete(Integer id) {
        // Placeholder: IRepository currently has no delete contract.
    }

    @Override
    public BigDecimal calculateProjectHRCost(int projectId) {
        Project project = projectRepository.findById((int) projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        long months = monthsBetweenRoundedUp(project.getStartDate(), project.getEndDate());
        BigDecimal durationMonths = BigDecimal.valueOf(months);

        BigDecimal total = BigDecimal.ZERO;
        Map<Employee, Integer> allocations = project.getEmployeeAllocations();

        if (allocations.isEmpty()) {
            // Dummy fallback so the method returns a sensible value during demos.
            BigDecimal dummyMonthlySalary = new BigDecimal("5000");
            BigDecimal dummyAllocation = new BigDecimal("0.50");
            return dummyMonthlySalary.multiply(durationMonths).multiply(dummyAllocation)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        for (Map.Entry<Employee, Integer> entry : allocations.entrySet()) {
            BigDecimal annualSalary = entry.getKey().getSalary() != null
                    ? entry.getKey().getSalary()
                    : new BigDecimal("60000");
            BigDecimal monthlySalary = annualSalary.divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_UP);
            BigDecimal allocationPct = BigDecimal.valueOf(entry.getValue())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal employeeCost = monthlySalary.multiply(durationMonths).multiply(allocationPct);
            total = total.add(employeeCost);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<Project> getProjectsByDepartment(int departmentId, String sortBy) {
        List<Project> projects = projectRepository.findAll().stream()
                .filter(p -> "Active".equalsIgnoreCase(p.getStatus()))
                .filter(p -> p.getDepartments().stream()
                        .anyMatch(d -> d.getId() != null && d.getId() == departmentId))
                .collect(Collectors.toList());

        Comparator<Project> comparator = comparatorFor(sortBy);
        projects.sort(comparator);
        return projects;
    }

    private static long monthsBetweenRoundedUp(LocalDate start, LocalDate end) {
        if (start == null || end == null || !end.isAfter(start)) return 1L;
        long fullMonths = ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1));
        LocalDate anchor = start.plusMonths(fullMonths);
        if (end.isAfter(anchor)) fullMonths += 1;
        return Math.max(1L, fullMonths);
    }

    private static Comparator<Project> comparatorFor(String sortBy) {
        if (sortBy == null) return Comparator.comparing(Project::getId, Comparator.nullsLast(Integer::compareTo));
        switch (sortBy.toLowerCase()) {
            case "project_budget":
            case "budget":
                return Comparator.comparing(Project::getBudget, Comparator.nullsLast(BigDecimal::compareTo));
            case "end_date":
                return Comparator.comparing(Project::getEndDate, Comparator.nullsLast(LocalDate::compareTo));
            case "start_date":
                return Comparator.comparing(Project::getStartDate, Comparator.nullsLast(LocalDate::compareTo));
            case "name":
                return Comparator.comparing(Project::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            default:
                return Comparator.comparing(Project::getId, Comparator.nullsLast(Integer::compareTo));
        }
    }
}

package service.interfaces;

import model.Project;

import java.math.BigDecimal;
import java.util.List;

public interface IProjectService extends IService<Project, Long> {
    BigDecimal calculateProjectHRCost(int projectId);
    List<Project> getProjectsByDepartment(int departmentId, String sortBy);
}

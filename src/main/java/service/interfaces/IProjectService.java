package service.interfaces;

import model.Project;

import java.util.List;

public interface IProjectService extends IService<Project> {
    Double calculateProjectHRCost(int projectId);

    List<Project> getProjectsByDepartment(int departmentId, String sortBy);
}

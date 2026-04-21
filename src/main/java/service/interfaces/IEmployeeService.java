package service.interfaces;

import model.Employee;

public interface IEmployeeService extends IService<Employee, Long> {
    void transferEmployeeToDepartment(int employeeId, int newDepartmentId);
}

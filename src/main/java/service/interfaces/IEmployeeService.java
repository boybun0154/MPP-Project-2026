package service.interfaces;

import model.Employee;

import java.util.Optional;

public interface IEmployeeService extends IService<Employee> {
    Optional<Employee> transferEmployeeToDepartment(int employeeId, int newDepartmentId);
}

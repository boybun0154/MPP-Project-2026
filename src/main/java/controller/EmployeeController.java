package controller;

import model.Employee;
import service.interfaces.IEmployeeService;
import java.util.List;
import java.util.Optional;

public class EmployeeController {
    private final IEmployeeService service;

    public EmployeeController(IEmployeeService service) {
        this.service = service;
    }

    public Employee create(Employee employee) {
        return service.create(employee);
    }

    public List<Employee> getAll() {
        return service.getAll();
    }

    public Optional<Employee> getById(Integer id) {
        return service.getById(id);
    }

    public Optional<Employee> update(Integer id, Employee employee) {
        return service.update(id, employee);
    }

    public void delete(Integer id) {
        service.delete(id);
    }

    public Optional<Employee> transfer(Integer empId, Integer newDeptId) {
        return service.transferEmployeeToDepartment(empId, newDeptId);
    }
}
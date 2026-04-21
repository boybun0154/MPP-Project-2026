package repository.jdbc;

import domain.Employee;
import repository.interfaces.IEmployeeRepository;
import repository.jdbc.core.DbClient;
import repository.jdbc.core.RowMapper;
import java.util.List;
import java.util.Optional;

public class EmployeeRepository implements IEmployeeRepository {
    @Override
    public Optional<Employee> findById(int id) {
        return DbClient.fetchOne(
                "SELECT * FROM employees WHERE id = ?",
                EMPLOYEE_MAPPER,
                id
        );
    }

    @Override
    public List<Employee> findAll() {
        return DbClient.query("SELECT * FROM employees", EMPLOYEE_MAPPER);
    }

    @Override
    public void save(Employee entity) {

    }

    private static final RowMapper<Employee> EMPLOYEE_MAPPER = rs -> new Employee(
//            rs.getInt("id"),
//            rs.getString("full_name"),
//            rs.getString("title"),
//            rs.getDate("hire_date").toLocalDate(),
//            rs.getBigDecimal("salary"),
//            rs.getInt("department_id")
    );
}

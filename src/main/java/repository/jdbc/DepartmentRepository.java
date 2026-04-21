package repository.jdbc;

import model.Department;
import repository.interfaces.IDepartmentRepository;
import repository.jdbc.core.DbClient;
import repository.jdbc.core.RowMapper;
import java.util.List;
import java.util.Optional;

public class DepartmentRepository implements IDepartmentRepository {
    @Override
    public Optional<Department> findById(Integer id) {
        return DbClient.fetchOne(
                "SELECT * FROM departments WHERE id = ?",
                DEPARTMENT_MAPPER,
                id
        );
    }

    @Override
    public List<Department> findAll() {
        return DbClient.query("SELECT * FROM departments", DEPARTMENT_MAPPER);
    }

    @Override
    public void save(Department entity) {

    }

    @Override
    public void delete(Integer id) {

    }

    private static final RowMapper<Department> DEPARTMENT_MAPPER = rs -> new Department();

}

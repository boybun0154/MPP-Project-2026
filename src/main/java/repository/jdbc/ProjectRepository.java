package repository.jdbc;


import model.Project;
import repository.interfaces.IProjectRepository;
import repository.jdbc.core.DbClient;
import repository.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class ProjectRepository implements IProjectRepository {
    @Override
    public Optional<Project> findById(Integer id) {
        return DbClient.fetchOne(
                "SELECT * FROM clients WHERE id = ?",
                PROJECT_MAPPER,
                id
        );
    }

    @Override
    public List<Project> findAll() {
        return DbClient.query("SELECT * FROM clients", PROJECT_MAPPER);
    }

    @Override
    public void save(Project entity) {

    }

    @Override
    public void delete(Integer id) {

    }

    private static final RowMapper<Project> PROJECT_MAPPER = rs -> new Project();
}

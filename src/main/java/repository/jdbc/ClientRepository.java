package repository.jdbc;

import model.Client;
import repository.interfaces.IClientRepository;
import repository.jdbc.core.DbClient;
import repository.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class ClientRepository implements IClientRepository {
    @Override
    public Optional<Client> findById(Integer id) {
        return DbClient.fetchOne(
                "SELECT * FROM clients WHERE id = ?",
                CLIENT_MAPPER,
                id
        );
    }

    @Override
    public List<Client> findAll() {
        return DbClient.query("SELECT * FROM clients", CLIENT_MAPPER);
    }

    @Override
    public void save(Client entity) {

    }

    @Override
    public void delete(Integer id) {

    }

    private static final RowMapper<Client> CLIENT_MAPPER = rs -> new Client();
}

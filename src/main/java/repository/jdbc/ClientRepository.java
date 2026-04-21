package repository.jdbc;

import model.Client;
import model.Project;
import repository.interfaces.IClientRepository;
import repository.jdbc.core.DbClient;
import repository.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class ClientRepository implements IClientRepository {

    @Override
    public Optional<Client> findById(Integer id) {
        Optional<Client> client = DbClient.fetchOne(
                "SELECT * FROM clients WHERE id = ?",
                CLIENT_MAPPER, id
        );
        client.ifPresent(this::hydrate);
        return client;
    }

    @Override
    public List<Client> findAll() {
        List<Client> clients = DbClient.query("SELECT * FROM clients", CLIENT_MAPPER);
        clients.forEach(this::hydrate);
        return clients;
    }

    @Override
    public void save(Client entity) {
        if (entity.getId() == null || entity.getId() == 0) {
            int id = DbClient.insert(
                    "INSERT INTO clients (name, industry, primary_contact_name, phone, email) VALUES (?, ?, ?, ?, ?)",
                    entity.getName(), entity.getIndustry(), entity.getPrimaryContactName(),
                    entity.getPhone(), entity.getEmail()
            );
            entity.setId(id);
        } else {
            DbClient.execute(
                    "UPDATE clients SET name = ?, industry = ?, primary_contact_name = ?, phone = ?, email = ? WHERE id = ?",
                    entity.getName(), entity.getIndustry(), entity.getPrimaryContactName(),
                    entity.getPhone(), entity.getEmail(), entity.getId()
            );
        }

        hydrate(entity);
    }

    @Override
    public void delete(Integer id) {
        DbClient.execute("DELETE FROM clients WHERE id = ?", id);
    }

    private void hydrate(Client client) {
        String sql = "SELECT p.* FROM projects p " +
                "JOIN client_projects cp ON p.id = cp.project_id " +
                "WHERE cp.client_id = ?";

        List<Project> projects = DbClient.query(sql, rs -> {
            Project p = new Project();
            p.setId(rs.getInt("id"));
            p.setName(rs.getString("name"));
            p.setEndDate(rs.getObject("end_date", LocalDate.class));
            return p;
        }, client.getId());

        client.setProjects(new HashSet<>(projects));
    }

    private static final RowMapper<Client> CLIENT_MAPPER = rs -> {
        Client c = new Client();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setIndustry(rs.getString("industry"));
        c.setPrimaryContactName(rs.getString("primary_contact_name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        return c;
    };
}
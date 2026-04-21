package controller;

import model.Client;
import service.interfaces.IClientService;

import java.util.List;
import java.util.Optional;

public class ClientController {
    private final IClientService service;

    public ClientController(IClientService service) {
        this.service = service;
    }

    public Client create(Client client) {
        return service.create(client);
    }

    public List<Client> getAll() {
        return service.getAll();
    }

    public Optional<Client> getById(Integer id) {
        return service.getById(id);
    }

    public Optional<Client> update(Integer id, Client client) {
        return service.update(id, client);
    }

    public void delete(Integer id) {
        service.delete(id);
    }

    public List<Client> findByDeadline(int days) {
        return service.findClientsByUpcomingProjectDeadline(days);
    }
}

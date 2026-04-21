package service;

import model.Client;
import repository.interfaces.IRepository;
import service.interfaces.IClientService;

import java.util.List;
import java.util.Optional;

public class ClientService implements IClientService {
    private final IRepository<Client, Long> clientRepository;

    public ClientService(IRepository<Client, Long> clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Client create(Client entity) {
        clientRepository.save(entity);
        return entity;
    }

    @Override
    public Optional<Client> getById(Long id) {
        return clientRepository.findById(id);
    }

    @Override
    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    @Override
    public Client update(Long id, Client entity) {
        // Placeholder: production version should validate id existence before save.
        clientRepository.save(entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        // Placeholder: IRepository currently has no delete contract.
    }

    @Override
    public List<Client> findClientsByUpcomingProjectDeadline(int daysUntilDeadline) {
        // Placeholder for page-2 task:
        // return clients tied to projects ending within daysUntilDeadline from now.
        throw new UnsupportedOperationException("findClientsByUpcomingProjectDeadline is not implemented yet.");
    }
}

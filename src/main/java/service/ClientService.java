package service;

import model.Client;
import repository.interfaces.IClientRepository;
import service.interfaces.IClientService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientService implements IClientService {
    private final IClientRepository clientRepository;

    public ClientService(IClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Client create(Client entity) {
        clientRepository.save(entity);
        return entity;
    }

    @Override
    public Optional<Client> getById(Integer id) {
        return clientRepository.findById(id);
    }

    @Override
    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    @Override
    public Optional<Client> update(Integer id, Client entity) {
        return clientRepository.findById(id).map(existing -> {
            entity.setId(id);
            clientRepository.save(entity);
            return entity;
        });
    }

    @Override
    public void delete(Integer id) {
        clientRepository.delete(id);
    }

    // Task 3: Client Project Report
    @Override
    public List<Client> findClientsByUpcomingProjectDeadline(int daysUntilDeadline) {
        if (daysUntilDeadline < 0) throw new IllegalArgumentException("Days must be positive");

        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(daysUntilDeadline);

        return clientRepository.findAll().stream()
                .filter(client -> client.getProjects().stream()
                        .anyMatch(p -> p.getEndDate() != null &&
                                !p.getEndDate().isBefore(today) &&
                                !p.getEndDate().isAfter(limit)))
                .collect(Collectors.toList());
    }
}
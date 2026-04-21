package service;

import model.Client;
import model.Project;
import repository.interfaces.IRepository;
import service.interfaces.IClientService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientService implements IClientService {
    private final IRepository<Client> clientRepository;

    public ClientService(IRepository<Client> clientRepository) {
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
        if (clientRepository.findById(id).isEmpty()) {
            return Optional.empty();
        }

        entity.setId(id);

        clientRepository.save(entity);

        return Optional.of(entity);
    }
    @Override
    public void delete(Integer id) {
        // Placeholder: IRepository currently has no delete contract.
    }

    @Override
    public List<Client> findClientsByUpcomingProjectDeadline(int daysUntilDeadline) {
        if (daysUntilDeadline < 0) {
            throw new IllegalArgumentException("daysUntilDeadline must be non-negative");
        }
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(daysUntilDeadline);

        return clientRepository.findAll().stream()
                .filter(client -> client.getProjects().stream().anyMatch(p -> withinDeadline(p, today, cutoff)))
                .collect(Collectors.toList());
    }

    private static boolean withinDeadline(Project p, LocalDate today, LocalDate cutoff) {
        LocalDate end = p.getEndDate();
        if (end == null) return false;
        return !end.isBefore(today) && !end.isAfter(cutoff);
    }
}

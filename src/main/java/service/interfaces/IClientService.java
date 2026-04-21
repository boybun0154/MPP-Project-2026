package service.interfaces;

import model.Client;

import java.util.List;

public interface IClientService extends IService<Client> {
    List<Client> findClientsByUpcomingProjectDeadline(int daysUntilDeadline);
}

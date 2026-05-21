package com.indigo.synapse.security.oauth2;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryRegisteredClientRepository implements RegisteredClientRepository {

    private final Map<String, RegisteredClient> clientsById = new ConcurrentHashMap<>();
    private final Map<String, RegisteredClient> clientsByClientId = new ConcurrentHashMap<>();

    @Override
    public void save(RegisteredClient registeredClient) {
        RegisteredClient client = Objects.requireNonNull(registeredClient, "registeredClient must not be null");
        clientsById.put(client.getId(), client);
        clientsByClientId.put(client.getClientId(), client);
    }

    @Override
    public RegisteredClient findById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        return clientsById.get(id);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        return clientsByClientId.get(clientId);
    }
}

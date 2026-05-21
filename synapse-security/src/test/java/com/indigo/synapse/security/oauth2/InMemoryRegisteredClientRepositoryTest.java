package com.indigo.synapse.security.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryRegisteredClientRepositoryTest {

    @Test
    void shouldSaveAndLookupRegisteredClient() {
        InMemoryRegisteredClientRepository repository = new InMemoryRegisteredClientRepository();
        RegisteredClient client = RegisteredClient.withId("client-1")
                .clientId("app-1")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();

        repository.save(client);

        assertEquals(client, repository.findById("client-1"));
        assertEquals(client, repository.findByClientId("app-1"));
        assertNull(repository.findById("missing"));
    }

    @Test
    void shouldRejectBlankIdentifiers() {
        InMemoryRegisteredClientRepository repository = new InMemoryRegisteredClientRepository();

        assertThrows(IllegalArgumentException.class, () -> repository.findById(" "));
        assertThrows(IllegalArgumentException.class, () -> repository.findByClientId(" "));
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }
}

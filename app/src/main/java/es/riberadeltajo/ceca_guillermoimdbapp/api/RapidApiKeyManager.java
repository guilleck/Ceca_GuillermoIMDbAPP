package es.riberadeltajo.ceca_guillermoimdbapp.api;

import java.util.ArrayList;
import java.util.List;

public class RapidApiKeyManager {
    private List<String> apiKeys;
    private int currentKeyIndex;

    public RapidApiKeyManager() {
        apiKeys = new ArrayList<>();
        apiKeys.add("fef30253e4msh63af6d6939b06a8p1db685jsn7f16ad7a721f");
        apiKeys.add("9068511b32mshc3474a094765530p1b4636jsn462c02b21ee0");
        apiKeys.add("031ee8cef5msh1dbf8ad579d4953p1b26eejsne34c44c51e12");

        currentKeyIndex = 0; // Asegurar que el índice inicial sea válido
    }

    public String getCurrentKey() {
        if (apiKeys.isEmpty()) {
            throw new IllegalStateException("No hay claves API disponibles.");
        }
        return apiKeys.get(currentKeyIndex);
    }

    public void switchToNextKey() {
        if (apiKeys.isEmpty()) {
            throw new IllegalStateException("No hay claves API disponibles para cambiar.");
        }
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size();
        System.out.println("Clave API cambiada a: " + getCurrentKey()); // Log para depuración
    }

    public void addApiKey(String newKey) {
        apiKeys.add(newKey);
    }

    public int getTotalKeys() {
        return apiKeys.size();
    }
}

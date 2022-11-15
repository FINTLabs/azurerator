package no.fintlabs.azure.storage;

import com.azure.resourcemanager.storage.StorageManager;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.AzureConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class StorageResourceRepository {

    private final Map<String, StorageResource> storageResources = new HashMap<>();
    private final StorageManager storageManager;
    private final AzureConfiguration azureConfiguration;


    public StorageResourceRepository(StorageManager storageManager, AzureConfiguration azureConfiguration) {
        this.storageManager = storageManager;
        this.azureConfiguration = azureConfiguration;
    }


    @PostConstruct
    public void init() {
        loadStorageResources();
    }

    public Collection<StorageResource> getAll() {
        return storageResources.values();
    }

    public Optional<StorageResource> get(String key) {
        return Optional.ofNullable(storageResources.get(key));
    }

    public void add(StorageResource storageResource) {

        storageResources.put(
                storageResource.getStorageAccountName(),
                storageResource
        );
    }

    public void remove(StorageResource storageResource) {
        storageResources.remove(storageResource.getStorageAccountName());
    }

    public boolean exists(String storageAccountName) {
        return storageResources.containsKey(storageAccountName);
    }

    public void update(StorageResource storageResource) {
        storageResources.put(storageResource.getStorageAccountName(), storageResource);
    }

    public long size() {
        return storageResources.size();
    }

    private void loadStorageResources() {
        storageManager.storageAccounts()
                .list()
                .stream()
                .filter(storageAccount -> storageAccount.resourceGroupName().equals(azureConfiguration.getStorageAccountResourceGroup()))
                .forEach(storageAccount -> add(StorageResource.of(storageAccount)));

        log.debug("Found {} storage accounts:", storageResources.size());
        storageResources.forEach((name, status) -> log.debug("{} -> {}", name, status));
    }
}

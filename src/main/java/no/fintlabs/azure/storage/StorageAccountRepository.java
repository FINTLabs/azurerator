package no.fintlabs.azure.storage;

import com.azure.resourcemanager.storage.StorageManager;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.AzureConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class StorageAccountRepository {

    private final Map<String, AzureStorageObject> storageAccounts = new HashMap<>();
    private final StorageManager storageManager;
    private final AzureConfiguration azureConfiguration;


    public StorageAccountRepository(StorageManager storageManager, AzureConfiguration azureConfiguration) {
        this.storageManager = storageManager;
        this.azureConfiguration = azureConfiguration;
    }


    @PostConstruct
    public void init() {
        loadStorageAccounts();
    }

    public Collection<AzureStorageObject> getAll() {
        return storageAccounts.values();
    }

    public void add(AzureStorageObject azureStorageObject) {

        storageAccounts.put(
                //getAccountStatusName(storageAccount.resourceGroupName(), storageAccount.name()),
                //storageAccount.accountStatuses().primary().name()
                azureStorageObject.getStorageAccountName(),
                azureStorageObject
        );
    }

    public void remove(AzureStorageObject azureStorageObject) {
        storageAccounts.remove(azureStorageObject.getStorageAccountName());
    }

    public boolean exists(String storageAccountName) {
        return storageAccounts.containsKey(storageAccountName);
    }

    public long size() {
        return storageAccounts.size();
    }

    private void loadStorageAccounts() {
        storageManager.storageAccounts()
                .list()
                .stream()
                .filter(storageAccount -> storageAccount.resourceGroupName().equals(azureConfiguration.getStorageAccountResourceGroup()))
                .forEach(storageAccount -> add(AzureStorageObject.of(storageAccount)));

        log.debug("Found {} storage accounts:", storageAccounts.size());
        storageAccounts.forEach((name, status) -> log.debug("{} -> {}", name, status));
    }

//    private String getAccountStatusName(String resourceGroup, String name) {
//        return String.format("%s/%s",
//                resourceGroup,
//                name);
//    }

}

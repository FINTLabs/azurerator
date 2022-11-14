package no.fintlabs.azure.storage;

import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.azure.AzureConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class StorageAccountRepository {

    private final Map<String, String> storageAccounts = new HashMap<>();
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

    public Map<String, String> getAll() {
        return storageAccounts;
    }
    public void add(StorageAccount storageAccount) {
        storageAccounts.put(
                getAccountStatusName(storageAccount.resourceGroupName(), storageAccount.name()),
                storageAccount.accountStatuses().primary().name()
        );
    }

    public void remove(AzureStorageObject azureStorageObject) {
        storageAccounts.remove(getAccountStatusName(azureStorageObject.getResourceGroup(), azureStorageObject.getStorageAccountName()));
    }

    public boolean exists(String storageAccountName) {
        return storageAccounts.containsKey(getAccountStatusName(azureConfiguration.getStorageAccountResourceGroup(), storageAccountName));
    }

    public long size() {
        return storageAccounts.size();
    }
    private void loadStorageAccounts() {
        storageManager.storageAccounts()
                .list()
                .stream()
                .filter(storageAccount -> storageAccount.resourceGroupName().equals(azureConfiguration.getStorageAccountResourceGroup()))
                .forEach(storageAccount ->
                        storageAccounts.put(
                                getAccountStatusName(storageAccount.resourceGroupName(), storageAccount.name()),
                                storageAccount.accountStatuses().primary().name())
                );
        log.debug("Found {} storage accounts:", storageAccounts.size());
        storageAccounts.forEach((name, status) -> log.debug("{} -> {}", name, status));
    }

    private String getAccountStatusName(String resourceGroup, String name) {
        return String.format("%s/%s",
                resourceGroup,
                name);
    }

}

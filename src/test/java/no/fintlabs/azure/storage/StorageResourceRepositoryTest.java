package no.fintlabs.azure.storage;

import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.*;
import no.fintlabs.Props;
import no.fintlabs.azure.AzureConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static no.fintlabs.azure.TagNames.TAG_ENVIRONMENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageResourceRepositoryTest {
    @Mock
    private StorageManager storageManager;

    @Mock
    private AzureConfiguration azureConfiguration;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StorageAccounts storageAccounts;

    @InjectMocks
    StorageResourceRepository storageResourceRepository;

    @Test
    public void testGetStorageResource() {
        var testResource = createTestStorageResource("test-storage-account-1");
        storageResourceRepository.add(testResource);
        assertEquals(testResource, storageResourceRepository.get("test-storage-account-1").get());
    }

    @Test
    public void testAddingStorageResourceIncreasesRepositorySizeByOne() {
        long beforeSize = storageResourceRepository.size();

        var testResource = createTestStorageResource("test-storage-account-1");
        storageResourceRepository.add(testResource);
        assertEquals(beforeSize + 1, storageResourceRepository.size());
    }

    @Test
    public void testRemovingStorageResourceDecreasesRepositorySizeByOne() {
        var testResource = createTestStorageResource("test-storage-account-1");
        storageResourceRepository.add(testResource);
        long beforeSize = storageResourceRepository.size();
        storageResourceRepository.remove(testResource);
        assertEquals(beforeSize - 1, storageResourceRepository.size());
    }

    @Test
    public void whenUpdatingStorageResourceThenItShouldBeReflectedInRepository() {
        var testResource = createTestStorageResource("test-storage-account-1");
        storageResourceRepository.add(testResource);
        var testResource2 = createTestStorageResource("test-storage-account-1");
        storageResourceRepository.update(testResource2);

        assertEquals(1, storageResourceRepository.size());
        assertEquals(testResource2, storageResourceRepository.get("test-storage-account-1").get());
    }

    @Test
    public void checkingIfANonexistentStorageResourceExistsShouldReturnFalse() {
        boolean exists = storageResourceRepository.exists("test");
        assertFalse(exists);
    }

    @Test
    public void checkingIfAStorageResourceExistsShouldReturnTrue() {
        var testResource = createTestStorageResource("test-storage-account-1");
        storageResourceRepository.add(testResource);
        assertTrue(storageResourceRepository.exists("test-storage-account-1"));
    }

    @Test
    public void testGetAll() {
        Collection<StorageResource> storageResources = storageResourceRepository.getAll();
        assertNotNull(storageResources);
    }

    @ResourceLock("Props")
    @Test
    public void testLoadResources() {
        var props = mockStatic(Props.class);
        props.when(Props::getEnvironment).thenReturn("test-env");

        var storageAccunts = List.of(
            createTestStorageAccount("test-storage-account-1", "test-rg", "test-env"),
            createTestStorageAccount("test-storage-account-2", "test-rg", "test-env")
        );

        when(azureConfiguration.getStorageAccountResourceGroup()).thenReturn("test-rg");
        when(storageManager.storageAccounts()).thenReturn(storageAccounts);
        when(storageAccounts.list().stream()).thenReturn(storageAccunts.stream());

        storageResourceRepository.loadStorageResources();

        assertEquals(2, storageResourceRepository.size());
        props.close();
    }

    @Test
    public void testLoadResourcesMultipleEnvironments() {
        var props = mockStatic(Props.class);
        props.when(Props::getEnvironment).thenReturn("test-env");

        var storageAccunts = List.of(
                createTestStorageAccount("test-storage-account-1", "test-rg", "test-env"),
                createTestStorageAccount("test-storage-account-2", "test-rg", "test-env-2")
        );

        when(azureConfiguration.getStorageAccountResourceGroup()).thenReturn("test-rg");
        when(storageManager.storageAccounts()).thenReturn(storageAccounts);
        when(storageAccounts.list().stream()).thenReturn(storageAccunts.stream());

        storageResourceRepository.loadStorageResources();

        assertEquals(1, storageResourceRepository.size());
    }

    public StorageAccount createTestStorageAccount(String name, String rg, String environment) {
        StorageAccount storageAccount = mock(StorageAccount.class);
        lenient().when(storageAccount.name()).thenReturn(name);
        when(storageAccount.resourceGroupName()).thenReturn(rg);
        when(storageAccount.tags()).thenReturn(Map.of(TAG_ENVIRONMENT, environment));
        lenient().when(storageAccount.accountStatuses()).thenReturn(new AccountStatuses(AccountStatus.fromString("AVAILABLE"), null));
        lenient().when(storageAccount.getKeys()).thenReturn(List.of(new StorageAccountKey()));
        return storageAccount;
    }

    public StorageResource createTestStorageResource(String storageAccountName) {
        StorageResource storageResource = mock(StorageResource.class);
        when(storageResource.getStorageAccountName()).thenReturn(storageAccountName);
        return storageResource;
    }
}

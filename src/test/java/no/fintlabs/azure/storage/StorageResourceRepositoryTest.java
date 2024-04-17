package no.fintlabs.azure.storage;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.spy;

public class StorageResourceRepositoryTest {

    StorageResource storageResource1 = StorageResource.builder()
            .storageAccountName("test-storage-account-1")
            .resourceGroup("test-resource-group-1")
            .connectionString("test-connection-string-1")
            .status("test")
            .team("test")
            .orgId("test")
            .portalUri("test")
            .environment("alpha")
            .path("test")
            .type(null)
            .crdName("test")
            .crdNamespace("test")
            .instance("test")
            .partOf("true")
            .build();

    StorageResource storageResource2 = StorageResource.builder()
            .storageAccountName("test-storage-account-2")
            .resourceGroup("test-resource-group-2")
            .connectionString("test-connection-string-2")
            .status("test")
            .team("test")
            .orgId("test")
            .portalUri("test")
            .environment("beta")
            .path("test")
            .type(null)
            .crdName("test")
            .crdNamespace("test")
            .instance("test")
            .partOf("false")
            .build();

    StorageResource storageResource3 = StorageResource.builder()
            .storageAccountName("test-storage-account-3")
            .resourceGroup("test-resource-group-3")
            .connectionString("test-connection-string-3")
            .status("test")
            .team("test")
            .orgId("test")
            .portalUri("test")
            .environment("api")
            .path("test")
            .type(null)
            .crdName("test")
            .crdNamespace("test")
            .instance("test")
            .partOf("false")
            .build();

    StorageResourceRepository storageAccountRepository = new StorageResourceRepository(null, null);


    @Test
    public void testAddingStorageResourceIncreasesRepositorySizeByOne() throws Exception {
        StorageResourceRepository storageResourceRepository = new StorageResourceRepository(null, null);
        storageResourceRepository.add(storageResource1);
        assert storageResourceRepository.size() == 1;
    }

    @Test
    public void testRemovingStorageResourceDecreasesRepositorySizeByOne() throws Exception {
        storageAccountRepository.add(storageResource1);
        storageAccountRepository.remove(storageResource1);
        assert storageAccountRepository.size() == 0;
    }

    @Test
    public void whenUpdatingStorageResourceThenItShouldBeReflectedInRepository() throws Exception {
        storageAccountRepository.add(storageResource1);
        storageResource1.setEnvironment("beta");
        assertEquals("beta", storageAccountRepository.get(storageResource1.getStorageAccountName()).get().getEnvironment());
    }

    @Test
    public void checkingIfANonexistentStorageResourceExistsShouldReturnFalse() throws Exception {
        boolean exists = storageAccountRepository.exists("test");
        assertFalse(exists);
    }

    @Test
    public void checkingIfAStorageResourceExistsShouldReturnTrue() throws Exception {
        storageAccountRepository.add(storageResource1);
        boolean exists = storageAccountRepository.exists("test-storage-account-1");
        assertTrue(exists);
    }

    @Test
    public void getAllStorageObjectsThatHasEnvironmentAlpha() throws Exception {
        storageAccountRepository.add(storageResource1);
        storageAccountRepository.add(storageResource2);
        storageAccountRepository.add(storageResource3);

        List<StorageResource> storageResources = (List<StorageResource>) storageAccountRepository.getStorageResourcesByEnvironment("alpha");

        assertEquals(1, storageResources.size());
    }

    @Test
    public void getAllStorageObjectsThatHasEnvironmentBeta() throws Exception {
        storageAccountRepository.add(storageResource1);
        storageAccountRepository.add(storageResource2);
        storageAccountRepository.add(storageResource3);

        List<StorageResource> storageResources = (List<StorageResource>) storageAccountRepository.getStorageResourcesByEnvironment("beta");

        assertEquals(1, storageResources.size());
    }

    @Test
    public void testRefresh() {
        StorageResourceRepository storageResourceRepository = new StorageResourceRepository(null, null);
        StorageResourceRepository spy = spy(storageResourceRepository);
        spy.refresh("alpha");
    }

    @Test
    public void testLoadStorageResources() {
        Collection<StorageResource> storageResources = storageAccountRepository.getAll();
        storageResources.forEach(storageResource -> {
            storageAccountRepository.loadStorageResources();
        });
    }
}

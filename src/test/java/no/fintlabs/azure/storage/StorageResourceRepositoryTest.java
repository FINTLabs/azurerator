package no.fintlabs.azure.storage;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StorageResourceRepositoryTest {
    StorageResourceRepository storageResourceRepository = new StorageResourceRepository(null, null);
    StorageResource storageResourceMock1 = mock(StorageResource.class);
    StorageResource storageResourceMock2 = mock(StorageResource.class);
    StorageResource storageResourceMock3 = mock(StorageResource.class);

    @Test
    public void testAddingStorageResourceIncreasesRepositorySizeByOne() {
        long beforeSize = storageResourceRepository.size("alpha");
        when(storageResourceMock1.getEnvironment()).thenReturn("alpha");
        storageResourceRepository.add(storageResourceMock1);
        assert storageResourceRepository.size("alpha") == beforeSize + 1.0;
    }

    @Test
    public void testRemovingStorageResourceDecreasesRepositorySizeByOne() {
        when(storageResourceMock1.getEnvironment()).thenReturn("alpha");
        storageResourceRepository.add(storageResourceMock1);
        storageResourceRepository.remove(storageResourceMock1);
        assert storageResourceRepository.size("alpha") == 0.0;
    }

    @Test
    public void whenUpdatingStorageResourceThenItShouldBeReflectedInRepository() {
        when(storageResourceMock1.getEnvironment()).thenReturn("beta");
        storageResourceRepository.update(storageResourceMock1);
        assertEquals("beta", storageResourceRepository.get(storageResourceMock1.getStorageAccountName()).get().getEnvironment());
    }

    @Test
    public void checkingIfANonexistentStorageResourceExistsShouldReturnFalse() {
        boolean exists = storageResourceRepository.exists("test");
        assertFalse(exists);
    }

    @Test
    public void checkingIfAStorageResourceExistsShouldReturnTrue() {
        when(storageResourceMock1.getStorageAccountName()).thenReturn("test-storage-account-1");
        storageResourceRepository.add(storageResourceMock1);
        boolean exists = storageResourceRepository.exists("test-storage-account-1");
        assertTrue(exists);
    }

    @Test
    public void getAllStorageObjectsThatHasEnvironmentAlpha() {
        when(storageResourceMock1.getStorageAccountName()).thenReturn("test-storage-account-1");
        when(storageResourceMock1.getEnvironment()).thenReturn("alpha");
        when(storageResourceMock2.getStorageAccountName()).thenReturn("test-storage-account-2");
        when(storageResourceMock2.getEnvironment()).thenReturn("beta");
        when(storageResourceMock3.getStorageAccountName()).thenReturn("test-storage-account-3");
        when(storageResourceMock3.getEnvironment()).thenReturn("alpha");
        storageResourceRepository.add(storageResourceMock1);
        storageResourceRepository.add(storageResourceMock2);
        storageResourceRepository.add(storageResourceMock3);
        Collection<StorageResource> storageResources = storageResourceRepository.getStorageResourcesByEnvironment("alpha");
        assertEquals(2, storageResources.size());
    }

    @Test
    public void testRefresh() {
        StorageResourceRepository spy = spy(storageResourceRepository);
        spy.refresh("alpha");
        StorageResourceRepository spyAfterRefresh = spy(spy);
        assertNotEquals(spy, spyAfterRefresh);
    }


    @Test
    public void testLoadStorageResources() {
        Collection<StorageResource> storageResources = storageResourceRepository.getAll();
        storageResources.forEach(storageResource -> {
            storageResourceRepository.loadStorageResources();
        });
    }
}

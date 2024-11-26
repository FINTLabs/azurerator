package no.fintlabs.azure.storage.blob;

import no.fintlabs.azure.storage.StorageResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StorageResourceTest {

    @Test
    public void testDesiredPathNotNull() {
        StorageResource resource = StorageResource.desired();
        assertNotNull(resource.getPath(), "Path should not be null");
    }

    @Test
    public void testDesiredPathNotEmpty() {
        StorageResource resource = StorageResource.desired();
        assertNotEquals("", resource.getPath(), "Path should not be empty");
    }

    @Test
    public void testDesiredPathUnique() {
        String path1 = StorageResource.desired().getPath();
        String path2 = StorageResource.desired().getPath();
        assertNotEquals(path1, path2, "Path should be unique");
    }
}



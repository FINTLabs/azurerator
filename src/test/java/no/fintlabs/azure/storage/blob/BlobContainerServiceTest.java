package no.fintlabs.azure.storage.blob;


import com.azure.resourcemanager.storage.fluent.ManagementPoliciesClient;
import com.azure.resourcemanager.storage.fluent.StorageManagementClient;
import com.azure.resourcemanager.storage.fluent.models.ManagementPolicyInner;
import com.azure.resourcemanager.storage.models.*;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import no.fintlabs.azure.storage.StorageAccountService;
import no.fintlabs.azure.storage.StorageResource;
import no.fintlabs.azure.storage.StorageResourceRepository;
import no.fintlabs.azure.storage.StorageType;
import com.azure.resourcemanager.storage.StorageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BlobContainerServiceTest {

    @Mock
    private StorageAccountService storageAccountService;

    @InjectMocks
    private BlobContainerService blobContainerService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StorageAccount storageAccount;

    @Mock
    private StorageAccounts storageAccounts;

    @Mock
    private StorageManagementClient storageManagementClient;

    @Mock
    private ManagementPoliciesClient managementPoliciesClient;

    @Mock
    private StorageManager storageManager;

    @Mock
    private BlobContainer blobContainer;

    private StorageResource desired = new StorageResource();

    private BlobContainerCrd crd = new BlobContainerCrd();

    @Test
    public void testAdd() throws Exception {

        desired = StorageResource.builder()
                .storageAccountName("storageAccountName")
                .resourceGroup("resourceGroup")
                .connectionString("connectionString")
                .status("status")
                .team("team")
                .orgId("orgId")
                .portalUri("portalUri")
                .environment("environment")
                .path("path")
                .type(StorageType.BLOB_CONTAINER)
                .crdName("crdName")
                .crdNamespace("crdNamespace")
                .instance("instance")
                .partOf("partOf")
                .lifespanDays("30")
                .build();

        crd = new BlobContainerCrd();
        crd.setMetadata(new ObjectMeta());
        crd.getMetadata().setName("crdName");
        crd.getMetadata().setNamespace("crdNamespace");

        BlobContainers blobContainers = mock(BlobContainers.class);
        BlobContainer.DefinitionStages.Blank blank = mock(BlobContainer.DefinitionStages.Blank.class);
        BlobContainer.DefinitionStages.WithPublicAccess withPublicAccess = mock(BlobContainer.DefinitionStages.WithPublicAccess.class);
        BlobContainer.DefinitionStages.WithCreate withCreate = mock(BlobContainer.DefinitionStages.WithCreate.class);

        when(storageAccountService.add(any(BlobContainerCrd.class), anyString(), eq(StorageType.BLOB_CONTAINER)))
                .thenReturn(storageAccount);
        when(storageAccount.manager()).thenReturn(storageManager);
        when(storageManager.blobContainers()).thenReturn(blobContainers);
        when(blobContainers.defineContainer(anyString())).thenReturn(blank);
        when(blank.withExistingStorageAccount(any(StorageAccount.class))).thenReturn(withPublicAccess);
        when(withPublicAccess.withPublicAccess(any(PublicAccess.class))).thenReturn(withCreate);
        when(withCreate.create()).thenReturn(blobContainer);

        when(storageManager.storageAccounts()).thenReturn(storageAccounts);
        when(storageAccounts.manager()).thenReturn(storageManager);
        when(storageManager.serviceClient()).thenReturn(storageManagementClient);
        when(storageManagementClient.getManagementPolicies()).thenReturn(managementPoliciesClient);

        StorageResource result = blobContainerService.add(desired, crd);

        verify(storageAccountService).add(any(BlobContainerCrd.class), eq("path"), eq(StorageType.BLOB_CONTAINER));
        verify(storageManager).blobContainers();
        verify(withCreate).create();

        assertNotNull(result);
        assertEquals(StorageType.BLOB_CONTAINER, result.getType());
        assertEquals("path", result.getPath());
    }

    @Test
    public void testSetLifecycleRules() {
        // Set StorageManager
        when(storageManager.storageAccounts()).thenReturn(storageAccounts);
        when(storageAccounts.manager()).thenReturn(storageManager);
        when(storageManager.serviceClient()).thenReturn(storageManagementClient);
        when(storageManagementClient.getManagementPolicies()).thenReturn(managementPoliciesClient);

        String resourceGroupName = "resourceGroupName";
        String storageAccountName = "storageAccountName";
        String containerName = "containerName";
        float lifespanDays = 30f;
        blobContainerService.setLifecycleRules(storageManager, resourceGroupName, storageAccountName, containerName, lifespanDays);

        verify(managementPoliciesClient).createOrUpdate(eq(resourceGroupName), eq(storageAccountName), eq(ManagementPolicyName.DEFAULT), any(ManagementPolicyInner.class));
    }
}

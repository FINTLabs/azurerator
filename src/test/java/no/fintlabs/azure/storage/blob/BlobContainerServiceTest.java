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

    @Mock
    private StorageResourceRepository storageResourceRepository;

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





//
//import com.azure.core.management.profile.AzureProfile;
//import com.azure.resourcemanager.AzureResourceManager;
//import com.azure.resourcemanager.storage.StorageManager;
//import com.azure.resourcemanager.storage.fluent.ManagementPoliciesClient;
//import com.azure.resourcemanager.storage.fluent.models.ManagementPolicyInner;
//import com.azure.resourcemanager.storage.models.*;
//import no.fintlabs.azure.AzureConfiguration;
//import no.fintlabs.azure.storage.StorageAccountService;
//import no.fintlabs.azure.storage.StorageResource;
//import no.fintlabs.azure.storage.StorageResourceRepository;
//import no.fintlabs.azure.storage.StorageType;
//import org.junit.Before;
//import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.verify;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({AzureResourceManager.class, BlobContainerService.class})
//public class BlobContainerServiceTest {
//    @Mock
//    private StorageAccountService storageAccountService;
//    @Mock
//    private StorageResourceRepository storageResourceRepository;
//    @Mock
//    private AzureConfiguration azureConfiguration;
//
//    @Mock
//    private AzureResourceManager.Configurable configurable;
//    @Mock
//    private AzureResourceManager.Authenticated authenticated;
//    @Mock
//    private AzureResourceManager azureResourceManager;
//    @Mock
//    private StorageAccount storageAccount;
//    @Mock
//    private StorageManager storageManager;
//    @Mock
//    private BlobContainers blobContainers;
//    @Mock
//    private BlobContainer blobContainer;
//    @Mock
//    private ManagementPoliciesClient managementPoliciesClient;
//
//    @Mock
//    private BlobContainer.DefinitionStages.Blank blobContainerBlank;
//    @Mock
//    private BlobContainer.DefinitionStages.WithPublicAccess blobContainerWithPublicAccess;
//    @Mock
//    private BlobContainer.DefinitionStages.WithCreate blobContainerWithCreate;
//
//    private BlobContainerService blobContainerService;
//    private BlobContainerCrd crd;
//
//    @Before
//    public void setup() throws Exception {
//        try {
//            MockitoAnnotations.openMocks(this);
//
//            PowerMockito.mockStatic(AzureResourceManager.class);
//            PowerMockito.when(AzureResourceManager.configure()).thenReturn(configurable);
//            PowerMockito.when(configurable.withLogLevel(any())).thenReturn(configurable);
//            PowerMockito.when(configurable.authenticate(any(), any(AzureProfile.class))).thenReturn(authenticated);
//            PowerMockito.when(authenticated.withSubscription(anyString())).thenReturn(azureResourceManager);
//
//            System.out.println("Mock setup for AzureResourceManager done");
//
//            PowerMockito.when(storageAccountService.add(any(), anyString(), any())).thenReturn(storageAccount);
//            PowerMockito.when(storageAccount.resourceGroupName()).thenReturn("testResourceGroup");
//            PowerMockito.when(storageAccount.name()).thenReturn("testStorageAccount");
//            PowerMockito.when(storageAccount.manager()).thenReturn(storageManager);
//            PowerMockito.when(storageManager.blobContainers()).thenReturn(blobContainers);
//            PowerMockito.when(blobContainers.defineContainer(anyString())).thenReturn(blobContainerBlank);
//            PowerMockito.when(blobContainerBlank.withExistingStorageAccount(any(StorageAccount.class))).thenReturn(blobContainerWithPublicAccess);
//            PowerMockito.when(blobContainerWithPublicAccess.withPublicAccess(any(PublicAccess.class))).thenReturn(blobContainerWithCreate);
//            PowerMockito.when(blobContainerWithCreate.create()).thenReturn(blobContainer);
//            PowerMockito.when(blobContainer.name()).thenReturn("testContainer");
//            PowerMockito.when(storageManager.serviceClient().getManagementPolicies()).thenReturn(managementPoliciesClient);
//
//            assertNotNull("StorageAccount is null!", storageAccount);
//            System.out.println("StorageAccount mock is set up correctly");
//
//            blobContainerService = new BlobContainerService(storageAccountService, storageResourceRepository, azureConfiguration);
//            crd = new BlobContainerCrd();
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw e;
//        }
//    }
//
//    @Test
//    public void testAddWithLifecyclePolicy() {
//        assertNotNull("StorageAccount is null at start of the test!", storageAccount);
//
//        StorageResource desired = StorageResource.of(storageAccount, "testContainer", StorageType.BLOB_CONTAINER);
//        PowerMockito.when(desired.getLifespanDays()).thenReturn("30");
//
//        assertNotNull("Desired storage resource is null!", desired);
//
//        StorageResource result = blobContainerService.add(desired, crd);
//
//        assertNotNull(result);
//        assertEquals("testContainer", result.getPath());
//        assertEquals(StorageType.BLOB_CONTAINER, result.getType());
//
//        verify(blobContainers).defineContainer("testContainer");
//        verify(blobContainerBlank).withExistingStorageAccount(storageAccount);
//        verify(blobContainerWithPublicAccess).withPublicAccess(PublicAccess.NONE);
//        verify(blobContainerWithCreate).create();
//
//        verify(managementPoliciesClient).createOrUpdate(eq("testResourceGroup"), eq("testStorageAccount"), eq(ManagementPolicyName.DEFAULT), any(ManagementPolicyInner.class));
//    }
//}

package no.fintlabs.azure.storage.blob;

import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.ManagementPoliciesClient;
import com.azure.resourcemanager.storage.fluent.models.ManagementPolicyInner;
import com.azure.resourcemanager.storage.models.*;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import no.fintlabs.azure.storage.StorageAccountService;
import no.fintlabs.azure.storage.StorageResource;
import no.fintlabs.azure.storage.StorageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static no.fintlabs.MetadataUtils.LABEL_ORG_ID;
import static no.fintlabs.MetadataUtils.LABEL_TEAM;
import static no.fintlabs.azure.TagNames.TAG_LIFESPAN_DAYS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BlobContainerServiceTest {

    @Mock
    private StorageAccountService storageAccountService;

    @InjectMocks
    private BlobContainerService blobContainerService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StorageAccount storageAccount;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StorageManager storageManager;

    @Mock
    private BlobContainer blobContainer;

    // Mocks for Blob Container definition stages
    @Mock
    private BlobContainers blobContainers;

    @Mock
    private BlobContainer.DefinitionStages.Blank blobContainerBlank;

    @Mock
    private BlobContainer.DefinitionStages.WithPublicAccess blobContainerWithPublicAccess;

    @Mock
    private BlobContainer.DefinitionStages.WithCreate blobContainerWithCreate;

    // Mocks for Management Policies
    @Mock
    private ManagementPoliciesClient managementPoliciesClient;

    private BlobContainerCrd crd;
    private StorageResource desired;

    @BeforeEach
    public void setup() {
        // Common storage account setup
        lenient().when(storageAccount.name()).thenReturn("test-account");
        lenient().when(storageAccount.resourceGroupName()).thenReturn("test-rg");
        lenient().when(storageAccount.manager()).thenReturn(storageManager);

        // Storage Manager mocks
        lenient().when(storageManager.blobContainers()).thenReturn(blobContainers);
        lenient().when(storageManager.serviceClient().getManagementPolicies()).thenReturn(managementPoliciesClient);

        // Blob Containers mock setup
        lenient().when(blobContainers.defineContainer(anyString())).thenReturn(blobContainerBlank);
        lenient().when(blobContainerBlank.withExistingStorageAccount(any(StorageAccount.class))).thenReturn(blobContainerWithPublicAccess);
        lenient().when(blobContainerWithPublicAccess.withPublicAccess(any(PublicAccess.class))).thenReturn(blobContainerWithCreate);
        lenient().when(blobContainerWithCreate.create()).thenReturn(blobContainer);

        // Setup common CRD and desired resource
        crd = createBlobContainerCrd();
        desired = StorageResource.desired();
    }

    @Test
    public void shouldCreateStorageAccountWithContainer() {
        when(storageAccountService.add(any(BlobContainerCrd.class), anyString(), eq(StorageType.BLOB_CONTAINER), eq(null)))
                .thenReturn(storageAccount);

        // Call the method under test
        StorageResource result = blobContainerService.add(desired, crd);

        // Verify the correct method was called
        verify(blobContainerWithCreate).create();

        // Assertions
        assertNotNull(result);
        assertEquals(StorageType.BLOB_CONTAINER, result.getType());
        assertNotNull(result.getPath());
    }

    @Test
    public void shouldCreateStorageAccountWithContainerAndLifecycle() {
        crd.getSpec().setLifespanDays(30);

        Map<String, String> tags = Map.of(TAG_LIFESPAN_DAYS, String.valueOf(30));
        when(storageAccountService.add(any(BlobContainerCrd.class), anyString(), eq(StorageType.BLOB_CONTAINER), eq(tags)))
                .thenReturn(storageAccount);

        ArgumentCaptor<ManagementPolicyInner> policyCaptor = ArgumentCaptor.forClass(ManagementPolicyInner.class);
        when(managementPoliciesClient.createOrUpdate(eq("test-rg"), eq("test-account"), eq(ManagementPolicyName.DEFAULT), policyCaptor.capture()))
                .thenReturn(mock(ManagementPolicyInner.class));

        StorageResource result = blobContainerService.add(desired, crd);

        verify(blobContainerWithCreate).create();
        verify(managementPoliciesClient).createOrUpdate(eq("test-rg"), eq("test-account"), eq(ManagementPolicyName.DEFAULT), any(ManagementPolicyInner.class));

        assertNotNull(result);
        assertEquals(StorageType.BLOB_CONTAINER, result.getType());
        assertNotNull(result.getPath());

        ManagementPolicyInner capturedPolicy = policyCaptor.getValue();
        assertNotNull(capturedPolicy);
        ManagementPolicyRule policyRule = capturedPolicy.policy().rules().get(0);
        assertNotNull(policyRule);
        assertEquals("DeleteOldBlobs", policyRule.name());
        assertTrue(policyRule.enabled());

        ManagementPolicyDefinition policyDefinition = policyRule.definition();
        assertNotNull(policyDefinition);
        assertEquals(30, policyDefinition.actions().baseBlob().delete().daysAfterModificationGreaterThan());

        ManagementPolicyFilter policyFilter = policyDefinition.filters();
        assertNotNull(policyFilter);
        assertEquals(result.getPath() + "/", policyFilter.prefixMatch().get(0));
        assertEquals("blockBlob", policyFilter.blobTypes().get(0));
    }

    @Test
    public void testDelete() {
        blobContainerService.delete(desired);
        verify(storageAccountService).delete(desired);
    }

    // Helper method to set up common mocked objects
    private BlobContainerCrd createBlobContainerCrd() {
        BlobContainerCrd crd = new BlobContainerCrd();
        crd.setMetadata(
                new ObjectMetaBuilder()
                        .withName("test-name")
                        .withNamespace("test-namespace")
                        .withLabels(Map.of(
                                LABEL_ORG_ID, "test-org",
                                LABEL_TEAM, "test-team"
                        ))
                        .build());
        crd.setSpec(new BlobContainerSpec());
        return crd;
    }
}

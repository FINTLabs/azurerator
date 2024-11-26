package no.fintlabs.azure.storage.blob;

import com.azure.core.management.Region;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.*;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;
import no.fintlabs.FlaisCrd;
import no.fintlabs.Props;
import no.fintlabs.azure.AzureConfiguration;
import no.fintlabs.azure.AzureSpec;
import no.fintlabs.azure.storage.StorageAccountService;
import no.fintlabs.azure.storage.StorageResource;
import no.fintlabs.azure.storage.StorageResourceRepository;
import no.fintlabs.azure.storage.StorageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Map;

import static no.fintlabs.MetadataUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StorageAccountServiceTest {
    @InjectMocks
    private StorageAccountService storageAccountService;

    @Mock
    private StorageManager storageManager;

    @Mock
    private StorageResourceRepository storageResourceRepository;

    @Mock
    private AzureConfiguration azureConfiguration;

    // Mocks for the method chain
    @Mock
    private StorageAccounts storageAccounts;

    @Mock
    private StorageAccount.DefinitionStages.Blank storageAccountBlank;

    @Mock
    private StorageAccount.DefinitionStages.WithGroup storageAccountWithGroup;

    @Mock
    private StorageAccount.DefinitionStages.WithCreate storageAccountWithCreate;

    @Mock
    CheckNameAvailabilityResult checkNameAvailabilityResult;

    @Mock
    private StorageAccount storageAccount;

    @ResourceLock("Props")
    @Test
    public void testAdd() {
        var crd = createStorageContainerCrd();
        var accountNameCaptor = ArgumentCaptor.forClass(String.class);
        var regionCaptor = ArgumentCaptor.forClass(Region.class);
        var resourceGroupCaptor = ArgumentCaptor.forClass(String.class);
        var skuCaptor = ArgumentCaptor.forClass(StorageAccountSkuType.class);
        var tagsCaptor = ArgumentCaptor.forClass(Map.class);

        var storageResourceCaptor = ArgumentCaptor.forClass(StorageResource.class);

        var props = mockStatic(Props.class);
        props.when(Props::getEnvironment).thenReturn("test-environment");

        // Mock azureConfiguration
        when(azureConfiguration.getStorageAccountResourceGroup()).thenReturn("test-rg");

        // Set up mocks for the method chain
        when(storageManager.storageAccounts()).thenReturn(storageAccounts);
        when(storageAccounts.define(accountNameCaptor.capture())).thenReturn(storageAccountBlank);
        when(storageAccountBlank.withRegion(regionCaptor.capture())).thenReturn(storageAccountWithGroup);
        when(storageAccountWithGroup.withExistingResourceGroup(resourceGroupCaptor.capture())).thenReturn(storageAccountWithCreate);
        when(storageAccountWithCreate.withGeneralPurposeAccountKindV2()).thenReturn(storageAccountWithCreate);
        when(storageAccountWithCreate.withSku(skuCaptor.capture())).thenReturn(storageAccountWithCreate);
        when(storageAccountWithCreate.disableBlobPublicAccess()).thenReturn(storageAccountWithCreate);
        when(storageAccountWithCreate.withTags(tagsCaptor.capture())).thenReturn(storageAccountWithCreate);
        when(storageAccountWithCreate.create()).thenReturn(storageAccount);

        // Mock the storage account creation
        when(storageAccount.name()).thenAnswer(invocation -> accountNameCaptor.getValue());
        when(storageAccount.resourceGroupName()).thenAnswer(invocation -> resourceGroupCaptor.getValue());
        when(storageAccount.getKeys()).thenReturn(List.of(new StorageAccountKey()));
        when(storageAccount.accountStatuses()).thenReturn(new AccountStatuses(AccountStatus.fromString("available"), null));
        when(storageAccount.tags()).thenAnswer(invocation -> tagsCaptor.getValue());

        when(storageResourceRepository.exists(anyString())).thenReturn(true);
        doNothing().when(storageResourceRepository).add(storageResourceCaptor.capture());

        when(storageAccounts.checkNameAvailability(anyString())).thenReturn(checkNameAvailabilityResult);
        when(checkNameAvailabilityResult.isAvailable()).thenReturn(true);

        storageAccountService.add(crd, "test-path", StorageType.UNKNOWN, Map.of("test-key", "test-value"));

        verify(storageResourceRepository).add(any(StorageResource.class));
        verify(storageResourceRepository).exists(accountNameCaptor.getValue());

        var storageResource = storageResourceCaptor.getValue();
        assertNotNull(storageResource);
        assertEquals(StorageType.UNKNOWN, storageResource.getType());
        assertEquals(resourceGroupCaptor.getValue(), storageResource.getResourceGroup());
        assertEquals(accountNameCaptor.getValue(), storageResource.getStorageAccountName());
        assertEquals("AVAILABLE", storageResource.getStatus());
        assertEquals("test-team", storageResource.getTeam());
        assertEquals("test-org", storageResource.getOrgId());
        assertEquals("test-name", storageResource.getCrdName());
        assertEquals("test-namespace", storageResource.getCrdNamespace());
        assertEquals("test-path", storageResource.getPath());
        assertEquals("test-environment", storageResource.getEnvironment());
        assertEquals("test-value", storageResource.getAdditionalTags().get("test-key"));

        props.close();
    }

    @Test
    public void testDelete() {
        var storageResource = new StorageResource();
        storageResource.setStorageAccountName("test-name");
        storageResource.setResourceGroup("test-rg");

        when(storageManager.storageAccounts()).thenReturn(storageAccounts);
        doNothing().when(storageAccounts).deleteByResourceGroup(storageResource.getResourceGroup(), storageResource.getStorageAccountName());
        doNothing().when(storageResourceRepository).remove(storageResource);

        storageAccountService.delete(storageResource);

        verify(storageAccounts).deleteByResourceGroup(storageResource.getResourceGroup(), storageResource.getStorageAccountName());
        verify(storageResourceRepository).remove(storageResource);
    }

    @Test
    public void testGet() {
        var crd = createStorageContainerCrd();
        crd.getMetadata().getAnnotations().put(ANNOTATION_STORAGE_ACCOUNT_NAME, "test-account-name");

        when(azureConfiguration.getStorageAccountResourceGroup()).thenReturn("test-rg");

        when(storageResourceRepository.exists(eq("test-account-name"))).thenReturn(true);

        when(storageManager.storageAccounts()).thenReturn(storageAccounts);
        when(storageAccounts.getByResourceGroup(eq("test-rg"), eq("test-account-name"))).thenReturn(storageAccount);

        var account = storageAccountService.getStorageAccount(crd);

        verify(storageResourceRepository).exists("test-account-name");
        verify(storageAccounts).getByResourceGroup("test-rg", "test-account-name");

        assertNotNull(account);
    }

    private DummyStorageAccountCrd createStorageContainerCrd() {
        var crd = new DummyStorageAccountCrd();
        crd.setMetadata(
                new ObjectMetaBuilder()
                        .withName("test-name")
                        .withNamespace("test-namespace")
                        .withLabels(Map.of(
                                LABEL_ORG_ID, "test-org",
                                LABEL_TEAM, "test-team"
                        ))
                        .build());
        return crd;
    }

    @Group("fintlabs.no")
    @Version("v1alpha1")
    @Kind("DummyStorageAccount")
    private static class DummyStorageAccountCrd extends FlaisCrd<AzureSpec> {
    }
}

package no.fintlabs.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.StorageManagementClient;
import com.azure.resourcemanager.storage.implementation.StorageManagementClientBuilder;
import com.azure.resourcemanager.storage.implementation.StorageManagementClientImpl;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfiguration {


    @Bean
    public StorageManager createStorageMananger() {
    AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
    TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();

        return StorageManager
                .authenticate(credential, profile);

    }
}

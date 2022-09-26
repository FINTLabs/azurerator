package no.fintlabs.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class AzureConfiguration {


    @Bean
    public StorageManager createStorageMananger() {
    AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
    TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();
    StorageManager manager = StorageManager
            .authenticate(credential, profile);

//        AzureResourceManager azureResourceManager = AzureResourceManager
//                .configure()
//                .withLogLevel(HttpLogDetailLevel.BASIC)
//                .authenticate(credential, profile)
//                .withDefaultSubscription();


        return manager;

    }
}

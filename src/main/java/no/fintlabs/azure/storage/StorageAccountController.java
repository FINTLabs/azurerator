package no.fintlabs.azure.storage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collection;

@RestController
@RequestMapping("api/flais/operators/azurerator")
public class StorageAccountController {

    private final StorageResourceRepository storageResourceRepository;

    public StorageAccountController(StorageResourceRepository storageResourceRepository) {
        this.storageResourceRepository = storageResourceRepository;
    }

    @GetMapping("storage-accounts")
    public Mono<Collection<StorageResource>> getStorageAccounts() {
        return Mono.justOrEmpty(storageResourceRepository.getAll());
    }
}

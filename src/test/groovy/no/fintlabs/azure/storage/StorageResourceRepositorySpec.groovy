package no.fintlabs.azure.storage

import com.azure.resourcemanager.storage.StorageManager
import no.fintlabs.azure.AzureConfiguration
import spock.lang.Specification

class StorageResourceRepositorySpec extends Specification {

    def storageAccountRepository

    void setup() {
        storageAccountRepository = new StorageResourceRepository(GroovyMock(StorageManager.class), Stub(AzureConfiguration))
    }

    def "When adding a storage resource the size of the repository should increase by 1"() {
        given:
        def azureStorageObject = StorageResource.builder().storageAccountName("test-storage-account-1").build()
        def beforeSize = storageAccountRepository.size()

        when:
        storageAccountRepository.add(azureStorageObject)

        then:
        storageAccountRepository.size() == beforeSize + 1
    }

    def "When deleting a storage resource the size of the repository should decrease by 1"() {
        given:
        def azureStorageObject = StorageResource.builder().storageAccountName("test-storage-account-1").build()
        storageAccountRepository.add(azureStorageObject)
        def beforeSize = storageAccountRepository.size()

        when:
        storageAccountRepository.remove(azureStorageObject)

        then:
        storageAccountRepository.size() == beforeSize - 1

    }

    def "When updating a storage resource the it should be reflected in the repository"() {
        given:
        def azureStorageObject = StorageResource.builder().storageAccountName("test-storage-account-1").build()
        storageAccountRepository.add(azureStorageObject)

        when:
        azureStorageObject.setEnvironment("test")
        def storageResource = storageAccountRepository.get(azureStorageObject.getStorageAccountName())

        then:
        storageResource.isPresent()
        storageResource.get().getEnvironment() == "test"
    }

    def "Checking if a none existing storage resource exists should return false"() {
        when:
        def exists = storageAccountRepository.exists("test")

        then:
        !exists
    }

    def "Checking if a exsiting storage resource exists should return true"() {
        given:
        def azureStorageObject = StorageResource.builder().storageAccountName("test-storage-account-1").build()
        storageAccountRepository.add(azureStorageObject)

        when:
        def exists = storageAccountRepository.exists("test-storage-account-1")

        then:
        exists
    }
}

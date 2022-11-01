# FLAIS Azurerator

<!-- TOC -->
* [FLAIS Azurerator](#flais-azurerator)
* [What does the operator do?](#what-does-the-operator-do)
* [Setup](#setup)
* [Usage](#usage)
  * [AzureBlobContainer](#azureblobcontainer)
    * [Specification parameters](#specification-parameters)
    * [Secret properties](#secret-properties)
  * [AzureFileShare](#azurefileshare)
    * [Specification parameters](#specification-parameters)
    * [Secret properties](#secret-properties)
<!-- TOC -->

This operator provides the ability integrate Azure with Kubernetes.

This Operator manages `AzureBlobContainer` and `AzureFileShare` Custom Resource Definitions (CRDs).

The `AzureBlobContainer` CRD, when created, will be used to create a Storage Account and a blob container in Azure
and compose a Kubernetes Secret containing the connection string and the blob container name.

The `AzureFileShare` CRD, when created, will be used to create a Storage Account and a file share in Azure
and compose a Kubernetes Secret containing the connection string and the file share name.

# What does the operator do?

When a `AzureBlobContainer` or `AzureFileShare` CRD is **created** the operator:

- Creates a storage account in Azure with the CRD name, normalized to fit the Azure storage account naming standards.
- Creates a blob container/file share with the name of the CRD in the created storage account.

When a `AzureBlobContainer` or `AzureFileShare` CRD is **updated** the operator:

- Nothing happends. If you like to modify it you need to delete it and re-create it.

When a `AzureBlobContainer` or `AzureFileShare` CRD is **deleted** the operator:

- Deletes the storage account in Azure.
- Deletes the secret.

# Setup

TODO

# Usage

## AzureBlobContainer

```yaml
apiVersion: fintlabs.no/v1alpha1
kind: AzureBlobContainer
metadata:
  name: arkiv-adapter
  labels:
    app.kubernetes.io/name: arkiv-adapter
    app.kubernetes.io/instance: arkiv-adapter_rogfk_no
    app.kubernetes.io/version: latest
    app.kubernetes.io/component: adapter
    app.kubernetes.io/part-of: arkiv
    fintlabs.no/team: flais
```
### Specification parameters

| Parameters    | Default value |
|:--------------|:--------------|
| resourceGroup | `rg-storage`  |

### Secret properties

| Property                                     | Description                       |
|----------------------------------------------|-----------------------------------|
| fint.azure.storage-account.connection-string | Storage account connection string |
| fint.azure.storage.container-blob.name       | Name of container blob            |

## AzureFileShare

```yaml
apiVersion: fintlabs.no/v1alpha1
kind: AzureFileShare
metadata:
  name: fint3-file-share-test
  labels:
    app.kubernetes.io/name: arkiv-adapter
    app.kubernetes.io/instance: arkiv-adapter_rogfk_no
    app.kubernetes.io/version: latest
    app.kubernetes.io/component: adapter
    app.kubernetes.io/part-of: arkiv
    fintlabs.no/team: flais
```

### Specification parameters

| Parameters    | Default value |
|:--------------|:--------------|
| resourceGroup | `rg-storage`  |

### Secret properties

| Property                                     | Description                       |
|----------------------------------------------|-----------------------------------|
| fint.azure.storage-account.connection-string | Storage account connection string |
| fint.azure.storage-account.file-share.name   | Name of file share                |

package com.indigo.synapse.file.storage;

import java.util.Optional;

public interface FileStorage {

    FileObject store(StoreFileCommand command);

    Optional<StoredFile> load(String bucket, String objectKey);

    boolean delete(String bucket, String objectKey);
}

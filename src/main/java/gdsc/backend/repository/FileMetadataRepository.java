package gdsc.backend.repository;

import gdsc.backend.domain.FileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetaData, Long> {
}


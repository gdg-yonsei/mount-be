package com.gdsc.mount.directory.repository;

import com.gdsc.mount.directory.domain.Directory;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectoryRepository extends MongoRepository<Directory, String> {
    boolean existsByPathIncludingDirectory(String pathIncludingDirectory);
    Optional<Directory> findByPathIncludingDirectory(String pathIncludingDirectory);
}

package com.gdsc.mount.directory.repository;

import com.gdsc.mount.directory.domain.Directory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DirectoryRepository extends MongoRepository<Directory, String> {
}

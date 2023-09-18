package com.gdsc.mount.metadata.repository;

import com.gdsc.mount.metadata.domain.Metadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MetadataRepository extends MongoRepository<Metadata, String> {
    List<Metadata> findAllByName(String name);
    Optional<Metadata> findByPath(String path);
}

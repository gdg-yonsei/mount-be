package com.gdsc.mount.metadata.repository;

import com.gdsc.mount.metadata.domain.Metadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MetadataRepository extends MongoRepository<Metadata, String> {
}

package com.gdsc.mount.metadata.repository;

import com.gdsc.mount.metadata.domain.Metadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MetadataRepository extends MongoRepository<Metadata, String> {
    List<Metadata> findAllByName(String name);
}

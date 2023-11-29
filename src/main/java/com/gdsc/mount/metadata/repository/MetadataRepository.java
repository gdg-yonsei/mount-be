package com.gdsc.mount.metadata.repository;

import com.gdsc.mount.metadata.domain.Metadata;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MetadataRepository extends MongoRepository<Metadata, String>, PagingAndSortingRepository<Metadata, String> {
    List<Metadata> findAllByName(String name);
    Optional<Metadata> findByPathWithFile(String path);
    boolean existsByPathWithFile(String path);
    Page<Metadata> findAllByPathWithoutFile(String path, Pageable pageable);

    void deleteByPathWithFile(String path);
}

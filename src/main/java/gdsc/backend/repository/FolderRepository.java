package gdsc.backend.repository;

import gdsc.backend.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    Optional<Folder> findById(Long id);
    Optional<Folder> findByNameAndParentId(String name, Long parentId);
}

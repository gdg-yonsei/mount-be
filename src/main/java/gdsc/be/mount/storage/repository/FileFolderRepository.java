package gdsc.be.mount.storage.repository;

import gdsc.be.mount.storage.entity.FileFolder;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FileFolderRepository extends JpaRepository<FileFolder, Long> {
    boolean existsByOriginalNameAndParentId(String folderName, Long parentId);
}

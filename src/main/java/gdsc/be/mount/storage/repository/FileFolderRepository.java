package gdsc.be.mount.storage.repository;

import gdsc.be.mount.storage.dto.response.FolderInfoResponse;
import gdsc.be.mount.storage.entity.FileFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface FileFolderRepository extends JpaRepository<FileFolder, Long> {
    boolean existsByOriginalNameAndParentId(String folderName, Long parentId);

    @Query("SELECT ff FROM FileFolder ff WHERE ff.id IN :childIds")
    List<FileFolder> findChildrenByChildIds(List<Long> childIds);

}

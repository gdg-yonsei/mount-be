package gdsc.be.mount.storage.repository;

import gdsc.be.mount.storage.Enum.FileFolderType;
import gdsc.be.mount.storage.entity.FileFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface FileFolderRepository extends JpaRepository<FileFolder, Long> {
    boolean existsByUserNameAndOriginalName(String userName, String folderName);

    @Query("SELECT ff FROM FileFolder ff WHERE ff.id IN :childIds")
    List<FileFolder> findChildrenByChildIds(List<Long> childIds);

    @Query("SELECT ff FROM FileFolder ff WHERE ff.id = :fileId AND ff.fileFolderType = :type AND ff.userName = :userName")
    Optional<FileFolder> findByIdAndTypeAndUserName(Long fileId, FileFolderType type, String userName);
}

package gdsc.backend.repository;

import gdsc.backend.domain.FileMetaData;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FileMetadataRepository {

    private final EntityManager em;

    public void save(FileMetaData fileMetaData) {
        if (fileMetaData.getId() == null) {
            em.persist(fileMetaData);
        } else {
            em.merge(fileMetaData);
        }
    }

    public FileMetaData findByUuidAndUserId(String uuid, String userId) {
        return em.createQuery("select f from FileMetaData f where f.uuid = :uuid and f.userId = :userId", FileMetaData.class)
                .setParameter("uuid", uuid)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public FileMetaData findByFileId(String fileId) {
        return em.createQuery("select f from FileMetaData f where f.fileId = :fileId", FileMetaData.class)
                .setParameter("fileId", fileId)
                .getSingleResult();
    }
}

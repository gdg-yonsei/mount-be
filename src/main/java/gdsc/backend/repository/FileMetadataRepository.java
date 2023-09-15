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

    public FileMetaData findByFileId(Long fileId) {
        return em.createQuery("select f from FileMetaData f where f.id = :fileId", FileMetaData.class)
                .setParameter("fileId", fileId)
                .getSingleResult();
    }
}

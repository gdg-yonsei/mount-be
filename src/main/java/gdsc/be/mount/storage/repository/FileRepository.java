package gdsc.be.mount.storage.repository;

import gdsc.be.mount.storage.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FileRepository extends JpaRepository<File, Long> {
}

package optimization.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductBackupRepository extends JpaRepository<ProductBackup, Long> {

    List<ProductBackup> findAllByOrderByCreateDateAsc();
}

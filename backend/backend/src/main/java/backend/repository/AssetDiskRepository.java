package backend.repository;

import backend.entity.AssetDisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetDiskRepository extends JpaRepository<AssetDisk, Long> {
    List<AssetDisk> findByAssetId(Long assetId);
    Optional<AssetDisk> findByAssetIdAndDriveName(Long assetId, String driveName);
}

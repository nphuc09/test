package com.viettel.importwiz.repository;

import com.viettel.importwiz.entity.RoleApiMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleApiMapRepository extends JpaRepository<RoleApiMap, Long> {
    @Query(value = "SELECT * FROM role_api_map " +
        "WHERE role_api_map_id = ?1 AND deleted = false", nativeQuery = true
    )
    RoleApiMap findByRoleApiMapId(Long accountId);
    @Query(value = "SELECT r FROM RoleApiMap r " +
        "WHERE LOWER(r.api) LIKE LOWER('%' || ?1 || '%') " +
        "AND r.deleted = false"
    )
    Page<RoleApiMap> findAllApiPaging(String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM role_api_map r WHERE LOWER(r.role) = " +
        "LOWER(?1) AND deleted = false", nativeQuery = true)
    List<RoleApiMap> getRoleApiMapByRole(String role);
    @Query(value = "UPDATE role_api_map SET deleted = true " +
        "WHERE role_api_map_id = ?1", nativeQuery = true
    )
    void deleteRoleApiMapById(Long id);
}

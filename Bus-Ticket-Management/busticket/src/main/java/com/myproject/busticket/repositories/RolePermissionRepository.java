package com.myproject.busticket.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.myproject.busticket.models.Role_Permission;

@Repository
public interface RolePermissionRepository extends JpaRepository<Role_Permission, Integer> {

    @Query("SELECT rp FROM Role_Permission rp WHERE rp.role.roleId = ?1")
    List<Role_Permission> findByRole_RoleId(int roleId);
}

package com.myproject.busticket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myproject.busticket.models.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    // Permission findByName(String name);

}

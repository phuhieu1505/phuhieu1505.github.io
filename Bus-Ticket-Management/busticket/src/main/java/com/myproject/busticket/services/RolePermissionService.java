package com.myproject.busticket.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myproject.busticket.models.Role_Permission;
import com.myproject.busticket.repositories.RolePermissionRepository;

@Service
public class RolePermissionService {
    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    public List<Role_Permission> getAll() {
        return rolePermissionRepository.findAll();
    }

    public List<Role_Permission> findByRole_RoleId(int roleId) {
        return rolePermissionRepository.findByRole_RoleId(roleId);
    }
}

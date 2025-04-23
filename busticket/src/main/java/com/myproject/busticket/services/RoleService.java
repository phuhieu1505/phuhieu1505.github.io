package com.myproject.busticket.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.myproject.busticket.models.Role;
import com.myproject.busticket.models.Role_Permission;
import com.myproject.busticket.repositories.RoleRepository;
import com.myproject.busticket.repositories.RolePermissionRepository;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    public Role getRoleById(int id) {
        return roleRepository.findById(id).orElse(null);
    }

    public Role getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName).orElse(null);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Page<Role> getAll(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    public List<Role_Permission> getPermissionsByRoleId(int roleId) {
        return rolePermissionRepository.findByRole_RoleId(roleId);
    }
}
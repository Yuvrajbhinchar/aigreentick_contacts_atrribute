package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.entity.Role;
import com.aigreentick.services.contacts.repository.RoleRepository;
import com.aigreentick.services.contacts.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new RuntimeException("Role already exists with name: " + role.getName());
        }
        return roleRepository.save(role);
    }

    @Override
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role updateRole(Long id, Role role) {
        Role existing = getRoleById(id);

        existing.setName(role.getName());
        existing.setDescription(role.getDescription());

        return roleRepository.save(existing);
    }

    @Override
    public void deleteRole(Long id) {
        Role existing = getRoleById(id);
        roleRepository.delete(existing);
    }
}

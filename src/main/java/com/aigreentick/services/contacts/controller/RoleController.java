package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.entity.Role;
import com.aigreentick.services.contacts.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        return new ResponseEntity<>(roleService.createRole(role), HttpStatus.CREATED);
    }

    // READ by ID
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(
            @PathVariable Long id,
            @RequestBody Role role
    ) {
        return ResponseEntity.ok(roleService.updateRole(id, role));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}

package org.example.docvanta_bcakend.controller;

import org.example.docvanta_bcakend.dto.ApiResponse;
import org.example.docvanta_bcakend.entity.ClinicPersonnel;
import org.example.docvanta_bcakend.entity.Patient;
import org.example.docvanta_bcakend.entity.Practitioner;
import org.example.docvanta_bcakend.entity.Specialty;
import org.example.docvanta_bcakend.entity.User;
import org.example.docvanta_bcakend.entity.UserRole;
import org.example.docvanta_bcakend.exception.ResourceNotFoundException;
import org.example.docvanta_bcakend.repository.SpecialtyRepository;
import org.example.docvanta_bcakend.repository.UserRepository;
import org.example.docvanta_bcakend.repository.UserRoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
public class AdminController {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SpecialtyRepository specialtyRepository;

    public AdminController(UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           SpecialtyRepository specialtyRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.specialtyRepository = specialtyRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(this::toUserMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", toUserMap(user)));
    }

    @PutMapping("/users/{id}/toggle-enabled")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleUserEnabled(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User " + (user.getEnabled() ? "enabled" : "disabled") + " successfully", toUserMap(user)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        userRepository.delete(user);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    // ── Role management ──────────────────────────────────────────────

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllRoles() {
        List<Map<String, Object>> roles = userRoleRepository.findAll().stream()
                .map(this::toRoleMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved successfully", roles));
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createRole(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Role name is required");
        }
        if (userRoleRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Role already exists: " + name);
        }
        UserRole role = UserRole.builder().name(name).build();
        userRoleRepository.save(role);
        return ResponseEntity.ok(ApiResponse.success("Role created successfully", toRoleMap(role)));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        UserRole role = userRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
        if (userRepository.existsByRole(role)) {
            throw new RuntimeException("Cannot delete role: it is still assigned to one or more users");
        }
        userRoleRepository.delete(role);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }

    private Map<String, Object> toRoleMap(UserRole role) {
        Map<String, Object> map = new HashMap<>();
        map.put("roleId", role.getUserRoleId());
        map.put("name", role.getName());
        return map;
    }

    // ── Specialty management ────────────────────────────────────────

    @GetMapping("/specialties")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllSpecialties() {
        List<Map<String, Object>> specialties = specialtyRepository.findAll().stream()
                .map(this::toSpecialtyMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Specialties retrieved successfully", specialties));
    }

    @PostMapping("/specialties")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createSpecialty(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Specialty name is required");
        }
        if (specialtyRepository.existsByName(name)) {
            throw new RuntimeException("Specialty already exists: " + name);
        }
        Specialty specialty = Specialty.builder().name(name).build();
        specialtyRepository.save(specialty);
        return ResponseEntity.ok(ApiResponse.success("Specialty created successfully", toSpecialtyMap(specialty)));
    }

    @DeleteMapping("/specialties/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSpecialty(@PathVariable Long id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with ID: " + id));
        specialtyRepository.delete(specialty);
        return ResponseEntity.ok(ApiResponse.success("Specialty deleted successfully", null));
    }

    private Map<String, Object> toSpecialtyMap(Specialty specialty) {
        Map<String, Object> map = new HashMap<>();
        map.put("specialtyId", specialty.getSpecialtyId());
        map.put("name", specialty.getName());
        return map;
    }

    // ── User helpers ────────────────────────────────────────────────

    private Map<String, Object> toUserMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUserId());
        map.put("username", user.getUsername());
        map.put("enabled", user.getEnabled());
        map.put("roleName", user.getRole() != null ? user.getRole().getName() : null);

        if (user instanceof Patient p) {
            map.put("userType", "PATIENT");
            map.put("firstName", p.getFirstName());
            map.put("lastName", p.getLastName());
            map.put("email", p.getEmail());
            map.put("phone", p.getPhone());
        } else if (user instanceof Practitioner p) {
            map.put("userType", "PRACTITIONER");
            map.put("firstName", p.getFirstName());
            map.put("lastName", p.getLastName());
            map.put("email", p.getEmail());
            map.put("phone", p.getPhone());
        } else if (user instanceof ClinicPersonnel p) {
            map.put("userType", "CLINIC_PERSONNEL");
            map.put("firstName", p.getFirstName());
            map.put("lastName", p.getLastName());
            map.put("personnelType", p.getPersonnelType() != null ? p.getPersonnelType().name() : null);
        }

        return map;
    }
}


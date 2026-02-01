package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.AuthResponse;
import org.example.docvanta_bcakend.dto.LoginRequest;
import org.example.docvanta_bcakend.dto.RegisterRequest;
import org.example.docvanta_bcakend.entity.ClinicPersonnel;
import org.example.docvanta_bcakend.entity.Patient;
import org.example.docvanta_bcakend.entity.PersonnelType;
import org.example.docvanta_bcakend.entity.Practitioner;
import org.example.docvanta_bcakend.entity.Specialty;
import org.example.docvanta_bcakend.entity.User;
import org.example.docvanta_bcakend.entity.UserRole;
import org.example.docvanta_bcakend.repository.SpecialtyRepository;
import org.example.docvanta_bcakend.repository.UserRepository;
import org.example.docvanta_bcakend.repository.UserRoleRepository;
import org.example.docvanta_bcakend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            SpecialtyRepository specialtyRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.specialtyRepository = specialtyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Public registration is restricted to PATIENT only.
        // Staff/practitioner accounts are created by administrators via /api/auth/admin/create-user.
        String roleName = "PATIENT";

        UserRole role = userRoleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName + ". Please ensure roles are initialized."));

        User user = createUserByRole(request, roleName, role);
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Map<String, Object> claims = buildClaims(user, roleName);
        String token = jwtService.generateToken(claims, userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(roleName)
                .userId(user.getUserId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Allow login by username or email
        String identifier = request.getUsername();
        String actualUsername = identifier;
        if (identifier.contains("@")) {
            User emailUser = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("No account found with this email"));
            actualUsername = emailUser.getUsername();
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        actualUsername,
                        request.getPassword()));

        User user = userRepository.findByUsername(actualUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        // Determine the effective role name
        String roleName = determineRole(user);

        Map<String, Object> claims = buildClaims(user, roleName);
        String token = jwtService.generateToken(claims, userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(roleName)
                .userId(user.getUserId())
                .build();
    }

    /**
     * Admin-only: create any type of user (practitioner, staff, patient, etc.)
     */
    public AuthResponse adminCreateUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        String roleName = normalizeRoleName(request.getRole() != null ? request.getRole().toUpperCase() : "PATIENT");

        UserRole role = userRoleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        User user = createUserByRole(request, roleName, role);
        userRepository.save(user);

        return AuthResponse.builder()
                .token("")
                .username(user.getUsername())
                .role(roleName)
                .userId(user.getUserId())
                .build();
    }

    /**
     * Determine the effective role for a user.
     * For ClinicPersonnel, the personnelType IS the role (RECEPTIONIST, SYSTEM_ADMINISTRATOR)
     * For others, use the role from the DB.
     */
    private String determineRole(User user) {
        // For ClinicPersonnel, personnelType is the actual role
        if (user instanceof ClinicPersonnel personnelUser && personnelUser.getPersonnelType() != null) {
            return personnelUser.getPersonnelType().name();
        }
        // For Practitioner/Patient, use the DB role
        return user.getRole() != null ? user.getRole().getName() : "PATIENT";
    }

    /**
     * Normalize various input role names to canonical role names.
     */
    private String normalizeRoleName(String inputRole) {
        return switch (inputRole) {
            case "DOCTOR", "PRACTITIONER" -> "PRACTITIONER";
            case "ADMIN", "SYSTEM_ADMINISTRATOR" -> "SYSTEM_ADMINISTRATOR";
            case "RECEPTIONIST", "STAFF" -> "RECEPTIONIST";
            default -> "PATIENT";
        };
    }

    private Map<String, Object> buildClaims(User user, String roleName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", roleName);
        return claims;
    }

    private User createUserByRole(RegisterRequest request, String roleName, UserRole role) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        return switch (roleName) {
            case "PRACTITIONER" -> {
                Practitioner practitioner = Practitioner.builder()
                        .username(request.getUsername())
                        .password(encodedPassword)
                        .enabled(true)
                        .role(role)
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .build();
                // Resolve specialties: prefer IDs over comma-separated names
                if (request.getSpecialtyIds() != null && !request.getSpecialtyIds().isEmpty()) {
                    List<Specialty> specialtyList = specialtyRepository.findAllById(request.getSpecialtyIds());
                    if (specialtyList.size() != request.getSpecialtyIds().size()) {
                        throw new RuntimeException("One or more specialty IDs are invalid");
                    }
                    practitioner.setSpecialties(new ArrayList<>(specialtyList));
                } else if (request.getSpecialties() != null && !request.getSpecialties().isBlank()) {
                    List<Specialty> specialtyList = Arrays.stream(request.getSpecialties().split(","))
                            .map(String::trim)
                            .filter(name -> !name.isEmpty())
                            .map(name -> specialtyRepository.findByName(name)
                                    .orElseGet(() -> specialtyRepository.save(Specialty.builder().name(name).build())))
                            .toList();
                    practitioner.setSpecialties(new ArrayList<>(specialtyList));
                }
                yield practitioner;
            }
            case "SYSTEM_ADMINISTRATOR" -> ClinicPersonnel.builder()
                    .username(request.getUsername())
                    .password(encodedPassword)
                    .enabled(true)
                    .role(role)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .personnelType(PersonnelType.SYSTEM_ADMINISTRATOR)
                    .build();
            case "RECEPTIONIST" -> ClinicPersonnel.builder()
                    .username(request.getUsername())
                    .password(encodedPassword)
                    .enabled(true)
                    .role(role)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .personnelType(PersonnelType.RECEPTIONIST)
                    .build();
            default -> {
                Patient.PatientBuilder<?, ?> patientBuilder = Patient.builder()
                        .username(request.getUsername())
                        .password(encodedPassword)
                        .enabled(true)
                        .role(role)
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .address(request.getAddress());
                if (request.getDob() != null && !request.getDob().isBlank()) {
                    patientBuilder.dob(LocalDate.parse(request.getDob()));
                }
                yield patientBuilder.build();
            }
        };
    }
}

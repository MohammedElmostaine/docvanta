package org.example.docvanta_bcakend.entity;

/**
 * Enum representing different types of clinic personnel positions.
 */
public enum PersonnelType {
    RECEPTIONIST("Receptionist"),
    SYSTEM_ADMINISTRATOR("System Administrator");

    private final String displayName;

    PersonnelType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert a string to PersonnelType enum (case-insensitive)
     */
    public static PersonnelType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return RECEPTIONIST; // default
        }
        String normalized = value.trim().toUpperCase();
        for (PersonnelType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }
        // Handle legacy position names
        if (normalized.equals("ADMINISTRATOR") || normalized.equals("ADMIN")) {
            return SYSTEM_ADMINISTRATOR;
        }
        return RECEPTIONIST;
    }

    /**
     * Check if this personnel type can manage appointments
     */
    public boolean canManageAppointments() {
        return this == RECEPTIONIST || this == SYSTEM_ADMINISTRATOR;
    }

    /**
     * Check if this personnel type can access patient information
     */
    public boolean canViewPatients() {
        return this == RECEPTIONIST || this == SYSTEM_ADMINISTRATOR;
    }

    /**
     * Check if this personnel type is a system administrator
     */
    public boolean isSystemAdministrator() {
        return this == SYSTEM_ADMINISTRATOR;
    }
}

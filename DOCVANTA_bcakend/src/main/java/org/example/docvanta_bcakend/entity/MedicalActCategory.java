package org.example.docvanta_bcakend.entity;

public enum MedicalActCategory {
    CONSULTATION("Consultation"),
    PROCEDURE("Procedure"),
    LAB_TEST("Lab Test"),
    IMAGING("Imaging"),
    TREATMENT("Treatment"),
    SURGERY("Surgery"),
    OTHER("Other");

    private final String displayName;

    MedicalActCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

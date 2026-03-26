package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.ClinicDTO;
import org.example.docvanta_bcakend.entity.Clinic;
import org.example.docvanta_bcakend.repository.ClinicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClinicServiceTest {

    @Mock
    private ClinicRepository clinicRepository;

    @InjectMocks
    private ClinicService clinicService;

    private Clinic clinic;
    private ClinicDTO clinicDTO;

    @BeforeEach
    void setUp() {
        clinic = Clinic.builder()
                .clinicId(1L)
                .name("Test Clinic")
                .address("123 Test Street")
                .build();

        clinicDTO = ClinicDTO.builder()
                .clinicId(1L)
                .name("Test Clinic")
                .address("123 Test Street")
                .build();
    }

    @Test
    @DisplayName("Should get all clinics")
    void getAllClinics_Success() {
        when(clinicRepository.findAll()).thenReturn(List.of(clinic));

        List<ClinicDTO> result = clinicService.getAllClinics();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Clinic", result.get(0).getName());
        verify(clinicRepository).findAll();
    }

    @Test
    @DisplayName("Should get clinic by ID")
    void getClinicById_Success() {
        when(clinicRepository.findById(1L)).thenReturn(Optional.of(clinic));

        ClinicDTO result = clinicService.getClinicById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getClinicId());
        assertEquals("Test Clinic", result.getName());
    }

    @Test
    @DisplayName("Should throw exception when clinic not found by ID")
    void getClinicById_NotFound() {
        when(clinicRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clinicService.getClinicById(999L));
    }

    @Test
    @DisplayName("Should get clinic by name")
    void getClinicByName_Success() {
        when(clinicRepository.findByName("Test Clinic")).thenReturn(Optional.of(clinic));

        ClinicDTO result = clinicService.getClinicByName("Test Clinic");

        assertNotNull(result);
        assertEquals("Test Clinic", result.getName());
    }

    @Test
    @DisplayName("Should throw exception when clinic not found by name")
    void getClinicByName_NotFound() {
        when(clinicRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clinicService.getClinicByName("Unknown"));
    }

    @Test
    @DisplayName("Should create clinic successfully")
    void createClinic_Success() {
        when(clinicRepository.save(any(Clinic.class))).thenReturn(clinic);

        ClinicDTO result = clinicService.createClinic(clinicDTO);

        assertNotNull(result);
        assertEquals("Test Clinic", result.getName());
        assertEquals("123 Test Street", result.getAddress());
        verify(clinicRepository).save(any(Clinic.class));
    }

    @Test
    @DisplayName("Should update clinic successfully")
    void updateClinic_Success() {
        ClinicDTO updateDTO = ClinicDTO.builder()
                .name("Updated Clinic")
                .address("456 New Street")
                .build();

        Clinic updatedClinic = Clinic.builder()
                .clinicId(1L)
                .name("Updated Clinic")
                .address("456 New Street")
                .build();

        when(clinicRepository.findById(1L)).thenReturn(Optional.of(clinic));
        when(clinicRepository.save(any(Clinic.class))).thenReturn(updatedClinic);

        ClinicDTO result = clinicService.updateClinic(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Updated Clinic", result.getName());
        assertEquals("456 New Street", result.getAddress());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent clinic")
    void updateClinic_NotFound() {
        when(clinicRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clinicService.updateClinic(999L, clinicDTO));
    }

    @Test
    @DisplayName("Should delete clinic successfully")
    void deleteClinic_Success() {
        when(clinicRepository.existsById(1L)).thenReturn(true);
        doNothing().when(clinicRepository).deleteById(1L);

        assertDoesNotThrow(() -> clinicService.deleteClinic(1L));

        verify(clinicRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent clinic")
    void deleteClinic_NotFound() {
        when(clinicRepository.existsById(999L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> clinicService.deleteClinic(999L));

        verify(clinicRepository, never()).deleteById(any());
    }
}

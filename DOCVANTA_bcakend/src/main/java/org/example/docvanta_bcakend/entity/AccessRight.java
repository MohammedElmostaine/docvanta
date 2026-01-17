package org.example.docvanta_bcakend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "access_rights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_right_id")
    private Long accessRightId;

    @Column(unique = true, nullable = false)
    private String code;

    private String description;
}

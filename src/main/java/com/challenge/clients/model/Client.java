package com.challenge.clients.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String firstName;

    @Column(name = "apellido", nullable = false, length = 100)
    private String lastName;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String companyName;

    @Column(name = "cuit", nullable = false, unique = true, length = 20)
    private String taxId;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate birthDate;

    @Column(name = "telefono_celular", nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "fecha_modificacion")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

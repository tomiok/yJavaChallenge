package com.challenge.clients.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String companyName;
    private String taxId;
    private LocalDate birthDate;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.challenge.clients.service;

import com.challenge.clients.dto.ClientRequestDTO;
import com.challenge.clients.dto.ClientResponseDTO;
import com.challenge.clients.exception.DuplicateResourceException;
import com.challenge.clients.exception.ResourceNotFoundException;
import com.challenge.clients.model.Client;
import com.challenge.clients.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;
    private ClientRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Perez")
                .companyName("JP Servicios SRL")
                .taxId("20-12345678-9")
                .birthDate(LocalDate.of(1985, 6, 15))
                .phoneNumber("1165874210")
                .email("juan.perez@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        requestDTO = ClientRequestDTO.builder()
                .firstName("Juan")
                .lastName("Perez")
                .companyName("JP Servicios SRL")
                .taxId("20-12345678-9")
                .birthDate(LocalDate.of(1985, 6, 15))
                .phoneNumber("1165874210")
                .email("juan.perez@example.com")
                .build();
    }

    @Test
    void findAll_returnsListOfClients() {
        when(clientRepository.findAll()).thenReturn(List.of(client));

        List<ClientResponseDTO> result = clientService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Juan");
        verify(clientRepository).findAll();
    }

    @Test
    void findAll_returnsEmptyList() {
        when(clientRepository.findAll()).thenReturn(List.of());

        List<ClientResponseDTO> result = clientService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_returnsClient() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientResponseDTO result = clientService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Juan");
        assertThat(result.getEmail()).isEqualTo("juan.perez@example.com");
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void searchByName_returnsMatchingClients() {
        when(clientRepository.searchByName("Juan")).thenReturn(List.of(client));

        List<ClientResponseDTO> result = clientService.searchByName("Juan");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Juan");
    }

    @Test
    void searchByName_returnsEmptyWhenNoMatch() {
        when(clientRepository.searchByName("XYZ")).thenReturn(List.of());

        List<ClientResponseDTO> result = clientService.searchByName("XYZ");

        assertThat(result).isEmpty();
    }

    @Test
    void create_savesAndReturnsClient() {
        when(clientRepository.findByTaxId(any())).thenReturn(Optional.empty());
        when(clientRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        ClientResponseDTO result = clientService.create(requestDTO);

        assertThat(result.getFirstName()).isEqualTo("Juan");
        assertThat(result.getTaxId()).isEqualTo("20-12345678-9");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void create_throwsOnDuplicateTaxId() {
        when(clientRepository.findByTaxId("20-12345678-9")).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> clientService.create(requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("tax ID");
    }

    @Test
    void create_throwsOnDuplicateEmail() {
        when(clientRepository.findByTaxId(any())).thenReturn(Optional.empty());
        when(clientRepository.findByEmail("juan.perez@example.com")).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> clientService.create(requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");
    }

    @Test
    void update_updatesAndReturnsClient() {
        ClientRequestDTO updateRequest = ClientRequestDTO.builder()
                .firstName("Juan Carlos")
                .lastName("Perez")
                .companyName("JC Servicios SRL")
                .taxId("20-12345678-9")
                .birthDate(LocalDate.of(1985, 6, 15))
                .phoneNumber("1165874999")
                .email("juan.perez@example.com")
                .build();

        Client updated = Client.builder()
                .id(1L)
                .firstName("Juan Carlos")
                .lastName("Perez")
                .companyName("JC Servicios SRL")
                .taxId("20-12345678-9")
                .birthDate(LocalDate.of(1985, 6, 15))
                .phoneNumber("1165874999")
                .email("juan.perez@example.com")
                .createdAt(client.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.findByTaxId("20-12345678-9")).thenReturn(Optional.of(client));
        when(clientRepository.findByEmail("juan.perez@example.com")).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(updated);

        ClientResponseDTO result = clientService.update(1L, updateRequest);

        assertThat(result.getFirstName()).isEqualTo("Juan Carlos");
        assertThat(result.getPhoneNumber()).isEqualTo("1165874999");
    }

    @Test
    void update_throwsWhenNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.update(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_throwsOnDuplicateTaxIdFromOtherClient() {
        Client otherClient = Client.builder().id(2L).taxId("20-12345678-9").build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.findByTaxId("20-12345678-9")).thenReturn(Optional.of(otherClient));

        assertThatThrownBy(() -> clientService.update(1L, requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("tax ID");
    }

    @Test
    void update_throwsOnDuplicateEmailFromOtherClient() {
        Client otherClient = Client.builder().id(2L).email("juan.perez@example.com").build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.findByTaxId(any())).thenReturn(Optional.of(client));
        when(clientRepository.findByEmail("juan.perez@example.com")).thenReturn(Optional.of(otherClient));

        assertThatThrownBy(() -> clientService.update(1L, requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");
    }

    @Test
    void delete_deletesClient() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        clientService.delete(1L);

        verify(clientRepository).delete(client);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}

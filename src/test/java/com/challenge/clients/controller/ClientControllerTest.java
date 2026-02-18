package com.challenge.clients.controller;

import com.challenge.clients.dto.ClientRequestDTO;
import com.challenge.clients.dto.ClientResponseDTO;
import com.challenge.clients.exception.DuplicateResourceException;
import com.challenge.clients.exception.GlobalExceptionHandler;
import com.challenge.clients.exception.ResourceNotFoundException;
import com.challenge.clients.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private ClientResponseDTO responseDTO;
    private ClientRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        responseDTO = ClientResponseDTO.builder()
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
    void getAll_returns200WithList() throws Exception {
        when(clientService.findAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Juan"));
    }

    @Test
    void getAll_returns200WithEmptyList() throws Exception {
        when(clientService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getById_returns200() throws Exception {
        when(clientService.findById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(clientService.findById(99L)).thenThrow(new ResourceNotFoundException("Client not found with id: 99"));

        mockMvc.perform(get("/api/clients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client not found with id: 99"));
    }

    @Test
    void search_returns200WithResults() throws Exception {
        when(clientService.searchByName("Juan")).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/clients/search").param("name", "Juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Juan"));
    }

    @Test
    void search_returns200WithEmptyResults() throws Exception {
        when(clientService.searchByName("XYZ")).thenReturn(List.of());

        mockMvc.perform(get("/api/clients/search").param("name", "XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void create_returns201() throws Exception {
        when(clientService.create(any(ClientRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan"));
    }

    @Test
    void create_returns400OnValidationError() throws Exception {
        ClientRequestDTO invalid = ClientRequestDTO.builder()
                .firstName("")
                .lastName("")
                .companyName("")
                .taxId("invalid")
                .birthDate(LocalDate.now().plusDays(1))
                .phoneNumber("")
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void create_returns409OnDuplicateTaxId() throws Exception {
        when(clientService.create(any(ClientRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("A client with tax ID 20-12345678-9 already exists"));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A client with tax ID 20-12345678-9 already exists"));
    }

    @Test
    void update_returns200() throws Exception {
        when(clientService.update(eq(1L), any(ClientRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        when(clientService.update(eq(99L), any(ClientRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Client not found with id: 99"));

        mockMvc.perform(put("/api/clients/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client not found with id: 99"));
    }

    @Test
    void update_returns400OnValidationError() throws Exception {
        ClientRequestDTO invalid = ClientRequestDTO.builder().build();

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(clientService).delete(1L);

        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Client not found with id: 99"))
                .when(clientService).delete(99L);

        mockMvc.perform(delete("/api/clients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client not found with id: 99"));
    }
}

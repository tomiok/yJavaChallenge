package com.challenge.clients.controller;

import com.challenge.clients.dto.ClientRequestDTO;
import com.challenge.clients.dto.ClientResponseDTO;
import com.challenge.clients.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Client management endpoints")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    @Operation(summary = "Get all clients")
    @ApiResponse(responseCode = "200", description = "List of all clients")
    public ResponseEntity<List<ClientResponseDTO>> getAll() {
        return ResponseEntity.ok(clientService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a client by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client found"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.findById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Search clients by name (partial, case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search results")
    public ResponseEntity<List<ClientResponseDTO>> search(@RequestParam String name) {
        return ResponseEntity.ok(clientService.searchByName(name));
    }

    @PostMapping
    @Operation(summary = "Create a new client")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Duplicate tax ID or email")
    })
    public ResponseEntity<ClientResponseDTO> create(@Valid @RequestBody ClientRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing client")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client updated"),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Duplicate tax ID or email")
    })
    public ResponseEntity<ClientResponseDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody ClientRequestDTO request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a client")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client deleted"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

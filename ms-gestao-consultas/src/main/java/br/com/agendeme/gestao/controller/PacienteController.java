package br.com.agendeme.gestao.controller;

import br.com.agendeme.gestao.dto.paciente.PacienteRequest;
import br.com.agendeme.gestao.dto.paciente.PacienteResponse;
import br.com.agendeme.gestao.dto.paciente.PacienteUpdate;
import br.com.agendeme.gestao.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "Operações de gerenciamento de pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    @Operation(summary = "Cadastrar paciente", description = "Realiza o cadastro de um novo paciente no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Paciente cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PacienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "CPF, login ou e-mail já cadastrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @PostMapping
    public ResponseEntity<PacienteResponse> cadastrar(@RequestBody @Valid PacienteRequest dto,
                                                      UriComponentsBuilder uriBuilder) {
        PacienteResponse response = pacienteService.cadastrar(dto);
        var uri = uriBuilder.path("/pacientes/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Buscar paciente por CPF", description = "Retorna os dados de um paciente com base no CPF informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PacienteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<PacienteResponse> buscarPorCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(pacienteService.buscarPorCpf(cpf));
    }

    @Operation(summary = "Buscar pacientes por nome", description = "Retorna uma lista paginada de pacientes cujo nome contenha o termo informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @GetMapping("/nome")
    public ResponseEntity<Page<PacienteResponse>> buscarPorNome(@RequestParam String nome,
                                                                @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(pacienteService.buscarPorNome(nome, pageable));
    }

    @Operation(summary = "Listar todos os pacientes", description = "Retorna uma lista paginada com todos os pacientes cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<PacienteResponse>> listarPacientes(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(pacienteService.listarPacientes(pageable));
    }

    @Operation(summary = "Listar pacientes ativos", description = "Retorna uma lista paginada com os pacientes que estão ativos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @GetMapping("/ativos")
    public ResponseEntity<Page<PacienteResponse>> listarPacientesAtivos(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(pacienteService.listarPacientesAtivos(pageable));
    }

    @Operation(summary = "Atualizar paciente", description = "Atualiza os dados de um paciente existente. Apenas os campos informados serão alterados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PacienteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Paciente inativo",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<PacienteResponse> atualizar(@PathVariable Long id,
                                                      @RequestBody @Valid PacienteUpdate dto) {
        return ResponseEntity.ok(pacienteService.atualizar(id, dto));
    }

    @Operation(summary = "Inativar paciente", description = "Realiza a inativação lógica de um paciente pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Paciente inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Paciente já está inativo",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @DeleteMapping("/{cpf}")
    public ResponseEntity<Void> inativar(@PathVariable String cpf) {
        pacienteService.inativar(cpf);
        return ResponseEntity.noContent().build();
    }
}


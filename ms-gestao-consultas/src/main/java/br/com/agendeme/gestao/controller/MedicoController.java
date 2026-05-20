package br.com.agendeme.gestao.controller;

import br.com.agendeme.gestao.dto.medico.MedicoRequest;
import br.com.agendeme.gestao.dto.medico.MedicoResponse;
import br.com.agendeme.gestao.dto.medico.MedicoUpdate;
import br.com.agendeme.gestao.model.enums.Especialidade;
import br.com.agendeme.gestao.service.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/medicos")
@RequiredArgsConstructor
@Tag(name = "Médicos", description = "Operações de gerenciamento de médicos")
public class MedicoController {

    private final MedicoService medicoService;

    @Operation(summary = "Cadastrar médico", description = "Realiza o cadastro de um novo médico no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Médico cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MedicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "CRM, login ou e-mail já cadastrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MedicoResponse> cadastrar(@RequestBody @Valid MedicoRequest dto,
                                                    UriComponentsBuilder uriBuilder) {
        MedicoResponse response = medicoService.cadastrar(dto);
        var uri = uriBuilder.path("/medicos/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Buscar médico por CRM", description = "Retorna os dados de um médico com base no número de CRM informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MedicoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Médico não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @GetMapping("/crm/{crm}")
    public ResponseEntity<MedicoResponse> buscarPorCrm(@PathVariable String crm) {
        return ResponseEntity.ok(medicoService.buscarPorCrm(crm));
    }

    @Operation(summary = "Buscar médicos por nome", description = "Retorna uma lista paginada de médicos cujo nome contenha o termo informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @GetMapping("/nome")
    public ResponseEntity<Page<MedicoResponse>> buscarPorNome(@RequestParam String nome,
                                                              @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(medicoService.buscarPorNome(nome, pageable));
    }

    @Operation(summary = "Buscar médicos por especialidade", description = "Retorna uma lista paginada de médicos filtrada pela especialidade informada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Especialidade inválida",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @GetMapping("/especialidade")
    public ResponseEntity<Page<MedicoResponse>> buscarPorEspecialidade(@RequestParam Especialidade especialidade,
                                                                       @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(medicoService.buscarPorEspecialidade(especialidade, pageable));
    }

    @Operation(summary = "Listar todos os médicos", description = "Retorna uma lista paginada com todos os médicos cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<MedicoResponse>> listarMedicos(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(medicoService.listarMedicos(pageable));
    }

    @Operation(summary = "Listar médicos ativos", description = "Retorna uma lista paginada com os médicos que estão ativos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @GetMapping("/ativos")
    public ResponseEntity<Page<MedicoResponse>> listarMedicosAtivos(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(medicoService.listarMedicosAtivos(pageable));
    }

    @Operation(summary = "Atualizar médico", description = "Atualiza os dados de um médico existente. Apenas os campos informados serão alterados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Médico atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MedicoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Médico não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Médico inativo",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<MedicoResponse> atualizar(@PathVariable Long id,
                                                    @RequestBody @Valid MedicoUpdate dto) {
        return ResponseEntity.ok(medicoService.atualizar(id, dto));
    }

    @Operation(summary = "Inativar médico", description = "Realiza a inativação lógica de um médico pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Médico inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Médico não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Médico já está inativo",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{crm}")
    public ResponseEntity<Void> inativar(@PathVariable String crm) {
        medicoService.inativar(crm);
        return ResponseEntity.noContent().build();
    }
}


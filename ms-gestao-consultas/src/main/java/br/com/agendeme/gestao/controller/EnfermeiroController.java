package br.com.agendeme.gestao.controller;

import br.com.agendeme.gestao.dto.enfermeiro.EnfermeiroReativacaoRequest;
import br.com.agendeme.gestao.dto.enfermeiro.EnfermeiroRequest;
import br.com.agendeme.gestao.dto.enfermeiro.EnfermeiroResponse;
import br.com.agendeme.gestao.dto.enfermeiro.EnfermeiroUpdate;
import br.com.agendeme.gestao.service.EnfermeiroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/enfermeiros")
@RequiredArgsConstructor
@Tag(name = "Enfermeiros", description = "Operações de gerenciamento de enfermeiros")
public class EnfermeiroController {

    private final EnfermeiroService enfermeiroService;

    @Operation(summary = "Cadastrar enfermeiro", description = "Realiza o cadastro de um novo enfermeiro no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Enfermeiro cadastrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnfermeiroResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "CRE, login ou e-mail já cadastrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EnfermeiroResponse> cadastrar(@RequestBody @Valid EnfermeiroRequest dto,
                                                        UriComponentsBuilder uriBuilder) {
        EnfermeiroResponse response = enfermeiroService.cadastrar(dto);
        var uri = uriBuilder.path("/enfermeiros/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Buscar enfermeiro por CRE", description = "Retorna os dados de um enfermeiro com base no número de CRE informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnfermeiroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Enfermeiro não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @GetMapping("/cre/{cre}")
    public ResponseEntity<EnfermeiroResponse> buscarPorCre(@PathVariable String cre) {
        return ResponseEntity.ok(enfermeiroService.buscarPorCre(cre));
    }

    @Operation(summary = "Buscar enfermeiros por nome", description = "Retorna uma lista paginada de enfermeiros cujo nome contenha o termo informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @GetMapping("/nome")
    public ResponseEntity<Page<EnfermeiroResponse>> buscarPorNome(@RequestParam String nome,
                                                                  @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(enfermeiroService.buscarPorNome(nome, pageable));
    }

    @Operation(summary = "Listar todos os enfermeiros", description = "Retorna uma lista paginada com todos os enfermeiros cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<EnfermeiroResponse>> listarEnfermeiros(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(enfermeiroService.listarEnfermeiros(pageable));
    }

    @Operation(summary = "Listar enfermeiros ativos", description = "Retorna uma lista paginada com os enfermeiros que estão ativos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'ADMIN')")
    @GetMapping("/ativos")
    public ResponseEntity<Page<EnfermeiroResponse>> listarEnfermeirosAtivos(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(enfermeiroService.listarEnfermeirosAtivos(pageable));
    }

    @Operation(summary = "Atualizar enfermeiro", description = "Atualiza os dados de um enfermeiro existente. Apenas os campos informados serão alterados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enfermeiro atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnfermeiroResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Enfermeiro não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Enfermeiro inativo",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<EnfermeiroResponse> atualizar(@PathVariable Long id,
                                                        @RequestBody @Valid EnfermeiroUpdate dto) {
        return ResponseEntity.ok(enfermeiroService.atualizar(id, dto));
    }

    @Operation(summary = "Inativar enfermeiro", description = "Realiza a inativação lógica de um enfermeiro pelo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Enfermeiro inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Enfermeiro não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Enfermeiro já está inativo",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{cre}")
    public ResponseEntity<Void> inativar(@PathVariable String cre) {
        enfermeiroService.inativar(cre);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar enfermeiro", description = "Reativa um enfermeiro inativo pelo CRE, atualizando opcionalmente seus dados. Login e CRE não podem ser alterados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enfermeiro reativado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnfermeiroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Enfermeiro não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "E-mail já está em uso por outro usuário",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Enfermeiro já está ativo",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{cre}/reativar")
    public ResponseEntity<EnfermeiroResponse> reativar(@PathVariable String cre,
                                                       @RequestBody @Valid EnfermeiroReativacaoRequest dto) {
        return ResponseEntity.ok(enfermeiroService.reativar(cre, dto));
    }
}

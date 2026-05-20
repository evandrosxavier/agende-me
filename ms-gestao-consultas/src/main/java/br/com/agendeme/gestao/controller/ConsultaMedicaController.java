package br.com.agendeme.gestao.controller;

import br.com.agendeme.gestao.dto.consulta.ConsultaAgendamentoResponse;
import br.com.agendeme.gestao.dto.consulta.ConsultaAgendamentoUpdate;
import br.com.agendeme.gestao.dto.consulta.ConsultaAtendimentoResponse;
import br.com.agendeme.gestao.dto.consulta.ConsultaAtendimentoUpdate;
import br.com.agendeme.gestao.dto.consulta.ConsultaRequest;
import br.com.agendeme.gestao.model.enums.Especialidade;
import br.com.agendeme.gestao.model.enums.StatusConsulta;
import br.com.agendeme.gestao.model.domain.Usuario;
import br.com.agendeme.gestao.service.ConsultaMedicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/consultas")
@RequiredArgsConstructor
@Tag(name = "Consultas Médicas", description = "Operações de agendamento e gerenciamento de consultas médicas")
public class ConsultaMedicaController {

    private final ConsultaMedicaService consultaMedicaService;

    @Operation(summary = "Agendar consulta", description = "Realiza o agendamento de uma nova consulta médica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consulta agendada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsultaAgendamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Paciente ou médico não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ConsultaAgendamentoResponse> cadastrarConsulta(@RequestBody @Valid ConsultaRequest dto,
                                                                         UriComponentsBuilder uriBuilder) {
        ConsultaAgendamentoResponse response = consultaMedicaService.cadastrarConsulta(dto);
        var uri = uriBuilder.path("/consultas/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Atualizar consulta agendada", description = "Atualiza os dados de agendamento de uma consulta (data, hora, médico ou especialidade).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsultaAgendamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Consulta ou médico não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO', 'ADMIN')")
    @PatchMapping("/{id}/agendamento")
    public ResponseEntity<ConsultaAgendamentoResponse> atualizarConsultaAgendada(@PathVariable Long id,
                                                                                  @RequestBody @Valid ConsultaAgendamentoUpdate dto) {
        return ResponseEntity.ok(consultaMedicaService.atualizarConsultaAgendada(id, dto));
    }

    @Operation(summary = "Registrar atendimento da consulta", description = "Registra o resultado do atendimento de uma consulta realizada (diagnóstico, tratamento e observações).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atendimento registrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsultaAgendamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    @PatchMapping("/{id}/atendimento")
    public ResponseEntity<ConsultaAtendimentoResponse> registrarAtendimento(@PathVariable Long id,
                                                                                   @RequestBody @Valid ConsultaAtendimentoUpdate dto) {
        return ResponseEntity.ok(consultaMedicaService.registrarAtendimento(id, dto));
    }

    @Operation(summary = "Atualizar uma consulta já realizada", description = "Atualiza os dados de uma consulta realizada (diagnóstico, tratamento e observações).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Atendimento registrado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsultaAgendamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    @PatchMapping("/{id}/atendimento/atualizacao")
    public ResponseEntity<ConsultaAtendimentoResponse> atualizarAtendimentoRealizado(@PathVariable Long id,
                                                                           @RequestBody @Valid ConsultaAtendimentoUpdate dto) {
        return ResponseEntity.ok(consultaMedicaService.atualizarAtendimentoRealizado(id, dto));
    }


    @Operation(summary = "Cancelar consulta", description = "Cancela uma consulta previamente agendada. Consultas realizadas não podem ser canceladas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta cancelada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConsultaAtendimentoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Consulta não encontrada",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Consulta já cancelada ou já realizada",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ConsultaAgendamentoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(consultaMedicaService.cancelar(id));
    }

    @Operation(summary = "Buscar consultas por CPF do paciente", description = "Retorna uma lista paginada de consultas de um paciente identificado pelo CPF.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Paciente não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })

    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @GetMapping("/paciente/{cpf}")
    public ResponseEntity<Page<ConsultaAgendamentoResponse>> buscarPorCpfPaciente(@PathVariable String cpf,
                                                                                   @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        return ResponseEntity.ok(consultaMedicaService.buscarPorCpfPaciente(cpf, pageable));
    }

    @Operation(summary = "Buscar consultas por CRM do médico", description = "Retorna uma lista paginada de consultas de um médico identificado pelo CRM.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Médico não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @GetMapping("/medico/{crm}")
    public ResponseEntity<Page<ConsultaAgendamentoResponse>> buscarPorCrmMedico(@PathVariable String crm,
                                                                                 @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        return ResponseEntity.ok(consultaMedicaService.buscarPorCrmMedico(crm, pageable));
    }

    @Operation(summary = "Buscar consultas por período", description = "Retorna uma lista paginada de consultas dentro do intervalo de datas informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Datas inválidas",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @GetMapping("/periodo")
    public ResponseEntity<Page<ConsultaAgendamentoResponse>> buscarPorPeriodo(
            @Parameter(description = "Data de início do período (formato: yyyy-MM-dd)", example = "2026-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data de fim do período (formato: yyyy-MM-dd)", example = "2026-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt    = fim.atTime(23, 59, 59);
        return ResponseEntity.ok(consultaMedicaService.buscarPorPeriodo(inicioDt, fimDt, pageable));
    }

    @Operation(summary = "Buscar consultas por status", description = "Retorna uma lista paginada de consultas filtradas pelo status informado (AGENDADA, REALIZADA ou CANCELADA).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @GetMapping("/status")
    public ResponseEntity<Page<ConsultaAgendamentoResponse>> buscarPorStatus(@RequestParam StatusConsulta status,
                                                                              @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        return ResponseEntity.ok(consultaMedicaService.buscarPorStatus(status, pageable));
    }

    @Operation(summary = "Buscar consultas por especialidade", description = "Retorna uma lista paginada de consultas filtradas pela especialidade médica informada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Especialidade inválida",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @GetMapping("/especialidade")
    public ResponseEntity<Page<ConsultaAgendamentoResponse>> buscarPorEspecialidade(@RequestParam Especialidade especialidade,
                                                                                     @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        return ResponseEntity.ok(consultaMedicaService.buscarPorEspecialidade(especialidade, pageable));
    }

    @Operation(summary = "Listar minhas consultas", description = "Retorna uma lista paginada com as consultas apenas do paciente autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasRole('PACIENTE')")
    @GetMapping("/minhas-consultas")
    public ResponseEntity<Page<ConsultaAgendamentoResponse>> minhasConsultas(
            @AuthenticationPrincipal Usuario usuario,
            @ParameterObject @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        return ResponseEntity.ok(
                consultaMedicaService.buscarConsultasDoPaciente(usuario.getLogin(), pageable));
    }
}


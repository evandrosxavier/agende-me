package br.com.agendeme.gestao.controller;

import br.com.agendeme.gestao.dto.auth.LoginRequest;
import br.com.agendeme.gestao.dto.auth.TokenResponse;
import br.com.agendeme.gestao.excecoes.BusinessException;
import br.com.agendeme.gestao.excecoes.ErrorCode;
import br.com.agendeme.gestao.model.domain.Usuario;
import br.com.agendeme.gestao.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest dto) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(dto.login(), dto.senha());
            Authentication authentication = authenticationManager.authenticate(authToken);
            String jwt = tokenService.gerarToken((Usuario) authentication.getPrincipal());
            return ResponseEntity.ok(new TokenResponse(jwt));
        } catch (DisabledException e) {
            throw new BusinessException(ErrorCode.USUARIO_INATIVO, HttpStatus.FORBIDDEN);
        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_PASSWORD, HttpStatus.UNAUTHORIZED);
        }
    }
}
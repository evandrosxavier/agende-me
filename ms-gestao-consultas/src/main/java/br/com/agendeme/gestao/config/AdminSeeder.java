package br.com.agendeme.gestao.config;

import br.com.agendeme.gestao.model.domain.Endereco;
import br.com.agendeme.gestao.model.enums.Role;
import br.com.agendeme.gestao.model.enums.Sexo;
import br.com.agendeme.gestao.model.domain.Usuario;
import br.com.agendeme.gestao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByLogin("admin").isEmpty()) {
            Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .email("admin@agendeme.com")
                    .ddd("11")
                    .telefone("000000000")
                    .sexo(Sexo.MASCULINO)
                    .dataNascimento(LocalDate.of(1990, 1, 1))
                    .endereco(Endereco.builder()
                            .logradouro("Sistema")
                            .numero("0")
                            .complemento("")
                            .bairro("Sistema")
                            .cidade("São Paulo")
                            .uf("SP")
                            .cep("00000000")
                            .build())
                    .login("admin")
                    .senha(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .ativo(true)
                    .build();
            usuarioRepository.save(admin);
            log.info("Admin padrão criado: login=admin senha=Admin@123");
        }
    }
}
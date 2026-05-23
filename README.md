# Agende-me

Sistema backend para agendamento e gerenciamento de consultas médicas em ambiente hospitalar, composto por três microserviços independentes com comunicação assíncrona via Apache Kafka.

---

## Arquitetura

```
┌─────────────────────────┐        Kafka Topics         ┌──────────────────────┐
│   ms-gestao-consultas   │ ──────────────────────────► │    ms-notificacao    │
│      (porta 8081)       │                             │      (porta 8082)    │
│  REST API + Swagger UI  │                             │  E-mail ao paciente  │
└─────────────────────────┘                             └──────────────────────┘
           │
           │ Kafka Topics
           ▼
┌─────────────────────────┐
│   ms-hist-consultas     │
│      (porta 8083)       │
│    GraphQL + GraphiQL   │
└─────────────────────────┘
```

Cada microserviço possui seu **próprio banco de dados PostgreSQL**, garantindo isolamento e disponibilidade independente.

---

## Microserviços

### ms-gestao-consultas — porta 8081
Serviço central do sistema. Responsável pelo cadastro e gerenciamento de pacientes, médicos, enfermeiros e consultas médicas. Publica eventos no Kafka sempre que uma consulta é criada ou alterada.

### ms-notificacao — porta 8082
Consome os eventos do Kafka publicados pelo `ms-gestao-consultas` e envia lembretes automáticos por e-mail aos pacientes sobre suas consultas.

### ms-hist-consultas — porta 8083
Consome os mesmos eventos do Kafka e mantém um banco de dados próprio com o histórico completo de eventos de cada consulta. Expõe os dados via GraphQL para consultas flexíveis.

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.x |
| Spring Security | JWT stateless |
| Spring Data JPA | Hibernate |
| Spring for Apache Kafka | Consumer / Producer |
| Spring GraphQL | GraphiQL habilitado |
| PostgreSQL | 16 |
| Docker / Docker Compose | — |
| Lombok | — |
| MapStruct | 1.6.3 |
| SpringDoc OpenAPI (Swagger) | ms-gestao-consultas |

---

## Decisões Técnicas

| Tecnologia | Decisão | Justificativa |
|---|---|---|
| Banco de dados | PostgreSQL por serviço | Isolamento garante disponibilidade independente entre serviços |
| Mensageria | Kafka | Permite múltiplos consumidores independentes no mesmo evento. Escalável para novos serviços |
| API de histórico | GraphQL | Flexibilidade para diferentes clientes consumirem exatamente os campos que precisam |
| Segurança | Spring Security + JWT | Autenticação stateless com autorização por perfil de acesso |
| Especialidade | Herdada do médico | Evita inconsistência entre a especialidade do médico e a informada na consulta |
| Reativação | Campos imutáveis | Nome, sexo, data de nascimento e especialidade identificam a pessoa e não podem ser alterados na reativação |

---

## Níveis de Acesso (ms-gestao-consultas)

| Perfil | Permissões |
|---|---|
| **ADMIN** | Acesso total: cadastrar/inativar/reativar médicos, enfermeiros e pacientes; gerenciar consultas |
| **MEDICO** | Registrar e atualizar atendimentos; visualizar consultas; visualizar médicos e pacientes |
| **ENFERMEIRO** | Agendar e cancelar consultas; cadastrar, atualizar e reativar pacientes; visualizar médicos |
| **PACIENTE** | Visualizar apenas as próprias consultas (`/consultas/minhas-consultas`) |

> O usuário **admin** padrão é criado automaticamente na primeira execução:
> - Login: `admin`
> - Senha: `Admin@123`

---

## Autenticação

Todos os endpoints (exceto `/login`) e as queries GraphQL requerem um token JWT.

### Como obter o token

```http
POST http://localhost:8081/login
Content-Type: application/json

{
  "login": "admin",
  "senha": "Admin@123"
}
```

O token retornado deve ser enviado no header de todas as requisições:

```
Authorization: Bearer <token>
```

> No **Swagger** (`/swagger-ui`): clique em **Authorize** e cole o token.
> No **GraphiQL**: clique em **Headers** (painel inferior) e adicione `{ "Authorization": "Bearer <token>" }`.

### Respostas de erro de autenticação

| Situação | Status | Mensagem |
|---|---|---|
| Login ou senha inválidos | 401 | "Login ou senha inválidos. Tente novamente!" |
| Usuário inativo | 403 | "Usuário inativo. Entre em contato com o administrador." |
| Token ausente ou inválido | 401 | "Token ausente, inválido ou expirado. Autentique-se para continuar." |
| Role insuficiente | 403 | "Você não tem permissão para acessar este recurso." |

---

## Tópicos Kafka

| Tópico | Publicado quando | Consumidores |
|---|---|---|
| `consulta-agendada` | Nova consulta é criada | ms-notificacao, ms-hist-consultas |
| `consulta-agendamento-atualizado` | Dados de agendamento são alterados (data, médico) | ms-notificacao, ms-hist-consultas |
| `consulta-atendimento-registrado` | Atendimento da consulta é registrado | ms-notificacao, ms-hist-consultas |
| `consulta-atendimento-atualizado` | Dados do atendimento são corrigidos | ms-hist-consultas apenas |
| `consulta-cancelada` | Consulta é cancelada | ms-notificacao, ms-hist-consultas |

---

## Endpoints — ms-gestao-consultas (porta 8081)

> Documentação interativa disponível em: `http://localhost:8081/swagger-ui/index.html`

### Autenticação
| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/login` | Público | Autenticação. Retorna token JWT |

### Pacientes
| Método | Endpoint | Roles | Descrição |
|---|---|---|---|
| POST | `/pacientes` | ENFERMEIRO, ADMIN | Cadastrar paciente |
| GET | `/pacientes` | ENFERMEIRO, ADMIN | Listar todos os pacientes (paginado) |
| GET | `/pacientes/ativos` | ENFERMEIRO, ADMIN | Listar pacientes ativos |
| GET | `/pacientes/cpf/{cpf}` | ENFERMEIRO, ADMIN | Buscar por CPF |
| GET | `/pacientes/nome?nome=` | ENFERMEIRO, ADMIN | Buscar por nome (parcial) |
| PATCH | `/pacientes/{id}` | ENFERMEIRO, ADMIN | Atualizar dados |
| DELETE | `/pacientes/{cpf}` | ENFERMEIRO, ADMIN | Inativar paciente |
| PATCH | `/pacientes/{cpf}/reativar` | ENFERMEIRO, ADMIN | Reativar paciente inativo (senha obrigatória) |

### Médicos
| Método | Endpoint | Roles | Descrição |
|---|---|---|---|
| POST | `/medicos` | ADMIN | Cadastrar médico |
| GET | `/medicos` | ENFERMEIRO, ADMIN | Listar todos os médicos |
| GET | `/medicos/ativos` | ENFERMEIRO, ADMIN | Listar médicos ativos |
| GET | `/medicos/crm/{crm}` | MEDICO, ENFERMEIRO, ADMIN | Buscar por CRM |
| GET | `/medicos/nome?nome=` | MEDICO, ENFERMEIRO, ADMIN | Buscar por nome (parcial) |
| GET | `/medicos/especialidade?especialidade=` | ENFERMEIRO, ADMIN | Buscar por especialidade |
| PATCH | `/medicos/{id}` | ADMIN | Atualizar dados |
| DELETE | `/medicos/{crm}` | ADMIN | Inativar médico |
| PATCH | `/medicos/{crm}/reativar` | ADMIN | Reativar médico inativo (senha obrigatória) |

### Enfermeiros
| Método | Endpoint | Roles | Descrição |
|---|---|---|---|
| POST | `/enfermeiros` | ADMIN | Cadastrar enfermeiro |
| GET | `/enfermeiros` | ENFERMEIRO, ADMIN | Listar todos |
| GET | `/enfermeiros/ativos` | ENFERMEIRO, ADMIN | Listar ativos |
| GET | `/enfermeiros/cre/{cre}` | ENFERMEIRO, ADMIN | Buscar por CRE |
| GET | `/enfermeiros/nome?nome=` | ENFERMEIRO, ADMIN | Buscar por nome (parcial) |
| PATCH | `/enfermeiros/{id}` | ADMIN | Atualizar dados |
| DELETE | `/enfermeiros/{cre}` | ADMIN | Inativar enfermeiro |
| PATCH | `/enfermeiros/{cre}/reativar` | ADMIN | Reativar enfermeiro inativo (senha obrigatória) |

### Consultas Médicas
| Método | Endpoint | Roles | Descrição |
|---|---|---|---|
| POST | `/consultas` | MEDICO, ENFERMEIRO, ADMIN | Agendar consulta (especialidade herdada do médico) |
| PATCH | `/consultas/{id}/agendamento` | MEDICO, ENFERMEIRO, ADMIN | Atualizar dados de agendamento |
| PATCH | `/consultas/{id}/atendimento` | MEDICO, ADMIN | Registrar atendimento realizado |
| PATCH | `/consultas/{id}/atendimento/atualizacao` | MEDICO, ADMIN | Corrigir dados do atendimento |
| PATCH | `/consultas/{id}/cancelar` | MEDICO, ENFERMEIRO, ADMIN | Cancelar consulta |
| GET | `/consultas/minhas-consultas` | PACIENTE | Ver consultas do paciente autenticado |
| GET | `/consultas/paciente/{cpf}` | MEDICO, ENFERMEIRO, ADMIN | Buscar por CPF do paciente |
| GET | `/consultas/medico/{crm}` | MEDICO, ENFERMEIRO, ADMIN | Buscar por CRM do médico |
| GET | `/consultas/periodo?inicio=&fim=` | MEDICO, ENFERMEIRO, ADMIN | Buscar por intervalo de datas |
| GET | `/consultas/status?status=` | MEDICO, ENFERMEIRO, ADMIN | Buscar por status |
| GET | `/consultas/especialidade?especialidade=` | MEDICO, ENFERMEIRO, ADMIN | Buscar por especialidade |

---

## Queries GraphQL — ms-hist-consultas (porta 8083)

> Interface interativa disponível em: `http://localhost:8083/graphiql`

### Autenticação no GraphiQL

As queries requerem um token JWT válido. Para autenticar no GraphiQL:

1. Gere o token via `POST /login` no ms-gestao-consultas
2. Acesse `http://localhost:8083/graphiql`
3. No painel inferior, clique em **Headers**
4. Adicione o seguinte header:

```json
{
  "Authorization": "Bearer <seu_token_aqui>"
}
```

> **Atenção:** a página do GraphiQL carrega normalmente (introspecção liberada), mas a execução das queries requer autenticação com role `MEDICO`, `ENFERMEIRO` ou `ADMIN`.

### Queries disponíveis

| Query | Parâmetros | Descrição |
|---|---|---|
| `buscarPorPacienteCpf` | `cpf!`, `page`, `size` | Histórico completo de um paciente pelo CPF |
| `buscarPorPacienteCpfAposData` | `cpf!`, `dataHora!`, `page`, `size` | Consultas a partir de uma data |
| `buscarPorPacienteNome` | `nome!`, `page`, `size` | Busca por nome parcial do paciente |
| `buscarPorMedicoCrm` | `crm!`, `page`, `size` | Histórico por CRM do médico |
| `buscarPorMedicoNome` | `nome!`, `page`, `size` | Busca por nome parcial do médico |
| `buscarPorStatus` | `status!`, `page`, `size` | Filtro por status: AGENDADA, REALIZADA, CANCELADA |

**Exemplo de query:**
```graphql
query {
  buscarPorPacienteCpf(cpf: "12345678900", page: 0, size: 10) {
    consultaId
    pacienteNome
    medicoNome
    especialidade
    dataHora
    status
    tipoEvento
    dataEvento
  }
}
```

---

## Configuração e Execução

### Pré-requisitos
- Docker e Docker Compose instalados
- Java 21
- Maven 3.9+

### 1. Subir a infraestrutura (banco + Kafka)

**Obrigatório: iniciar primeiro o ms-gestao-consultas** (contém Zookeeper, Kafka e seu banco):
```bash
cd ms-gestao-consultas
docker compose up -d
```

Em seguida, subir os bancos dos demais serviços:
```bash
cd ../ms-notificacao
docker compose up -d

cd ../ms-hist-consultas
docker compose up -d
```

### 2. Executar os microserviços

Em terminais separados, na raiz de cada microserviço:

```bash
# Terminal 1
cd ms-gestao-consultas
./mvnw spring-boot:run

# Terminal 2
cd ms-notificacao
./mvnw spring-boot:run

# Terminal 3
cd ms-hist-consultas
./mvnw spring-boot:run
```

### 3. Verificar os serviços

| Serviço | URL |
|---|---|
| ms-gestao-consultas (Swagger) | http://localhost:8081/swagger-ui/index.html |
| ms-hist-consultas (GraphiQL) | http://localhost:8083/graphiql |

---

## Bancos de Dados

| Serviço | Container | Porta | Banco |
|---|---|---|---|
| ms-gestao-consultas | agende-me-db | 5432 | agendeme_db |
| ms-notificacao | agende-me-notificacoes-db | 5433 | agendeme_notificacoes_db |
| ms-hist-consultas | agende-me-historico-db | 5434 | agendeme_historico_db |

Credenciais padrão: `admin` / `admin`

---

## Estrutura do Repositório

```
agende-me/
├── README.md
├── TDD.md                             # Documentação técnica completa
├── ms-gestao-consultas/               # Serviço central (REST API + Kafka Producer)
│   ├── docker-compose.yml             # PostgreSQL + Zookeeper + Kafka
│   └── src/
├── ms-notificacao/                    # Serviço de notificações por e-mail
│   ├── docker-compose.yaml            # Apenas PostgreSQL (Kafka compartilhado)
│   └── src/
└── ms-hist-consultas/                 # Serviço de histórico (GraphQL)
    ├── docker-compose.yml             # Apenas PostgreSQL (Kafka compartilhado)
    └── src/
```

# Relatório Técnico: Sistema Agende-me

**Autor:** Evandro Santos Xavier
**RM:** 368088
**Curso:** Arquitetura e Desenvolvimento Java
**Instituição:** Faculdade de Informática e Administração Paulista (FIAP)
**Data:** 22 de Maio de 2026
**Repositório:** [https://github.com/evandrosxavier/agende-me](https://github.com/evandrosxavier/agende-me)

---

## Sumário

1. [Introdução](#1-introdução)
2. [Arquitetura do Sistema](#2-arquitetura-do-sistema)
3. [Regras de Negócio](#3-regras-de-negócio)
4. [Comunicação Kafka](#4-comunicação-kafka)
5. [Segurança](#5-segurança)
6. [API REST](#6-api-rest)
7. [API GraphQL](#7-api-graphql)
8. [Modelo de Dados](#8-modelo-de-dados)
9. [Configuração e Execução](#9-configuração-e-execução)
10. [Guia de Testes](#10-guia-de-testes)
11. [Stack Tecnológica](#11-stack-tecnológica)
12. [Estrutura do Repositório](#12-estrutura-do-repositório)

---

## 1. Introdução

### 1.1. Descrição do Problema

O sistema **Agende-me** foi desenvolvido para gerenciar o ciclo completo de consultas médicas. A solução proporciona a rastreabilidade dos atendimentos, o envio de notificações automáticas para pacientes e disponibiliza o histórico auditável de todas as consultas registradas em sistema.

### 1.2. Objetivo do Projeto

Desenvolver um backend baseado em microsserviços com Spring Boot para:

- Gerenciar o cadastro de médicos, enfermeiros e pacientes com controle de acesso por perfil (RBAC);
- Permitir o agendamento, atualização, realização e cancelamento de consultas médicas;
- Notificar pacientes por e-mail a cada evento do ciclo de vida de uma consulta;
- Registrar um histórico imutável e auditável de todos os eventos de consultas;
- Expor o histórico de consultas via API GraphQL com paginação e múltiplos filtros.

### 1.3. Escopo

O sistema é composto por três microsserviços independentes que se comunicam de forma assíncrona por meio do Apache Kafka:

| Microsserviço           | Responsabilidade                                                |
|-------------------------|-----------------------------------------------------------------|
| `ms-gestao-consultas`   | Gestão de usuários e consultas; produtor dos eventos Kafka      |
| `ms-notificacao`        | Envia e-mails para pacientes e registra log de notificações     |
| `ms-hist-consultas`     | Persiste histórico de eventos e expõe API GraphQL               |

---

## 2. Arquitetura do Sistema

### 2.1. Descrição da Arquitetura

A solução adota uma arquitetura de **microsserviços com comunicação assíncrona via Apache Kafka**. O `ms-gestao-consultas` é o único ponto de entrada externo para escrita, expondo uma API RESTful protegida por JWT. Os demais serviços são consumidores passivos, reagindo a eventos publicados no Kafka sem acoplamento direto com o produtor.

Cada microsserviço possui seu próprio banco de dados PostgreSQL, garantindo independência de dados e alinhamento com o princípio de isolamento de domínio.

### 2.2. Diagrama da Arquitetura

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Cliente (HTTP/JWT)                           │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ REST (porta 8081)
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      ms-gestao-consultas                             │
│                                                                     │
│  AuthController ──► TokenService (JWT / HMAC256)                    │
│  MedicoController                                                   │
│  EnfermeiroController    ──► Services ──► Repositories              │
│  PacienteController                           │                     │
│  ConsultaMedicaController                     ▼                     │
│                                        PostgreSQL                   │
│                                        agendeme_db:5432             │
│                                               │                     │
│                              KafkaProducerService                   │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ Kafka (porta 9092)
              ┌─────────────────┴──────────────────┐
              ▼                                    ▼
┌─────────────────────────┐          ┌─────────────────────────────┐
│    ms-notificacao        │          │     ms-hist-consultas        │
│    (porta 8082)          │          │     (porta 8083)             │
│                         │          │                              │
│  ConsultaNotificacao    │          │  HistoricoConsultaListener   │
│  Listener               │          │       │                      │
│       │                 │          │       ▼                      │
│       ▼                 │          │  HistoricoConsultaService    │
│  NotificacaoEmail       │          │       │                      │
│  Service (Gmail SMTP)   │          │       ▼                      │
│       │                 │          │  PostgreSQL                  │
│       ▼                 │          │  agendeme_historico_db:5434  │
│  PostgreSQL             │          │       │                      │
│  agendeme_notificacoes  │          │       ▼                      │
│  _db:5433               │          │  GraphQL API (/graphql)      │
└─────────────────────────┘          └─────────────────────────────┘
```

### 2.3. Tabela de Microsserviços

| Atributo             | ms-gestao-consultas              | ms-notificacao                      | ms-hist-consultas                |
|----------------------|----------------------------------|-------------------------------------|----------------------------------|
| **Porta HTTP**       | 8081                             | 8082                                | 8083                             |
| **Banco de Dados**   | agendeme_db (5432)               | agendeme_notificacoes_db (5433)     | agendeme_historico_db (5434)     |
| **Papel Kafka**      | Produtor                         | Consumidor (group: ms-notificacoes) | Consumidor (group: ms-historico) |
| **API exposta**      | REST + Swagger                   | Nenhuma (serviço interno)           | GraphQL (+ GraphiQL)             |
| **Segurança**        | JWT (emite e valida)             | N/A                                 | JWT (valida token externo)       |
| **Tecnologia extra** | MapStruct, AdminSeeder           | JavaMail (Gmail SMTP)               | Spring GraphQL                   |

### 2.4. Decisões Técnicas

| Decisão                              | Justificativa                                                                                      |
|--------------------------------------|----------------------------------------------------------------------------------------------------|
| Kafka para integração entre serviços | Desacoplamento total entre produtor e consumidores; resiliência a falhas temporárias nos consumidores |
| Banco de dados separado por serviço  | Isolamento de domínio; cada serviço evolui seu schema de forma independente                       |
| Herança JOINED no JPA                | Tabelas separadas para médico, paciente e enfermeiro com campos específicos, sem colunas nulas     |
| GraphQL para histórico               | Consultas flexíveis com múltiplos filtros e paginação sem proliferar endpoints REST                |
| JWT stateless                        | Sem necessidade de armazenamento de sessão; adequado para microsserviços                          |
| AdminSeeder                          | Garante a existência de um usuário ADMIN ao iniciar o serviço, sem necessidade de script manual   |
| Inativação lógica (soft delete)      | Preserva integridade referencial e auditabilidade; registros históricos continuam válidos          |
| MapStruct para mapeamento            | Mapeamento em tempo de compilação; mais performático e type-safe que reflexão em runtime           |
| Padrão ProblemDetail (RFC 7807)      | Respostas de erro padronizadas e legíveis por máquina                                             |
| Especialidade herdada do médico      | Evita inconsistência entre a especialidade do médico e a informada na consulta                    |
| Campos imutáveis na reativação       | Nome, sexo, data de nascimento e especialidade identificam a pessoa e não devem ser alterados     |
| Atualização por identificador de negócio | CPF, CRM ou CRE no path de atualização e inativação; consistência com endpoints de busca e evita exposição de IDs internos |
| Nome imutável na atualização         | Preserva integridade do prontuário; mudança de nome exige inativar e reativar o cadastro          |
| Dois tipos GraphQL no histórico      | `HistoricoConsulta` (com dados clínicos) para profissionais; `HistoricoConsultaResumo` (sem dados clínicos) para pacientes |
| Queries de último estado vs. auditoria | Profissionais de saúde veem o estado atual de cada consulta; ADMIN acessa o ciclo de vida completo para fins de auditoria |

---

## 3. Regras de Negócio

### 3.1. Perfis de Acesso (RBAC)

O sistema utiliza quatro perfis de acesso definidos no enum `Role`:

| Role          | Descrição                                                                    |
|---------------|------------------------------------------------------------------------------|
| `ADMIN`       | Acesso total. Único perfil que pode cadastrar e inativar médicos e enfermeiros; acesso às queries de auditoria no GraphQL |
| `ENFERMEIRO`  | Gerencia pacientes e consulta dados de médicos/enfermeiros; agenda consultas; acesso às queries de último estado no GraphQL |
| `MEDICO`      | Agenda e realiza consultas; acessa dados de médicos e pacientes; acesso às queries de último estado no GraphQL |
| `PACIENTE`    | Visualiza apenas suas próprias consultas via `/consultas/minhas-consultas` (REST) e `minhasConsultas` (GraphQL) sem dados clínicos |

### 3.2. Ciclo de Vida da Consulta

Uma consulta médica possui três estados possíveis e transições controladas:

```
                    ┌─────────────┐
          POST      │             │   PATCH /{id}/agendamento
        /consultas  │  AGENDADA   │◄──────────────────────────
   ─────────────────►             │
                    └──────┬──────┘
                           │
              ┌────────────┴───────────────┐
              │                            │
   PATCH /{id}/atendimento      PATCH /{id}/cancelar
              │                            │
              ▼                            ▼
     ┌────────────────┐          ┌──────────────────┐
     │    REALIZADA   │          │    CANCELADA      │
     │                │          │                   │
     │ PATCH /{id}/   │          │  (estado final,   │
     │ atendimento/   │          │   não pode mais   │
     │ atualizacao    │          │   ser alterada)   │
     └────────────────┘          └──────────────────┘
```

**Regras de transição:**

- `AGENDADA → REALIZADA`: apenas via `PATCH /consultas/{id}/atendimento`; somente roles `MEDICO` ou `ADMIN`
- `AGENDADA → CANCELADA`: via `PATCH /consultas/{id}/cancelar`; roles `MEDICO`, `ENFERMEIRO` ou `ADMIN`
- `REALIZADA → REALIZADA (atualização)`: via `PATCH /consultas/{id}/atendimento/atualizacao`; somente `MEDICO` ou `ADMIN`
- Consulta `CANCELADA` **não pode** ser cancelada novamente (`CONSULTA_JA_CANCELADA`)
- Consulta `REALIZADA` **não pode** ser cancelada (`CONSULTA_NAO_PODE_SER_CANCELADA`)
- Consulta **não `AGENDADA`** não pode ter atendimento registrado (`CONSULTA_NAO_PODE_SER_REGISTRADA`)
- Consulta **não `REALIZADA`** não pode ter atendimento atualizado (`CONSULTA_NAO_ESTA_REALIZADA`)

**Regras de validação no agendamento (`POST /consultas`):**

- Paciente deve existir e estar **ativo**
- Médico deve existir e estar **ativo**
- A especialidade da consulta é **herdada automaticamente** da especialidade cadastrada do médico — não é informada pelo solicitante
- Data e hora devem ser presentes ou futuras (`@FutureOrPresent`)

**Regras de validação na atualização de agendamento (`PATCH /consultas/{id}/agendamento`):**

- O **paciente** da consulta deve continuar **ativo** no momento da atualização
- Se `medicoId` for informado: o novo médico deve existir e estar **ativo**; a especialidade da consulta é atualizada automaticamente para a do novo médico
- Se `medicoId` não for informado: valida se o **médico atual** ainda está **ativo** (pode ter sido inativado após o agendamento)

**Regras de validação nas buscas de consultas:**

- `GET /consultas/paciente/{cpf}`: retorna 404 se o CPF não pertencer a nenhum paciente cadastrado
- `GET /consultas/medico/{crm}`: retorna 404 se o CRM não pertencer a nenhum médico cadastrado
- `GET /consultas/periodo`: a data de início **não pode ser posterior** à data de fim; retorna 400 (`PERIODO_INVALIDO`)
- `GET /consultas/minhas-consultas`: usa o **login do usuário autenticado** para resolver automaticamente o CPF do paciente; não requer parâmetro externo

### 3.3. Regras por Entidade

#### Médico
- CRM + UF formam uma combinação única no sistema
- Somente `ADMIN` pode cadastrar, inativar ou reativar médicos
- Inativação é lógica (`ativo = false`); não há exclusão física
- Médico inativo não pode ser vinculado a novas consultas nem ter dados alterados
- A especialidade do médico é propagada automaticamente para a consulta no momento do agendamento
- **Campos atualizáveis:** `ddd`, `telefone`, `endereco`
- **Campos imutáveis na atualização:** `nome`, `crm`, `crmUf`, `especialidade`, `sexo`, `dataNascimento`, `login`
- **Cadastro com CRM ativo existente**: retorna 409 (`CRM_JA_CADASTRADO`)
- **Cadastro com CRM inativo existente**: retorna 422 (`MEDICO_CADASTRO_INATIVO`) orientando o uso do endpoint de reativação

#### Enfermeiro
- CRE + UF formam uma combinação única no sistema
- Somente `ADMIN` pode cadastrar, inativar ou reativar enfermeiros
- Inativação é lógica; não há exclusão física
- **Campos atualizáveis:** `ddd`, `telefone`, `endereco`
- **Campos imutáveis na atualização:** `nome`, `cre`, `creUf`, `sexo`, `dataNascimento`, `login`
- **Cadastro com CRE ativo existente**: retorna 409 (`CRE_JA_CADASTRADO`)
- **Cadastro com CRE inativo existente**: retorna 422 (`ENFERMEIRO_CADASTRO_INATIVO`) orientando o uso do endpoint de reativação

#### Paciente
- CPF é único no sistema
- Cadastro e inativação por `ENFERMEIRO` ou `ADMIN`
- Paciente inativo não pode ter novas consultas agendadas ou existentes alteradas
- **Campos atualizáveis:** `ddd`, `telefone`, `endereco`
- **Campos imutáveis na atualização:** `nome`, `cpf`, `sexo`, `dataNascimento`, `login`
- **Cadastro com CPF ativo existente**: retorna 409 (`CPF_JA_CADASTRADO`)
- **Cadastro com CPF inativo existente**: retorna 422 (`PACIENTE_CADASTRO_INATIVO`) orientando o uso do endpoint de reativação

#### Unicidade Global de Login e E-mail
- `login` e `email` são únicos em toda a tabela `usuarios`, abrangendo registros **ativos e inativos**
- Validados apenas quando o CPF/CRM/CRE não existe na base — se existir (ativo ou inativo), a exceção é lançada antes de chegar na validação de login/email
- Essa ordem de validação garante que o atendente sempre receba a mensagem mais relevante e orientativa

### 3.4. Regras de Reativação

O sistema permite reativar um registro inativo pelo seu identificador único (CPF, CRM ou CRE) via endpoints dedicados, sem criar um novo registro. Isso preserva o histórico de consultas vinculado ao `id` original da entidade.

**Endpoints:**
- `PATCH /pacientes/{cpf}/reativar` — roles `ENFERMEIRO`, `ADMIN`
- `PATCH /medicos/{crm}/reativar` — role `ADMIN`
- `PATCH /enfermeiros/{cre}/reativar` — role `ADMIN`

**Campos editáveis e imutáveis:**

| Campo                                             | Comportamento na reativação                                      |
|---------------------------------------------------|------------------------------------------------------------------|
| `senha`                                           | **Obrigatória.** Sempre redefinida com BCrypt                    |
| `email`                                           | Opcional. Se informado, validado contra outros usuários          |
| `ddd`, `telefone`, `endereco`                     | Opcionais. Apenas os campos enviados são atualizados             |
| `nome`, `sexo`, `dataNascimento`                  | **Imutáveis.** Identificam a pessoa; não aceitos no payload      |
| `especialidade`                                   | **Imutável** (somente médico). Atrelada ao registro profissional |
| `login`, `cpf` / `crm` / `cre`                   | **Imutáveis.** Identificadores permanentes                       |

**Validações:**
- Registro não encontrado → 404
- Registro já ativo → 422 (`PACIENTE_JA_ATIVO` / `MEDICO_JA_ATIVO` / `ENFERMEIRO_JA_ATIVO`)
- Novo e-mail já pertence a outro usuário → 409 (`EMAIL_ALREADY_EXISTS`)

---

## 4. Comunicação Kafka

### 4.1. Infraestrutura

| Componente   | Imagem                             | Porta  | Container                  |
|--------------|------------------------------------|--------|----------------------------|
| Zookeeper    | `confluentinc/cp-zookeeper:7.5.0`  | 2181   | `agende-me-zookeeper`      |
| Kafka Broker | `confluentinc/cp-kafka:7.5.0`      | 9092   | `agende-me-kafka`          |

O Kafka é declarado no `docker-compose.yml` do `ms-gestao-consultas` e **compartilhado** pelos demais microsserviços.

### 4.2. Tópicos e Gatilhos

| Tópico                            | Gatilho (operação)                              | Consumidores                         | Tipo de Evento          |
|-----------------------------------|-------------------------------------------------|--------------------------------------|-------------------------|
| `consulta-agendada`               | `POST /consultas`                               | ms-notificacao, ms-hist-consultas    | `CRIACAO`               |
| `consulta-agendamento-atualizado` | `PATCH /consultas/{id}/agendamento`             | ms-notificacao, ms-hist-consultas    | `ALTERACAO_AGENDAMENTO` |
| `consulta-atendimento-registrado` | `PATCH /consultas/{id}/atendimento`             | ms-notificacao, ms-hist-consultas    | `ATENDIMENTO_REALIZADO` |
| `consulta-atendimento-atualizado` | `PATCH /consultas/{id}/atendimento/atualizacao` | ms-hist-consultas **apenas**         | `ALTERACAO_ATENDIMENTO` |
| `consulta-cancelada`              | `PATCH /consultas/{id}/cancelar`                | ms-notificacao, ms-hist-consultas    | `CANCELAMENTO`          |

> O tópico `consulta-atendimento-atualizado` não é consumido pelo `ms-notificacao` pois a correção de dados clínicos é uma operação interna que não requer notificação ao paciente.

### 4.3. Payload da Mensagem (ConsultaNotificacaoDTO)

Todos os tópicos utilizam o mesmo payload JSON:

```json
{
  "consultaId": 10,
  "pacienteNome": "Maria Clara Santos",
  "pacienteCpf": "12345678900",
  "pacienteEmail": "maria@email.com",
  "medicoNome": "Dr. Carlos Ferreira",
  "medicoCrm": "123456",
  "especialidade": "CARDIOLOGIA",
  "dataHora": "2026-06-15T14:30:00",
  "status": "AGENDADA",
  "dataEvento": "2026-05-22T10:00:00",
  "diagnostico": null,
  "tratamentoProposto": null,
  "demaisObservacoes": null
}
```

### 4.4. Consumer Groups

| Consumer Group     | Microsserviço       | Comportamento              |
|--------------------|---------------------|----------------------------|
| `ms-notificacoes`  | ms-notificacao      | Envio de e-mail + log      |
| `ms-historico`     | ms-hist-consultas   | Persistência do histórico  |

Cada grupo consome independentemente todos os tópicos assinados, garantindo que ambos os serviços processem cada evento sem conflito.

---

### 4.5. Notificações por E-mail (ms-notificacao)

O `ms-notificacao` envia **quatro tipos de e-mail** ao paciente, cada um disparado por um tópico Kafka diferente. O envio é feito via **Gmail SMTP** (conta `app.agende.me@gmail.com`).

> **Dica para testar o fluxo ponta a ponta:** ao cadastrar um paciente, informe um **e-mail válido e acessível**. Você verá as notificações chegando na caixa de entrada a cada evento do ciclo de vida da consulta.

### Tipos de Notificação

| Tipo de Evento             | Tópico Kafka                        | Assunto do E-mail                   | Status da Consulta |
|----------------------------|-------------------------------------|-------------------------------------|--------------------|
| `CRIACAO`                  | `consulta-agendada`                 | "Consulta Agendada - Agende-me"     | AGENDADA           |
| `ALTERACAO_AGENDAMENTO`    | `consulta-agendamento-atualizado`   | "Consulta Atualizada - Agende-me"   | ATUALIZADA         |
| `ATENDIMENTO_REALIZADO`    | `consulta-atendimento-registrado`   | "Consulta Realizada - Agende-me"    | REALIZADA          |
| `CANCELAMENTO`             | `consulta-cancelada`                | "Consulta Cancelada - Agende-me"    | CANCELADA          |

> O evento `ALTERACAO_ATENDIMENTO` (correção de dados clínicos) **não gera notificação ao paciente** — é uma operação interna registrada apenas no histórico.

### Exemplos de Corpo de E-mail

**1. Consulta Agendada**
```
Olá, Maria Clara Santos!

Sua consulta foi agendada com sucesso.

Médico:       Dr. Carlos Ferreira
Especialidade: CARDIOLOGIA
Data e Hora:  01/07/2026 às 10:00

Em caso de dúvidas ou necessidade de cancelamento, entre em contato com nossa equipe.

Atenciosamente,
Equipe Agende-me
```

**2. Agendamento Atualizado**
```
Olá, Maria Clara Santos!

Os dados da sua consulta foram atualizados.

Médico:       Dr. Carlos Ferreira
Especialidade: CARDIOLOGIA
Nova Data e Hora: 10/07/2026 às 14:30

Qualquer dúvida, entre em contato.

Atenciosamente,
Equipe Agende-me
```

**3. Atendimento Realizado**
```
Olá, Maria Clara Santos!

Seu atendimento foi registrado com sucesso.

Médico:       Dr. Carlos Ferreira
Especialidade: CARDIOLOGIA
Data e Hora:  01/07/2026 às 10:00

Obrigado por utilizar o Agende-me!

Atenciosamente,
Equipe Agende-me
```

**4. Consulta Cancelada**
```
Olá, Maria Clara Santos!

Sua consulta foi cancelada.

Médico:       Dr. Carlos Ferreira
Especialidade: CARDIOLOGIA
Data e Hora:  01/07/2026 às 10:00

Se desejar reagendar, entre em contato com nossa equipe.

Atenciosamente,
Equipe Agende-me
```

### Log de Notificações

Cada envio (ou falha de envio) é persistido na tabela `notificacoes_log` do banco `agendeme_notificacoes_db`:

```
consulta_id | tipo_evento          | destinatario          | assunto                          | status_consulta | status  | data_envio
------------|----------------------|-----------------------|----------------------------------|-----------------|---------|-----------------------------
10          | CRIACAO              | maria@email.com       | Consulta Agendada - Agende-me    | AGENDADA        | ENVIADO | 2026-07-01T09:00:00
10          | ALTERACAO_AGENDAMENTO| maria@email.com       | Consulta Atualizada - Agende-me  | ATUALIZADA      | ENVIADO | 2026-07-02T11:00:00
10          | ATENDIMENTO_REALIZADO| maria@email.com       | Consulta Realizada - Agende-me   | REALIZADA       | ENVIADO | 2026-07-10T14:35:00
```

Em caso de falha no envio (SMTP indisponível, e-mail inválido), o campo `status` é gravado como `FALHA` e `mensagem_erro` contém o detalhe da exceção.

---

## 5. Segurança

### 5.1. Estratégia de Autenticação

O sistema utiliza **JWT (JSON Web Token)** com algoritmo `HMAC256`. O token é gerado pelo `ms-gestao-consultas` e compartilhado como segredo com o `ms-hist-consultas`. O `ms-notificacao` não possui endpoints externos e não exige autenticação.

### 5.2. Estrutura do Token JWT

| Campo       | Valor                                              |
|-------------|----------------------------------------------------|
| `iss`       | `agende-me`                                        |
| `sub`       | login do usuário                                   |
| `role`      | role do usuário (ex: `ADMIN`)                      |
| `cpf`       | CPF do paciente — **presente apenas quando `role = PACIENTE`** |
| `exp`       | 2 horas após emissão (UTC-3)                       |
| Algoritmo   | HMAC256                                            |
| Secret      | `agende-me-secret-key-2026`                        |

> O claim `cpf` é usado pelo `ms-hist-consultas` para autenticar a query `minhasConsultas` sem que o paciente precise informar o CPF como parâmetro.

### 5.3. AdminSeeder — Usuário Padrão

Ao iniciar o `ms-gestao-consultas`, o `AdminSeeder` verifica se o login `admin` existe no banco. Caso não exista, cria automaticamente o usuário abaixo:

| Campo          | Valor                   |
|----------------|-------------------------|
| Login          | `admin`                 |
| Senha          | `Admin@123`             |
| Role           | `ADMIN`                 |
| E-mail         | `admin@agendeme.com`    |
| Status         | Ativo                   |

> **Importante:** As credenciais padrão devem ser alteradas em ambiente de produção.

### 5.4. Fluxo de Autenticação e Autorização

```
Cliente
  │
  ├── POST /login  {login, senha}
  │       │
  │       ▼
  │   AuthController
  │   try { authenticationManager.authenticate() }
  │   catch (BadCredentialsException) → 401 "Login ou senha inválidos"
  │   catch (DisabledException)       → 403 "Usuário inativo"
  │   TokenService.gerarToken()
  │       │
  │       ▼
  │   { "token": "eyJhbGciOiJIUzI1NiJ9..." }
  │
  ├── Requisição com header: Authorization: Bearer <token>
  │       │
  │       ▼
  │   SecurityFilter (OncePerRequestFilter)
  │   TokenService.getSubject(token)
  │   → token inválido/expirado: retorna null silenciosamente
  │   → token válido: UsuarioRepository.findByLogin(login)
  │   SecurityContextHolder.setAuthentication(...)
  │       │
  │       ▼
  │   @PreAuthorize("hasRole('...')") ← verifica role
  │       │
  │       ▼
  │   Controller / Service
  │
  ├── Requisição sem token ou token inválido
  │       │
  │       ▼
  │   AuthenticationEntryPoint → 401 "Token ausente, inválido ou expirado"
  │
  └── Token válido, mas role insuficiente
          │
          ▼
      AccessDeniedHandler → 403 "Você não tem permissão para acessar este recurso"
```

### 5.5. Tratamento de Erros de Autenticação

| Situação                              | Status HTTP | Mensagem                                            |
|---------------------------------------|-------------|-----------------------------------------------------|
| Login ou senha inválidos              | 401         | "Login ou senha inválidos. Tente novamente!"        |
| Usuário inativo tentando logar        | 403         | "Usuário inativo. Entre em contato com o administrador." |
| Token ausente ou inválido             | 401         | "Token ausente, inválido ou expirado. Autentique-se para continuar." |
| Token válido, role insuficiente       | 403         | "Você não tem permissão para acessar este recurso." |

> Todas as respostas seguem o padrão **ProblemDetail (RFC 7807)**.

### 5.6. Endpoints Públicos

| Serviço              | Endpoint          | Método | Descrição                             |
|----------------------|-------------------|--------|---------------------------------------|
| ms-gestao-consultas  | `/login`          | POST   | Autenticação e geração de JWT         |
| ms-gestao-consultas  | `/swagger-ui/**`  | GET    | Documentação interativa da API        |
| ms-gestao-consultas  | `/v3/api-docs/**` | GET    | Especificação OpenAPI                 |
| ms-hist-consultas    | `/graphiql/**`    | GET    | Interface interativa do GraphQL       |
| ms-hist-consultas    | `/graphql`        | POST   | Endpoint GraphQL (introspecção livre; queries protegidas por `@PreAuthorize`) |

> **Nota sobre o GraphQL:** o endpoint `/graphql` é liberado para permitir que o GraphiQL carregue o schema (introspecção). As queries de dados permanecem protegidas por `@PreAuthorize` nos resolvers, exigindo token JWT válido com role `MEDICO`, `ENFERMEIRO` ou `ADMIN`.

---

## 6. API REST

Base URL: `http://localhost:8081`
Documentação interativa: `http://localhost:8081/swagger-ui/index.html`

### 6.1. Autenticação

| Método | Rota     | Roles   | Descrição                                |
|--------|----------|---------|------------------------------------------|
| POST   | `/login` | Pública | Autentica o usuário e retorna token JWT  |

**Request Body:**
```json
{ "login": "admin", "senha": "Admin@123" }
```
**Response (200):**
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

### 6.2. Médicos (`/medicos`)

| Método | Rota                                         | Roles                     | Descrição                                        |
|--------|----------------------------------------------|---------------------------|--------------------------------------------------|
| POST   | `/medicos`                                   | ADMIN                     | Cadastra novo médico                             |
| GET    | `/medicos/crm/{crm}`                         | MEDICO, ENFERMEIRO, ADMIN | Busca médico pelo CRM                            |
| GET    | `/medicos/nome?nome={termo}`                 | MEDICO, ENFERMEIRO, ADMIN | Busca médicos por nome (paginado)                |
| GET    | `/medicos/especialidade?especialidade={enum}`| ENFERMEIRO, ADMIN         | Filtra médicos por especialidade (paginado)      |
| GET    | `/medicos`                                   | ENFERMEIRO, ADMIN         | Lista todos os médicos (paginado)                |
| GET    | `/medicos/ativos`                            | ENFERMEIRO, ADMIN         | Lista médicos ativos (paginado)                  |
| PATCH  | `/medicos/{crm}`                             | ADMIN                     | Atualiza ddd, telefone e endereço                |
| DELETE | `/medicos/{crm}`                             | ADMIN                     | Inativação lógica do médico                      |
| PATCH  | `/medicos/{crm}/reativar`                    | ADMIN                     | Reativa médico inativo; senha obrigatória        |

> **Campos imutáveis na atualização:** `nome`, `crm`, `crmUf`, `especialidade`, `sexo`, `dataNascimento`, `login`.

### 6.3. Enfermeiros (`/enfermeiros`)

| Método | Rota                            | Roles             | Descrição                                       |
|--------|---------------------------------|-------------------|-------------------------------------------------|
| POST   | `/enfermeiros`                  | ADMIN             | Cadastra novo enfermeiro                        |
| GET    | `/enfermeiros/cre/{cre}`        | ENFERMEIRO, ADMIN | Busca enfermeiro pelo CRE                       |
| GET    | `/enfermeiros/nome?nome={termo}`| ENFERMEIRO, ADMIN | Busca enfermeiros por nome (paginado)           |
| GET    | `/enfermeiros`                  | ENFERMEIRO, ADMIN | Lista todos os enfermeiros (paginado)           |
| GET    | `/enfermeiros/ativos`           | ENFERMEIRO, ADMIN | Lista enfermeiros ativos (paginado)             |
| PATCH  | `/enfermeiros/{cre}`            | ADMIN             | Atualiza ddd, telefone e endereço               |
| DELETE | `/enfermeiros/{cre}`            | ADMIN             | Inativação lógica do enfermeiro                 |
| PATCH  | `/enfermeiros/{cre}/reativar`   | ADMIN             | Reativa enfermeiro inativo; senha obrigatória   |

> **Campos imutáveis na atualização:** `nome`, `cre`, `creUf`, `sexo`, `dataNascimento`, `login`.

### 6.4. Pacientes (`/pacientes`)

| Método | Rota                            | Roles             | Descrição                                      |
|--------|---------------------------------|-------------------|------------------------------------------------|
| POST   | `/pacientes`                    | ENFERMEIRO, ADMIN | Cadastra novo paciente                         |
| GET    | `/pacientes/cpf/{cpf}`          | ENFERMEIRO, ADMIN | Busca paciente pelo CPF                        |
| GET    | `/pacientes/nome?nome={termo}`  | ENFERMEIRO, ADMIN | Busca pacientes por nome (paginado)            |
| GET    | `/pacientes`                    | ENFERMEIRO, ADMIN | Lista todos os pacientes (paginado)            |
| GET    | `/pacientes/ativos`             | ENFERMEIRO, ADMIN | Lista pacientes ativos (paginado)              |
| PATCH  | `/pacientes/{cpf}`              | ENFERMEIRO, ADMIN | Atualiza ddd, telefone e endereço              |
| DELETE | `/pacientes/{cpf}`              | ENFERMEIRO, ADMIN | Inativação lógica do paciente                  |
| PATCH  | `/pacientes/{cpf}/reativar`     | ENFERMEIRO, ADMIN | Reativa paciente inativo; senha obrigatória    |

> **Campos imutáveis na atualização:** `nome`, `cpf`, `sexo`, `dataNascimento`, `login`.

### 6.5. Consultas Médicas (`/consultas`)

| Método | Rota                                        | Roles                     | Descrição                                                            |
|--------|---------------------------------------------|---------------------------|----------------------------------------------------------------------|
| POST   | `/consultas`                                | MEDICO, ENFERMEIRO, ADMIN | Agenda nova consulta; dispara `consulta-agendada`                    |
| PATCH  | `/consultas/{id}/agendamento`               | MEDICO, ENFERMEIRO, ADMIN | Atualiza data/hora ou médico; dispara `consulta-agendamento-atualizado` |
| PATCH  | `/consultas/{id}/atendimento`               | MEDICO, ADMIN             | Registra atendimento (AGENDADA→REALIZADA); dispara `consulta-atendimento-registrado` |
| PATCH  | `/consultas/{id}/atendimento/atualizacao`   | MEDICO, ADMIN             | Atualiza dados clínicos de consulta realizada; dispara `consulta-atendimento-atualizado` |
| PATCH  | `/consultas/{id}/cancelar`                  | MEDICO, ENFERMEIRO, ADMIN | Cancela consulta agendada; dispara `consulta-cancelada`              |
| GET    | `/consultas/paciente/{cpf}`                 | MEDICO, ENFERMEIRO, ADMIN | Lista consultas de um paciente pelo CPF (paginado)                   |
| GET    | `/consultas/medico/{crm}`                   | MEDICO, ENFERMEIRO, ADMIN | Lista consultas de um médico pelo CRM (paginado)                     |
| GET    | `/consultas/periodo?inicio={}&fim={}`       | MEDICO, ENFERMEIRO, ADMIN | Filtra por intervalo de datas (`yyyy-MM-dd'T'HH:mm:ss`, paginado)   |
| GET    | `/consultas/status?status={enum}`           | MEDICO, ENFERMEIRO, ADMIN | Filtra por status (AGENDADA, REALIZADA, CANCELADA, paginado)         |
| GET    | `/consultas/especialidade?especialidade={}` | MEDICO, ENFERMEIRO, ADMIN | Filtra por especialidade (paginado)                                  |
| GET    | `/consultas/minhas-consultas`               | PACIENTE                  | Lista as consultas do paciente autenticado (paginado)                |

### 6.6. Parâmetros de Paginação

Todos os endpoints paginados aceitam os seguintes parâmetros de query (Spring `Pageable`):

| Parâmetro | Padrão | Descrição                                   |
|-----------|--------|---------------------------------------------|
| `page`    | `0`    | Número da página (base zero)                |
| `size`    | `10`   | Quantidade de registros por página          |
| `sort`    | —      | Campo de ordenação (ex: `nome`, `dataHora`) |

### 6.7. Respostas de Erro

Todas as respostas de erro seguem o padrão **ProblemDetail (RFC 7807)**:

```json
{
  "type": "https://api.agendeme.com/errors/business-error",
  "title": "Erro de Negócio",
  "status": 422,
  "detail": "A consulta informada já está cancelada.",
  "instance": "/consultas/5/cancelar"
}
```

| Código HTTP | Situação                                             |
|-------------|------------------------------------------------------|
| 400         | Dados inválidos / parâmetros fora do formato         |
| 401         | Token ausente, inválido ou expirado                  |
| 403         | Role insuficiente para o recurso / usuário inativo   |
| 404         | Entidade não encontrada                              |
| 409         | Conflito (e-mail, login, CPF, CRM ou CRE duplicados) |
| 422         | Regra de negócio violada (inativo, já cancelado...)  |
| 500         | Erro interno do servidor                             |

---

## 7. API GraphQL

Base URL: `http://localhost:8083/graphql`
Interface interativa: `http://localhost:8083/graphiql`

### 7.1. Autenticação no GraphQL

O `ms-hist-consultas` **não possui endpoint de login próprio**. O token JWT deve ser gerado no `ms-gestao-consultas` e reutilizado aqui.

**Passo a passo:**

**1. Gerar o token:**
```http
POST http://localhost:8081/login
Content-Type: application/json

{ "login": "admin", "senha": "Admin@123" }
```

**2. Usar o token no GraphiQL:**
- Acesse `http://localhost:8083/graphiql`
- Clique na aba **Headers** (painel inferior)
- Adicione:
```json
{ "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9..." }
```

**3. Usar o token em chamadas diretas (Postman):**
```http
POST http://localhost:8083/graphql
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{ "query": "{ buscarPorStatus(status: \"AGENDADA\") { consultaId status } }" }
```

> **Introspecção:** liberada sem token para permitir o carregamento do schema no GraphiQL. As queries de dados permanecem protegidas por `@PreAuthorize`.

### 7.2. Tipos GraphQL

O serviço expõe dois tipos distintos conforme o perfil do usuário:

#### `HistoricoConsulta` — para MEDICO, ENFERMEIRO e ADMIN

```graphql
type HistoricoConsulta {
    id: ID
    consultaId: ID
    pacienteNome: String
    pacienteCpf: String
    pacienteEmail: String
    medicoNome: String
    medicoCrm: String
    especialidade: String
    dataHora: String          # formato: yyyy-MM-dd'T'HH:mm:ss
    status: String            # AGENDADA | ATUALIZADA | REALIZADA | CANCELADA
    dataEvento: String        # formato: yyyy-MM-dd'T'HH:mm:ss
    dataDoRegistro: String    # formato: yyyy-MM-dd'T'HH:mm:ss
    diagnostico: String       # dados clínicos — visível apenas neste tipo
    tratamentoProposto: String
    demaisObservacoes: String
}
```

#### `HistoricoConsultaResumo` — exclusivo para PACIENTE

```graphql
type HistoricoConsultaResumo {
    id: ID
    consultaId: ID
    pacienteNome: String
    pacienteCpf: String
    pacienteEmail: String
    medicoNome: String
    medicoCrm: String
    especialidade: String
    dataHora: String
    status: String
    dataEvento: String
    dataDoRegistro: String
    # sem campos clínicos
}
```

### 7.3. Queries de Último Estado

Retornam **apenas o registro mais recente** de cada consulta (determinado pelo `MAX(dataEvento)` por `consultaId`). Refletem a situação atual da consulta, sem o histórico de alterações.

| Query                          | Parâmetros obrigatórios           | Roles                     | Tipo de retorno         |
|--------------------------------|-----------------------------------|---------------------------|-------------------------|
| `buscarPorPacienteCpf`         | `cpf: String!`                    | MEDICO, ENFERMEIRO, ADMIN | `[HistoricoConsulta]`   |
| `buscarPorPacienteCpfAposData` | `cpf: String!, dataHora: String!` | MEDICO, ENFERMEIRO, ADMIN | `[HistoricoConsulta]`   |
| `buscarPorPacienteNome`        | `nome: String!`                   | MEDICO, ENFERMEIRO, ADMIN | `[HistoricoConsulta]`   |
| `buscarPorMedicoCrm`           | `crm: String!`                    | MEDICO, ENFERMEIRO, ADMIN | `[HistoricoConsulta]`   |
| `buscarPorMedicoNome`          | `nome: String!`                   | MEDICO, ENFERMEIRO, ADMIN | `[HistoricoConsulta]`   |
| `buscarPorStatus`              | `status: String!`                 | MEDICO, ENFERMEIRO, ADMIN | `[HistoricoConsulta]`   |
| `buscarPorPeriodo`             | `inicio: String!, fim: String!`   | MEDICO, ENFERMEIRO, ADMIN | `[HistoricoConsulta]`   |
| `minhasConsultas`              | —                                 | PACIENTE                  | `[HistoricoConsultaResumo]` |

> `minhasConsultas` não requer parâmetros — o CPF é extraído automaticamente do claim `cpf` no token JWT do paciente autenticado.

### 7.4. Queries de Auditoria (ciclo de vida completo)

Retornam **todos os eventos** de cada consulta em ordem cronológica. Disponíveis exclusivamente para `ADMIN`.

| Query                          | Parâmetros obrigatórios              | Descrição                                          |
|--------------------------------|--------------------------------------|----------------------------------------------------|
| `auditoriaBuscarPorCpf`        | `cpf: String!`                       | Todos os eventos de consultas de um paciente       |
| `auditoriaBuscarPorPeriodo`    | `inicio: String!, fim: String!`      | Todos os eventos registrados em um intervalo       |
| `auditoriaBuscarPorConsultaId` | `consultaId: ID!`                    | Ciclo de vida completo de uma consulta específica  |

### 7.5. Exemplos de Consultas

**Último estado com dados clínicos (médico):**
```graphql
query {
  buscarPorPacienteCpf(cpf: "12345678900", page: 0, size: 5) {
    consultaId
    pacienteNome
    medicoNome
    especialidade
    dataHora
    status
    diagnostico
    tratamentoProposto
    demaisObservacoes
    dataEvento
  }
}
```

**Buscar por período (enfermeiro):**
```graphql
query {
  buscarPorPeriodo(
    inicio: "2026-06-01T00:00:00"
    fim: "2026-06-30T23:59:59"
    page: 0
    size: 10
  ) {
    consultaId
    pacienteNome
    medicoNome
    status
    dataHora
  }
}
```

**Paciente consultando as próprias consultas:**
```graphql
query {
  minhasConsultas(page: 0, size: 10) {
    consultaId
    medicoNome
    especialidade
    dataHora
    status
  }
}
```

**Auditoria completa de uma consulta (admin):**
```graphql
query {
  auditoriaBuscarPorConsultaId(consultaId: "5", page: 0, size: 20) {
    id
    status
    tipoEvento
    dataEvento
    dataDoRegistro
    diagnostico
    tratamentoProposto
  }
}
```

---

## 8. Modelo de Dados

### 8.1. Banco de Dados: `agendeme_db` (ms-gestao-consultas, porta 5432)

#### Tabela: `usuarios`

| Coluna            | Tipo         | Restrições                                  |
|-------------------|--------------|---------------------------------------------|
| `id`              | BIGINT       | PK, AUTO_INCREMENT                          |
| `nome`            | VARCHAR(100) | NOT NULL                                    |
| `email`           | VARCHAR(100) | NOT NULL, UNIQUE                            |
| `ddd`             | VARCHAR(3)   | NOT NULL                                    |
| `telefone`        | VARCHAR(20)  | NOT NULL                                    |
| `sexo`            | VARCHAR(20)  | NOT NULL (MASCULINO / FEMININO / OUTRO)     |
| `data_nascimento` | DATE         | NOT NULL                                    |
| `logradouro`      | VARCHAR(150) | NOT NULL                                    |
| `numero`          | VARCHAR(10)  | NOT NULL                                    |
| `complemento`     | VARCHAR(50)  | nullable                                    |
| `bairro`          | VARCHAR(100) | NOT NULL                                    |
| `cidade`          | VARCHAR(100) | NOT NULL                                    |
| `uf`              | VARCHAR(2)   | NOT NULL                                    |
| `cep`             | VARCHAR(8)   | NOT NULL                                    |
| `login`           | VARCHAR(255) | NOT NULL, UNIQUE                            |
| `senha`           | VARCHAR(255) | NOT NULL (BCrypt hash)                      |
| `role`            | VARCHAR(20)  | NOT NULL (ADMIN/MEDICO/ENFERMEIRO/PACIENTE) |
| `ativo`           | BOOLEAN      | NOT NULL                                    |
| `tipo_usuario`    | VARCHAR(31)  | DISCRIMINATOR (MEDICO/PACIENTE/ENFERMEIRO)  |

#### Tabela: `medicos` (JOINED de `usuarios`)

| Coluna         | Tipo        | Restrições                       |
|----------------|-------------|----------------------------------|
| `id`           | BIGINT      | PK, FK → `usuarios.id`          |
| `crm`          | VARCHAR(15) | NOT NULL                         |
| `crm_uf`       | VARCHAR(2)  | NOT NULL                         |
| `especialidade`| VARCHAR(40) | NOT NULL (ver enum Especialidade)|
| —              | —           | UNIQUE(crm, crm_uf)              |

#### Tabela: `pacientes` (JOINED de `usuarios`)

| Coluna | Tipo        | Restrições              |
|--------|-------------|-------------------------|
| `id`   | BIGINT      | PK, FK → `usuarios.id` |
| `cpf`  | VARCHAR(14) | NOT NULL, UNIQUE        |

#### Tabela: `enfermeiros` (JOINED de `usuarios`)

| Coluna   | Tipo        | Restrições              |
|----------|-------------|-------------------------|
| `id`     | BIGINT      | PK, FK → `usuarios.id` |
| `cre`    | VARCHAR(15) | NOT NULL                |
| `cre_uf` | VARCHAR(2)  | NOT NULL                |
| —        | —           | UNIQUE(cre, cre_uf)     |

#### Tabela: `consultas_medicas`

| Coluna                | Tipo        | Restrições                              |
|-----------------------|-------------|-----------------------------------------|
| `id`                  | BIGINT      | PK, AUTO_INCREMENT                      |
| `paciente_id`         | BIGINT      | NOT NULL, FK → `pacientes.id`           |
| `medico_id`           | BIGINT      | NOT NULL, FK → `medicos.id`             |
| `especialidade`       | VARCHAR(40) | NOT NULL (herdada do médico)            |
| `data_hora`           | TIMESTAMP   | NOT NULL                                |
| `status`              | VARCHAR(20) | NOT NULL (AGENDADA/REALIZADA/CANCELADA) |
| `diagnostico`         | TEXT        | nullable                                |
| `tratamento_proposto` | TEXT        | nullable                                |
| `demais_observacoes`  | TEXT        | nullable                                |
| `data_criacao`        | TIMESTAMP   | NOT NULL, não atualizável               |
| `data_modificacao`    | TIMESTAMP   | NOT NULL, atualizado automaticamente    |

---

### 8.2. Banco de Dados: `agendeme_notificacoes_db` (ms-notificacao, porta 5433)

#### Tabela: `notificacoes_log`

| Coluna           | Tipo         | Restrições                                                             |
|------------------|--------------|------------------------------------------------------------------------|
| `id`             | BIGINT       | PK, AUTO_INCREMENT                                                     |
| `consulta_id`    | BIGINT       | NOT NULL                                                               |
| `tipo_evento`    | VARCHAR(255) | NOT NULL (CRIACAO, ALTERACAO_AGENDAMENTO, ATENDIMENTO_REALIZADO, CANCELAMENTO) |
| `destinatario`   | VARCHAR(255) | NOT NULL (e-mail do paciente)                                          |
| `paciente_nome`  | VARCHAR(255) | NOT NULL                                                               |
| `assunto`        | VARCHAR(255) | NOT NULL                                                               |
| `status_consulta`| VARCHAR(30)  | nullable (status da consulta no momento do evento)                     |
| `status`         | VARCHAR(10)  | NOT NULL (ENVIADO / FALHA)                                             |
| `mensagem_erro`  | TEXT         | nullable (preenchido em caso de falha no envio)                        |
| `data_envio`     | TIMESTAMP    | NOT NULL, gerado automaticamente                                       |

---

### 8.3. Banco de Dados: `agendeme_historico_db` (ms-hist-consultas, porta 5434)

#### Tabela: `historico_consultas`

| Coluna                | Tipo         | Restrições                                                                        |
|-----------------------|--------------|-----------------------------------------------------------------------------------|
| `id`                  | BIGINT       | PK, AUTO_INCREMENT                                                                |
| `consulta_id`         | BIGINT       | NOT NULL                                                                          |
| `paciente_nome`       | VARCHAR(100) | NOT NULL                                                                          |
| `paciente_cpf`        | VARCHAR(14)  | NOT NULL                                                                          |
| `paciente_email`      | VARCHAR(100) | NOT NULL                                                                          |
| `medico_nome`         | VARCHAR(100) | NOT NULL                                                                          |
| `medico_crm`          | VARCHAR(15)  | NOT NULL                                                                          |
| `especialidade`       | VARCHAR(40)  | NOT NULL                                                                          |
| `data_hora`           | TIMESTAMP    | NOT NULL                                                                          |
| `status`              | VARCHAR(20)  | NOT NULL (AGENDADA / REALIZADA / CANCELADA)                                       |
| `diagnostico`         | TEXT        | nullable                                                                          |
| `tratamento_proposto` | TEXT        | nullable                                                                          |
| `demais_observacoes`  | TEXT        | nullable                                                                          |
| `tipo_evento`         | VARCHAR(30)  | NOT NULL (CRIACAO, ALTERACAO_AGENDAMENTO, ATENDIMENTO_REALIZADO, ALTERACAO_ATENDIMENTO, CANCELAMENTO) |
| `data_evento`         | TIMESTAMP    | NOT NULL                                                                          |
| `data_do_registro`    | TIMESTAMP    | NOT NULL, gerado automaticamente                                                  |
| —                     | —            | UNIQUE(consulta_id, data_evento)                                                  |

> **Idempotência:** a constraint `UNIQUE(consulta_id, data_evento)` previne duplicação de eventos. O `HistoricoConsultaService` verifica a existência antes de inserir e descarta silenciosamente eventos duplicados.

---

## 9. Configuração e Execução

### 9.1. Pré-requisitos

| Ferramenta     | Versão mínima | Observação                         |
|----------------|---------------|------------------------------------|
| Java           | 21            | JDK 21                             |
| Maven          | 3.9+          | Incluso via `mvnw` em cada serviço |
| Docker         | 24+           | Para PostgreSQL e Kafka            |
| Docker Compose | 2.x           | Incluído no Docker Desktop         |

### 9.2. Variáveis de Configuração

#### ms-gestao-consultas (`application.yml`)

| Propriedade                      | Valor padrão                                     |
|----------------------------------|--------------------------------------------------|
| `server.port`                    | `8081`                                           |
| `spring.datasource.url`          | `jdbc:postgresql://localhost:5432/agendeme_db`   |
| `spring.kafka.bootstrap-servers` | `localhost:9092`                                 |
| `api.security.token.secret`      | `agende-me-secret-key-2026`                      |

#### ms-notificacao (`application.yaml`)

| Propriedade                      | Valor padrão                                              |
|----------------------------------|-----------------------------------------------------------|
| `server.port`                    | `8082`                                                    |
| `spring.datasource.url`          | `jdbc:postgresql://localhost:5433/agendeme_notificacoes_db` |
| `spring.kafka.consumer.group-id` | `ms-notificacoes`                                         |
| `spring.mail.host`               | `smtp.gmail.com`                                          |
| `spring.mail.username`           | `app.agende.me@gmail.com`                                 |

#### ms-hist-consultas (`application.yml`)

| Propriedade                      | Valor padrão                                           |
|----------------------------------|--------------------------------------------------------|
| `server.port`                    | `8083`                                                 |
| `spring.datasource.url`          | `jdbc:postgresql://localhost:5434/agendeme_historico_db` |
| `spring.kafka.consumer.group-id` | `ms-historico`                                         |
| `spring.graphql.path`            | `/graphql`                                             |
| `spring.graphql.graphiql.path`   | `/graphiql`                                            |
| `api.security.token.secret`      | `agende-me-secret-key-2026`                            |

### 9.3. Ordem de Inicialização

```
Passo 1 — Infraestrutura (ms-gestao-consultas)
  cd ms-gestao-consultas && docker compose up -d
  # Sobe: PostgreSQL (5432), Zookeeper, Kafka (9092)

Passo 2 — Banco de ms-notificacao
  cd ms-notificacao && docker compose up -d
  # Sobe: PostgreSQL (5433)

Passo 3 — Banco de ms-hist-consultas
  cd ms-hist-consultas && docker compose up -d
  # Sobe: PostgreSQL (5434)

Passo 4 — Iniciar ms-gestao-consultas
  cd ms-gestao-consultas && ./mvnw spring-boot:run
  # AdminSeeder cria o usuário admin na primeira execução

Passo 5 — Iniciar ms-notificacao
  cd ms-notificacao && ./mvnw spring-boot:run

Passo 6 — Iniciar ms-hist-consultas
  cd ms-hist-consultas && ./mvnw spring-boot:run
```

### 9.4. URLs de Acesso

| Serviço          | URL                                          |
|------------------|----------------------------------------------|
| API REST         | `http://localhost:8081`                      |
| Swagger UI       | `http://localhost:8081/swagger-ui/index.html`|
| GraphQL Endpoint | `http://localhost:8083/graphql`              |
| GraphiQL (UI)    | `http://localhost:8083/graphiql`             |

---

## 10. Guia de Testes

### 10.1. Collection Postman

A collection com todos os endpoints da API já configurados está disponível no repositório:

📥 [agende-me_postman_collection.json](https://github.com/evandrosxavier/agende-me/blob/main/agende-me_postman_collection.json)

**Como importar:**
1. Abra o Postman e clique em **Import**
2. Cole o link acima ou faça download do arquivo e selecione-o
3. Gere o token via `POST /login` e adicione-o no header `Authorization: Bearer <token>`

> Para testar o GraphQL no Postman: selecione o método **POST**, informe `http://localhost:8083/graphql`, escolha o tipo de body **GraphQL** e monte a query desejada. O token JWT deve ser enviado no header `Authorization`.

---

### 10.2. Fluxo Recomendado de Ponta a Ponta

#### Etapa 1 — Autenticação com Admin

```http
POST /login
{ "login": "admin", "senha": "Admin@123" }
```
Adicione ao header: `Authorization: Bearer <token>`

#### Etapa 2 — Cadastrar Médico

```http
POST /medicos
{
  "nome": "Dr. Carlos Ferreira",
  "email": "carlos@clinica.com",
  "ddd": "11",
  "telefone": "999999999",
  "sexo": "MASCULINO",
  "dataNascimento": "1980-05-15",
  "endereco": {
    "logradouro": "Rua das Flores", "numero": "100",
    "bairro": "Centro", "cidade": "São Paulo", "uf": "SP", "cep": "01310100"
  },
  "login": "dr.carlos",
  "senha": "Senha@123",
  "crm": "123456",
  "crmUf": "SP",
  "especialidade": "CARDIOLOGIA"
}
```

#### Etapa 3 — Cadastrar Paciente

> **Dica:** use um **e-mail válido e acessível** no campo `email` para acompanhar as notificações chegando na caixa de entrada a cada evento da consulta.

```http
POST /pacientes
{
  "nome": "Maria Clara Santos",
  "email": "maria@email.com",
  "ddd": "11",
  "telefone": "988888888",
  "sexo": "FEMININO",
  "dataNascimento": "1995-03-20",
  "endereco": {
    "logradouro": "Av. Paulista", "numero": "200",
    "bairro": "Bela Vista", "cidade": "São Paulo", "uf": "SP", "cep": "01310100"
  },
  "login": "maria.santos",
  "senha": "Senha@123",
  "cpf": "12345678900"
}
```

#### Etapa 4 — Agendar Consulta

```http
POST /consultas
{
  "pacienteId": <id-paciente>,
  "medicoId": <id-medico>,
  "dataHora": "2026-07-01T10:00:00"
}
```

Verifique:
- Resposta com `status: AGENDADA` e `especialidade: CARDIOLOGIA` (herdada do médico)
- E-mail enviado para `maria@email.com`
- Registro criado no histórico com `tipoEvento: CRIACAO`

#### Etapa 5 — Consultar Histórico via GraphQL

Acesse `http://localhost:8083/graphiql`, adicione o token nos headers e execute:

```graphql
query {
  buscarPorPacienteCpf(cpf: "12345678900") {
    consultaId
    status
    dataHora
    especialidade
    medicoNome
    diagnostico
    tratamentoProposto
    dataEvento
  }
}
```

#### Etapa 6 — Registrar Atendimento

```http
PATCH /consultas/<id>/atendimento
{
  "diagnostico": "Hipertensão leve",
  "tratamentoProposto": "Losartana 50mg 1x ao dia",
  "demaisObservacoes": "Retorno em 30 dias"
}
```

Verifique:
- Status alterado para `REALIZADA`
- Novo registro no histórico com `tipoEvento: ATENDIMENTO_REALIZADO`
- E-mail enviado ao paciente

#### Etapa 7 — Testar Acesso do Paciente

**REST:**
```http
GET /consultas/minhas-consultas
Authorization: Bearer <token-paciente>
```
Confirme que apenas as consultas do próprio paciente são retornadas.

**GraphQL** (com token de PACIENTE):
```graphql
query {
  minhasConsultas(page: 0, size: 10) {
    consultaId
    medicoNome
    especialidade
    dataHora
    status
  }
}
```
> Campos clínicos (`diagnostico`, `tratamentoProposto`, `demaisObservacoes`) **não estão disponíveis** nesta query — se adicionados, o GraphQL retornará erro de schema.

### 10.3. Cenários de Validação de Regras de Negócio

| Cenário                                          | Resultado esperado                                       |
|--------------------------------------------------|----------------------------------------------------------|
| Cancelar consulta já cancelada                   | HTTP 422 `CONSULTA_JA_CANCELADA`                         |
| Cancelar consulta já realizada                   | HTTP 422 `CONSULTA_NAO_PODE_SER_CANCELADA`               |
| Agendar consulta com médico inativo              | HTTP 422 `MEDICO_INATIVO_CONSULTA`                       |
| Agendar consulta com paciente inativo            | HTTP 422 `PACIENTE_INATIVO_CONSULTA`                     |
| Cadastrar médico com CRM ativo existente         | HTTP 409 `CRM_JA_CADASTRADO`                             |
| Cadastrar médico com CRM inativo existente       | HTTP 422 `MEDICO_CADASTRO_INATIVO`                       |
| Reativar médico já ativo                         | HTTP 422 `MEDICO_JA_ATIVO`                               |
| Login com credenciais inválidas                  | HTTP 401 "Login ou senha inválidos"                      |
| Login com usuário inativo                        | HTTP 403 "Usuário inativo"                               |
| Requisição sem token JWT                         | HTTP 401 "Token ausente, inválido ou expirado"           |
| Paciente acessando `/medicos`                    | HTTP 403 "Você não tem permissão"                        |
| Busca por período com início > fim               | HTTP 400 `PERIODO_INVALIDO`                              |
| Evento Kafka duplicado no ms-hist-consultas      | Ignorado silenciosamente (idempotência por constraint)   |

---

## 11. Stack Tecnológica

| Categoria             | Tecnologia / Biblioteca              | Versão      |
|-----------------------|--------------------------------------|-------------|
| Linguagem             | Java                                 | 21          |
| Framework principal   | Spring Boot                          | 3.5.x       |
| Segurança             | Spring Security + auth0 JWT          | —           |
| Persistência          | Spring Data JPA + Hibernate          | —           |
| Banco de dados        | PostgreSQL                           | 16          |
| Mensageria            | Apache Kafka (Confluent)             | 7.5.0       |
| Coordenação Kafka     | Apache Zookeeper                     | 7.5.0       |
| API REST              | Spring Web (Servlet)                 | —           |
| API GraphQL           | Spring GraphQL                       | —           |
| Documentação API      | SpringDoc OpenAPI (Swagger UI)       | 2.8.x       |
| Mapeamento de objetos | MapStruct                            | 1.6.3       |
| Boilerplate           | Lombok                               | —           |
| E-mail                | Spring Mail + JavaMail (Gmail SMTP)  | —           |
| Validação             | Jakarta Validation (Bean Validation) | —           |
| Containerização       | Docker + Docker Compose              | 24+ / 2.x   |
| Build                 | Apache Maven (Wrapper incluído)      | 3.9+        |

---

## 12. Estrutura do Repositório

```
agende-me/
│
├── ms-gestao-consultas/               # Microsserviço principal (REST API)
│   ├── docker-compose.yml             # PostgreSQL + Kafka + Zookeeper
│   ├── pom.xml
│   └── src/main/java/br/com/agendeme/gestao/
│       ├── config/
│       │   ├── AdminSeeder.java       # Cria usuário admin padrão na 1ª execução
│       │   ├── KafkaProducerConfig.java
│       │   ├── OpenApiConfig.java
│       │   ├── SecurityConfig.java    # JWT stateless, BCrypt, EntryPoint, AccessDenied
│       │   └── SecurityFilter.java    # Filtro de validação JWT (OncePerRequestFilter)
│       ├── controller/
│       │   ├── AuthController.java    # Login com tratamento de BadCredentials/Disabled
│       │   ├── ConsultaMedicaController.java
│       │   ├── EnfermeiroController.java
│       │   ├── MedicoController.java
│       │   ├── PacienteController.java
│       │   └── handler/
│       │       └── ControllerExceptionHandler.java  # ProblemDetail (RFC 7807)
│       ├── dto/                       # Records de entrada e saída da API
│       ├── excecoes/
│       │   ├── BusinessException.java
│       │   └── ErrorCode.java         # Catálogo de erros de negócio
│       ├── mapper/                    # MapStruct
│       ├── model/
│       │   ├── domain/
│       │   │   ├── Usuario.java       # Entidade base (JOINED inheritance)
│       │   │   ├── Medico.java
│       │   │   ├── Paciente.java
│       │   │   ├── Enfermeiro.java
│       │   │   ├── ConsultaMedica.java
│       │   │   └── Endereco.java      # @Embeddable
│       │   └── enums/
│       │       ├── Role.java
│       │       ├── StatusConsulta.java
│       │       ├── Especialidade.java
│       │       └── Sexo.java
│       ├── repository/
│       └── service/
│           ├── AuthenticationService.java  # UserDetailsService (UsernameNotFoundException)
│           ├── ConsultaMedicaService.java
│           ├── EnfermeiroService.java
│           ├── KafkaProducerService.java
│           ├── MedicoService.java
│           ├── PacienteService.java
│           └── TokenService.java           # JWT: gera token, valida (retorna null em falha)
│
├── ms-notificacao/                    # Microsserviço de e-mail
│   ├── docker-compose.yaml            # PostgreSQL (porta 5433)
│   ├── pom.xml
│   ├── asyncapi.yaml                  # Documentação dos contratos Kafka
│   └── src/main/java/br/com/agendeme/notificacoes/
│       ├── config/
│       │   └── KafkaConsumerConfig.java
│       ├── dto/
│       │   └── ConsultaNotificacaoDTO.java
│       ├── listener/
│       │   └── ConsultaNotificacaoListener.java  # Consome 4 tópicos Kafka
│       ├── model/
│       │   ├── NotificacaoLog.java
│       │   └── StatusEnvio.java        # ENVIADO | FALHA
│       ├── repository/
│       │   └── NotificacaoLogRepository.java
│       └── service/
│           ├── NotificacaoEmailService.java  # Gmail SMTP
│           └── NotificacaoLogService.java    # Persiste log de cada envio
│
├── ms-hist-consultas/                 # Microsserviço de histórico (GraphQL)
│   ├── docker-compose.yml             # PostgreSQL (porta 5434)
│   ├── pom.xml
│   └── src/main/java/br/com/agendeme/historico/
│       ├── config/
│       │   ├── GraphQlExceptionHandler.java
│       │   ├── KafkaConsumerConfig.java
│       │   ├── SecurityConfig.java    # /graphql liberado para introspecção
│       │   └── SecurityFilter.java    # Valida JWT externo do ms-gestao-consultas
│       ├── controller/
│       │   └── HistoricoConsultaController.java  # @QueryMapping + @PreAuthorize (11 queries)
│       ├── dto/
│       │   ├── ConsultaEventDTO.java
│       │   ├── HistoricoConsultaDTO.java       # com dados clínicos (MEDICO/ENFERMEIRO/ADMIN)
│       │   └── HistoricoConsultaResumo.java    # sem dados clínicos (PACIENTE)
│       ├── excecoes/
│       ├── listener/
│       │   └── HistoricoConsultaListener.java  # Consome 5 tópicos Kafka
│       ├── model/
│       │   └── HistoricoConsulta.java
│       ├── repository/
│       │   └── HistoricoConsultaRepository.java
│       └── service/
│           ├── HistoricoConsultaService.java   # Idempotência na persistência
│           └── TokenService.java
│   └── src/main/resources/
│       └── graphql/
│           └── schema.graphqls        # Schema GraphQL tipado
│
├── README.md                          # Guia de execução e referência rápida
└── TDD.md                             # Este documento técnico
```

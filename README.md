# Agende-me

Sistema backend para agendamento e gerenciamento de consultas médicas em ambiente hospitalar, composto por múltiplos serviços independentes com comunicação assíncrona.

## Arquitetura

O sistema é dividido em três microserviços independentes, cada um com seu próprio banco de dados e responsabilidades bem definidas.

### Serviços

**ms-gestao-consultas** — responsável pelo cadastro, atualização, cancelamento e consulta de agendamentos médicos. É o serviço central do sistema e publica eventos no Kafka sempre que uma consulta é criada ou alterada.

**ms-notificacao** — consome os eventos publicados pelo ms-gestao-consultas e envia lembretes automáticos aos pacientes sobre suas consultas.

**ms-hist-consultas** — consome os mesmos eventos do Kafka e mantém seu próprio banco de dados com o histórico de consultas, expondo-o via GraphQL para consultas flexíveis por diferentes clientes.

## Decisões Técnicas

| Tecnologia | Decisão | Justificativa |
|---|---|---|
| Banco de dados | PostgreSQL por serviço | Dados estruturados com relacionamentos claros. Isolamento garante disponibilidade independente entre serviços |
| Mensageria | Kafka | Permite múltiplos consumidores independentes no mesmo evento. Escalável para novos serviços no futuro |
| API de histórico | GraphQL | Flexibilidade para diferentes clientes consumirem exatamente os campos que precisam |
| Segurança | Spring Security | Autenticação e autorização por perfil de acesso em cada serviço |

## Níveis de Acesso

| Perfil | Permissões |
|---|---|
| Médico | Visualizar e editar histórico de consultas |
| Enfermeiro | Registrar novas consultas e acessar histórico |
| Paciente | Visualizar apenas as próprias consultas |

## Tecnologias

- Java 21
- Spring Boot 3.5.x
- Spring Security
- Spring Data JPA
- Spring for Apache Kafka
- PostgreSQL
- GraphQL
- Docker Compose

## Estrutura do Repositório
agende-me/
├── README.md
├── docker-compose.yml
├── ms-gestao-consultas/
├── ms-notificacao/
└── ms-hist-consultas/
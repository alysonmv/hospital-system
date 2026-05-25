# Hospital Management System

Backend modular para ambientes hospitalares, desenvolvido como projeto de portfólio para a Pós-Tech FIAP — Arquitetura e Desenvolvimento Java (Fase 3).

O sistema cobre agendamento de consultas, histórico médico e notificações automáticas, distribuídos em três microsserviços independentes que se comunicam via RabbitMQ.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange?logo=rabbitmq)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## Sumário

- [Visão geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Como executar](#como-executar)
- [Credenciais de teste](#credenciais-de-teste)
- [Endpoints](#endpoints)
- [GraphQL](#graphql)
- [Mensageria](#mensageria)
- [Testes](#testes)
- [Tecnologias](#tecnologias)

---

## Visão geral

| Serviço | Porta | Responsabilidade |
|---|---|---|
| `appointment-service` | 8080 | Autenticação JWT, CRUD de consultas, publicação de eventos |
| `history-service` | 8081 | Histórico médico via GraphQL |
| `notification-service` | 8082 | Consumo de eventos e geração de lembretes |

**Controle de acesso por perfil:**

| Ação | MEDICO | ENFERMEIRO | PACIENTE |
|---|:---:|:---:|:---:|
| Criar / editar consulta | ✓ | ✓ | — |
| Visualizar todas as consultas | ✓ | ✓ | — |
| Visualizar próprias consultas | ✓ | ✓ | ✓ |
| Acessar histórico médico | ✓ | ✓ | ✓ |

---

## Arquitetura

```
                        ┌──────────────────────┐
                        │    appointment-service│
          REST + JWT ──▶│       :8080           │──▶ PostgreSQL :5432
                        │                      │
                        └──────────┬───────────┘
                                   │ publica evento
                                   ▼
                        ┌──────────────────────┐
          GraphQL + JWT │  RabbitMQ :5672       │
                  ──▶   │  hospital.exchange    │
                        └──────────┬───────────┘
                                   │ consome evento
          ┌────────────────────────┼────────────────────────┐
          ▼                        ▼                        ▼
┌─────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│ history-service │     │notification-serv.│     │  Dead Letter     │
│    :8081        │     │    :8082         │     │  Queue (falhas)  │
│ PostgreSQL:5433 │     │ PostgreSQL :5434 │     └──────────────────┘
└─────────────────┘     └──────────────────┘
```

O `appointment-service` é o único que emite tokens JWT. O `history-service` valida o mesmo token sem precisar consultar o serviço de origem — a chave secreta é compartilhada via variável de ambiente.

---

## Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e em execução

Nenhuma outra instalação é necessária. PostgreSQL, RabbitMQ e os três serviços sobem automaticamente via Docker Compose.

---

## Como executar

```bash
git clone https://github.com/seu-usuario/hospital-system.git
cd hospital-system
docker compose up --build
```

O primeiro build leva aproximadamente 5 a 10 minutos enquanto o Maven resolve as dependências. Nas execuções seguintes, o cache do Docker reduz esse tempo para menos de 1 minuto.

O ambiente está pronto quando os três serviços exibirem `Started *Application` nos logs. Para confirmar:

```bash
curl http://localhost:8080/actuator/health  # {"status":"UP"}
curl http://localhost:8081/actuator/health  # {"status":"UP"}
curl http://localhost:8082/actuator/health  # {"status":"UP"}
```

Para encerrar:

```bash
docker compose down          # mantém os volumes (dados persistidos)
docker compose down -v       # remove os volumes (banco zerado)
```

---

## Credenciais de teste

Os usuários e o paciente de teste são criados automaticamente pelo Flyway na primeira inicialização. Não é necessário nenhum setup manual de banco de dados.

| Nome | E-mail | Senha | Perfil |
|---|---|---|---|
| Dr. Carlos Silva | medico@hospital.com | Hospital@123 | ROLE_MEDICO |
| Enf. Ana Souza | enfermeiro@hospital.com | Hospital@123 | ROLE_ENFERMEIRO |
| João Paciente | paciente@hospital.com | Hospital@123 | ROLE_PACIENTE |

IDs fixos para uso nas requisições:

```
medicoId:    a1b2c3d4-0000-0000-0000-000000000001
pacienteId:  b1b2c3d4-0000-0000-0000-000000000001
```

---

## Endpoints

A documentação interativa completa está disponível em:
**http://localhost:8080/swagger-ui.html** (após subir o ambiente)

### Autenticação

```
POST /api/v1/auth/login
```

```json
// Request
{ "email": "medico@hospital.com", "senha": "Hospital@123" }

// Response 200
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "role": "ROLE_MEDICO"
}
```

### Consultas

Todos os endpoints abaixo exigem o header `Authorization: Bearer {token}`.

```
POST   /api/v1/appointments          Criar consulta          MEDICO, ENFERMEIRO
GET    /api/v1/appointments          Listar consultas        Todos *
GET    /api/v1/appointments/{id}     Buscar por ID           Todos *
PUT    /api/v1/appointments/{id}     Atualizar consulta      MEDICO, ENFERMEIRO
DELETE /api/v1/appointments/{id}     Cancelar consulta       MEDICO, ENFERMEIRO
```

> `*` Pacientes visualizam apenas suas próprias consultas. Médicos e enfermeiros visualizam todas.

**Exemplo — criar consulta:**

```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "dataConsulta": "2026-09-15T10:00:00",
    "medicoId":    "a1b2c3d4-0000-0000-0000-000000000001",
    "pacienteId":  "b1b2c3d4-0000-0000-0000-000000000001",
    "descricao":   "Consulta de rotina"
  }'
```

Ao criar ou editar uma consulta, o `notification-service` recebe automaticamente o evento via RabbitMQ e registra o lembrete nos logs:

```
NOTIFICAÇÃO ENVIADA
Para:     João Paciente | (48) 99999-1234
Mensagem: Olá, João Paciente! Sua consulta foi AGENDADA com Dr(a). Dr. Carlos Silva para 15/09/2026 às 10:00.
```

---

## GraphQL

**Endpoint:** `POST http://localhost:8081/graphql`  
**Playground interativo:** http://localhost:8081/graphiql

O header `Authorization: Bearer {token}` é obrigatório. O token é o mesmo gerado pelo `appointment-service`.

### Queries disponíveis

```graphql
# Histórico médico de um paciente
query {
  buscarHistoricoPaciente(pacienteId: "b1b2c3d4-0000-0000-0000-000000000001") {
    id
    descricao
    dataRegistro
    paciente { nome telefone }
  }
}

# Consultas futuras
query {
  listarConsultasFuturas(pacienteId: "b1b2c3d4-0000-0000-0000-000000000001") {
    id
    dataConsulta
    status
    medico    { nome }
    paciente  { nome }
  }
}

# Consultas passadas
query {
  listarConsultasPassadas {
    id
    dataConsulta
    status
    medico   { nome }
    paciente { nome }
  }
}

# Buscar consulta por ID
query {
  buscarConsultaPorId(id: "c1b2c3d4-0000-0000-0000-000000000001") {
    id
    dataConsulta
    status
    descricao
    medico   { nome email }
    paciente { nome telefone }
  }
}
```

---

## Mensageria

O `appointment-service` publica eventos na exchange `hospital.exchange` (TopicExchange) com as seguintes routing keys:

| Evento | Routing key |
|---|---|
| Consulta criada | `appointment.created` |
| Consulta atualizada | `appointment.updated` |
| Consulta cancelada | `appointment.cancelled` |

O `notification-service` consome da fila `appointment.events.queue`. Em caso de falha no processamento, a mensagem é reenviada automaticamente até 3 vezes (intervalos de 3s, 6s e 12s). Após a terceira falha, a mensagem é encaminhada para a Dead Letter Queue `appointment.events.dlq`.

O painel de gerenciamento do RabbitMQ pode ser acessado em **http://localhost:15672** (usuário: `guest`, senha: `guest`).

---

## Testes

```bash
# A partir da raiz de cada serviço
cd appointment-service  && mvn test
cd history-service      && mvn test
cd notification-service && mvn test
```

Cobertura implementada:

- Testes unitários de serviço com Mockito (`AppointmentService`, `NotificationService`)
- Testes de controller com MockMvc (`AuthController`)
- Testes de resolver GraphQL com `GraphQlTester` (`HistoryGraphQLResolver`)

---

## Collection Postman

Importe o arquivo `Hospital-System.postman_collection.json` no Postman. O request de login salva o token automaticamente na variável `{{token}}`, que é reutilizada por todos os demais requests.

---

## Tecnologias

- **Java 21** com **Spring Boot 3.2.4**
- **Spring Security 6** — autenticação JWT (JJWT 0.12.5), BCrypt strength 12
- **Spring GraphQL** — schema-first com `schema.graphqls`
- **Spring AMQP** — RabbitMQ com retry e Dead Letter Queue
- **Spring Data JPA** + **PostgreSQL 16**
- **Flyway** — migrations e seed de dados automáticos
- **MapStruct** — mapeamento entre entidades e DTOs
- **SpringDoc OpenAPI** — Swagger UI
- **Docker + Docker Compose** — ambiente completo em um único comando

---

## Licença

MIT

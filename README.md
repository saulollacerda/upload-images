# 📸 UpImages — API de Upload de Imagens para Amazon S3

API REST desenvolvida com **Spring Boot 4** para upload, consulta e exclusão de imagens armazenadas em um bucket **Amazon S3**. O sistema conta com autenticação JWT, validação customizada de arquivos e banco de dados H2 para persistência dos metadados das imagens.

---

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Arquitetura](#-arquitetura)
- [Tecnologias](#-tecnologias)
- [Pré-requisitos](#-pré-requisitos)
- [Configuração](#️-configuração)
- [Como Executar](#-como-executar)
- [Endpoints da API](#-endpoints-da-api)
- [Autenticação](#-autenticação)
- [Validação de Arquivos](#-validação-de-arquivos)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Modelo de Dados](#-modelo-de-dados)
- [Tratamento de Erros](#-tratamento-de-erros)
- [Infraestrutura como Código (Terraform)](#️-infraestrutura-como-código-terraform)

---

## 🔍 Visão Geral

O **UpImages** é um serviço backend que permite a usuários autenticados:

- **Fazer upload** de imagens (JPEG, PNG, GIF, WEBP) para um bucket S3 da AWS
- **Gerar URLs pré-assinadas** (presigned URLs) para acesso temporário às imagens (válidas por 10 minutos)
- **Excluir imagens** do bucket S3 e do banco de dados
- **Registrar-se e autenticar-se** via JWT (JSON Web Token)

Cada imagem enviada é associada ao usuário autenticado, e seus metadados (nome original, tamanho, chave S3, data de upload) são persistidos no banco de dados.

---

## 🏗 Arquitetura

O projeto segue uma arquitetura em camadas (Layered Architecture):

```
Controller → Service → Repository → Database
                ↓
           AWS S3 Client
```

| Camada | Responsabilidade |
|---|---|
| **Controller** | Recebe as requisições HTTP e delega para os serviços |
| **Service** | Contém a lógica de negócio (upload S3, JWT, gerenciamento de usuários) |
| **Repository** | Abstração de acesso ao banco de dados via Spring Data JPA |
| **Config** | Configurações de segurança (Spring Security + JWT), cliente S3 |
| **DTO** | Objetos de transferência de dados entre camadas |
| **Entity** | Entidades JPA mapeadas para tabelas do banco |
| **Validator** | Validadores customizados (ex: validação de arquivo) |
| **Exception Handler** | Tratamento centralizado de exceções via `@ControllerAdvice` |

---

## 🛠 Tecnologias

| Tecnologia | Versão | Descrição |
|---|---|---|
| **Java** | 17 | Linguagem principal |
| **Spring Boot** | 4.0.3 | Framework base da aplicação |
| **Spring Security** | - | Autenticação e autorização |
| **Spring Data JPA** | - | Persistência de dados |
| **AWS SDK for Java** | 2.41.5 | Integração com Amazon S3 |
| **JJWT** | 0.12.6 | Geração e validação de tokens JWT |
| **H2 Database** | - | Banco de dados em memória (perfil de teste) |
| **Terraform** | >= 1.0 | Infraestrutura como código (IaC) para provisionamento AWS |
| **Bean Validation** | - | Validação de dados de entrada |
| **Maven** | - | Gerenciamento de dependências e build |

---

## 📌 Pré-requisitos

- **Java 17+** instalado
- **Maven 3.8+** (ou use o wrapper `./mvnw` incluso no projeto)
- **Conta AWS** com:
  - Um **bucket S3** criado
  - Um **perfil de credenciais** configurado no `~/.aws/credentials` (ex: `my-user`)

### Exemplo de `~/.aws/credentials`

> ⚠️ **Nunca commite credenciais AWS em repositórios.** Use variáveis de ambiente ou o AWS CLI (`aws configure`) para configurar suas credenciais.

```ini
[default]
aws_access_key_id = SUA_ACCESS_KEY
aws_secret_access_key = SUA_SECRET_KEY
```

---

## ⚙️ Configuração

### Variáveis de Ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `APP_PROFILE` | `test` | Perfil ativo do Spring (`test` usa H2 in-memory) |
| `S3_BUCKET_NAME` | `meu-bucket-s3` | Nome do bucket S3 na AWS |
| `JWT_SECRET` | *(defina uma chave segura com pelo menos 256 bits)* | Chave secreta para assinatura dos tokens JWT |

### Propriedades Principais (`application.properties`)

```properties
spring.application.name=upimages
spring.profiles.active=${APP_PROFILE:test}

aws.s3.bucket-name=${S3_BUCKET_NAME:meu-bucket-s3}
aws.profile=${AWS_PROFILE:default}

jwt.secret=${JWT_SECRET:sua-chave-secreta-aqui}
jwt.expiration=86400000   # 24 horas em milissegundos
```

### Perfil de Teste (`application-test.properties`)

No perfil `test`, a aplicação utiliza o **H2 Database** em memória com console web habilitado:

- **URL do H2 Console:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Usuário:** `sa` | **Senha:** *(vazio)*
- O esquema é criado/destruído automaticamente (`ddl-auto=create-drop`)

---

## 🚀 Como Executar

```bash
# Clone o repositório
git clone <url-do-repositório>
cd upimages/backend/upimages

# Execute com o Maven Wrapper
./mvnw spring-boot:run

# Ou com variáveis de ambiente customizadas
S3_BUCKET_NAME=meu-bucket JWT_SECRET=minha-chave ./mvnw spring-boot:run
```

A aplicação será iniciada em `http://localhost:8080`.

---

## 📡 Endpoints da API

### 🔓 Públicos (sem autenticação)

#### Registrar Usuário

```http
POST /user/register
Content-Type: application/json

{
  "username": "João",
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Resposta:** `200 OK`

#### Login

```http
POST /user/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### 🔐 Protegidos (requerem JWT)

> Inclua o header `Authorization: Bearer <token>` em todas as requisições.

#### Upload de Imagem

```http
POST /images/upload
Content-Type: multipart/form-data
Authorization: Bearer <token>

file: <arquivo de imagem>
description: "Descrição opcional da imagem"
```

**Resposta:**
```json
{
  "key": "a1b2c3d4-foto.jpg",
  "uploadedAt": "2026-03-10T14:30:00"
}
```

#### Obter URL Pré-assinada

```http
GET /images/{key}
Authorization: Bearer <token>
```

**Resposta:** URL pré-assinada do S3 (válida por **10 minutos**)

```
https://s3.amazonaws.com/bucket/a1b2c3d4-foto.jpg?X-Amz-Algorithm=...
```

#### Excluir Imagem

```http
DELETE /images/{id}
Authorization: Bearer <token>
```

**Resposta:** `200 OK` — `"Image deleted"`

---

## 🔐 Autenticação

O sistema utiliza **JWT (JSON Web Token)** para autenticação stateless:

1. O usuário se registra via `POST /user/register`
2. Realiza login via `POST /user/login` e recebe um **token JWT**
3. O token deve ser enviado no header `Authorization: Bearer <token>` em todas as rotas protegidas
4. O token tem validade de **24 horas** (`86400000 ms`)

### Fluxo de Autenticação

```
Cliente → POST /user/login → Servidor valida credenciais → Retorna JWT
Cliente → GET /images/{key} + Bearer JWT → JwtAuthenticationFilter valida token → Acesso permitido
```

### Configuração de Segurança

- **CSRF** desabilitado (API stateless)
- **Sessão** stateless (`SessionCreationPolicy.STATELESS`)
- **Rotas públicas:** `/user/register`, `/user/login`, `/h2-console/**`
- **Demais rotas:** requerem autenticação
- **Senhas** criptografadas com **BCrypt**

---

## ✅ Validação de Arquivos

A aplicação utiliza uma **annotation customizada** `@ValidFile` com um validador dedicado (`ValidFileValidator`) para garantir a integridade dos uploads:

| Regra | Detalhe |
|---|---|
| **Arquivo obrigatório** | Não aceita arquivo vazio ou nulo |
| **Tipos permitidos** | `image/jpeg`, `image/png`, `image/gif`, `image/webp` |
| **Tamanho máximo** | 5 MB |

**Mensagens de validação:**
- `"O arquivo não pode ser vazio"`
- `"Tipo de arquivo não permitido. Aceitos: JPEG, PNG, GIF, WEBP"`
- `"O arquivo não pode ser maior do que 5MB"`

---

## 📂 Estrutura do Projeto

```
backend/upimages/src/main/java/com/upimages/upimages/
├── UpimagesApplication.java           # Classe principal (entry point)
├── annotation/
│   └── ValidFile.java                 # Annotation customizada de validação de arquivo
├── config/
│   ├── JwtAuthenticationFilter.java   # Filtro JWT (intercepta requisições)
│   ├── S3Config.java                  # Configuração do cliente AWS S3 e S3Presigner
│   └── SecurityConfig.java           # Configuração do Spring Security
├── controller/
│   ├── ImageController.java           # Endpoints de imagens (upload, get, delete)
│   ├── UserController.java           # Endpoints de usuário (register, login)
│   └── exceptions/
│       └── ResourceExceptionHandler.java  # Handler global de exceções
├── dto/
│   ├── ImageResponseDTO.java         # DTO de resposta do upload (key + timestamp)
│   ├── ImageUploadDTO.java           # DTO de entrada do upload (file + description)
│   └── StandardErrorDTO.java         # DTO padrão de erro (timestamp, status, message)
├── entity/
│   ├── ImageEntity.java              # Entidade JPA de imagem (tb_image)
│   └── UserEntity.java              # Entidade JPA de usuário (tb_user) + UserDetails
├── infra/
│   ├── terraform.tf               # Configuração do provider AWS e versão do Terraform
│   ├── variables.tf               # Variáveis (região, ambiente)
│   └── main.tf                    # Recursos: S3 bucket, encryption, CORS, public access block
├── repository/
│   ├── ImageRepository.java          # Repository JPA de imagens
│   └── UserRepository.java          # Repository JPA de usuários
├── service/
│   ├── ImageService.java             # Lógica de upload/delete/presign no S3
│   ├── JwtService.java              # Geração e validação de tokens JWT
│   ├── UserService.java             # Registro de usuários + UserDetailsService
│   └── exceptions/
│       └── ResourceNotFoundException.java  # Exceção de recurso não encontrado
└── validator/
    └── ValidFileValidator.java        # Implementação do validador @ValidFile
```

---

## 🗄 Modelo de Dados

### `tb_user`

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | `BIGINT` (PK, auto) | Identificador único |
| `username` | `VARCHAR` | Nome do usuário |
| `email` | `VARCHAR` (unique) | Email (usado como login) |
| `password` | `VARCHAR` | Senha criptografada (BCrypt) |
| `role` | `VARCHAR` | Papel do usuário (padrão: `USER`) |

### `tb_image`

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | `BIGINT` (PK, auto) | Identificador único |
| `s3_key` | `VARCHAR` | Chave do objeto no bucket S3 |
| `original_file_name` | `VARCHAR` | Nome original do arquivo enviado |
| `file_size` | `BIGINT` | Tamanho do arquivo em bytes |
| `upload_date` | `TIMESTAMP` | Data/hora do upload |
| `id_user` | `BIGINT` (FK) | Referência ao usuário proprietário |

### Relacionamento

```
tb_user (1) ←——→ (N) tb_image
```

Um usuário pode ter várias imagens. A exclusão de um usuário remove todas as suas imagens em cascata (`CascadeType.ALL`).

---

## ❌ Tratamento de Erros

A aplicação possui um handler global de exceções (`@ControllerAdvice`) que retorna respostas padronizadas:

### Recurso não encontrado — `404`

```json
{
  "timestamp": "2026-03-10T17:00:00Z",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Resource not found",
  "path": "/images/999"
}
```

### Erro de validação — `400`

```json
{
  "timestamp": "2026-03-10T17:00:00Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Detalhes da validação...",
  "path": "/images/upload"
}
```

---

## 🔧 Detalhes Técnicos

### Integração com AWS S3

- O cliente S3 é configurado para a região **us-east-1**
- Utiliza **ProfileCredentialsProvider** (credenciais do `~/.aws/credentials`)
- O nome do arquivo no S3 é gerado com **UUID + nome original** para evitar colisões
- URLs pré-assinadas têm validade de **10 minutos**

### Segurança

- **JWT** assinado com HMAC-SHA usando chave configurável
- Filtro `JwtAuthenticationFilter` intercepta todas as requisições e valida o token
- Integração com `UserDetailsService` para carregar dados do usuário autenticado
- O email do usuário é utilizado como `username` no Spring Security

---

## 🏗️ Infraestrutura como Código (Terraform)

Toda a infraestrutura AWS do projeto é provisionada via **Terraform**, garantindo reprodutibilidade, versionamento e automação do ambiente.

### Recursos Provisionados

| Recurso | Descrição |
|---|---|
| **S3 Bucket** | Bucket para armazenamento das imagens, com nome gerado dinamicamente (`image-bucket-<random_id>`) |
| **Public Access Block** | Bloqueio total de acesso público ao bucket (ACLs e policies bloqueadas) |
| **Server-Side Encryption** | Criptografia automática dos objetos com **AES-256** |
| **CORS Configuration** | Permite requisições cross-origin (`GET`, `PUT`, `POST`) para integração com frontends |

### Estrutura dos Arquivos Terraform

```
infra/
├── terraform.tf    # Configuração do Terraform e provider AWS (~> 4.0)
├── variables.tf    # Variáveis (região, ambiente)
└── main.tf         # Recursos: S3 bucket, encryption, CORS, public access block
```

### Como Provisionar

```bash
cd backend/upimages/src/main/java/com/upimages/upimages/infra

# Inicializar o Terraform
terraform init

# Visualizar o plano de execução
terraform plan

# Aplicar a infraestrutura
terraform apply
```

### Variáveis Terraform

| Variável | Padrão | Descrição |
|---|---|---|
| `aws_region` | `us-east-1` | Região AWS onde os recursos serão criados |
| `environment` | `dev` | Nome do ambiente (dev, staging, prod) |

### Políticas de Segurança do Bucket

O bucket S3 é provisionado com as seguintes proteções:

- 🔒 **Acesso público totalmente bloqueado** (`block_public_acls`, `block_public_policy`, `ignore_public_acls`, `restrict_public_buckets`)
- 🔐 **Criptografia server-side** com AES-256 aplicada por padrão a todos os objetos
- O acesso às imagens é feito exclusivamente via **Presigned URLs** geradas pela API

---


## 📄 Licença

Este projeto está sob desenvolvimento.

# CampusFace API - ZL devs

## 👥 Integrantes do Grupo (ZL devs)
* **Gabriel Meira**
* **Gabriel Barbosa**
* **Gabriela Fiori**
* **Heitor Morais**
* **Henrique Cesar**

---

## 📝 Descrição do Projeto
O **CampusFace** é uma solução de backend desenvolvida em **Kotlin** com **Spring Boot** para orquestrar o controle de acesso em instituições (Hubs/Campus).

O sistema gerencia identidades, permissões e validações de acesso, atuando como um orquestrador central que sincroniza dados com totens de reconhecimento facial (Python/Edge) e permite validação secundária via QR Code. O projeto utiliza uma arquitetura híbrida com **Google Firestore** (NoSQL) para flexibilidade e velocidade, e **Cloudinary** para armazenamento de imagens faciais.

---

## 🛠️ Tecnologias Utilizadas
* **Linguagem:** Kotlin (JVM 21)
* **Framework:** Spring Boot 3.5.4
* **Banco de Dados:** Google Firebase Firestore (NoSQL)
* **Armazenamento de Imagens:** Cloudinary
* **Documentação:** SpringDoc OpenAPI (Swagger)
* **Segurança:** Spring Security + JWT (HMAC256)
* **Edge Computing:** Python

---

## 🛠️ Edge Computing - Server de reconhecimento local
> **Importante:** Conforme alinhado e autorizado pelo professor, neste projeto **utilizamos ChromaDB para fazer o gerenciamento dos embeddings**.<br />
> Dessa forma optamos por fazer um sistema interno de gerenciamento das imagens dos rostos, com 5 nomenclaturas importantes<br />
> `Change Request`: Uma solicitação de mudança de imagem, que, quando aceita, é propagada nos servidores.<br />
> `Entry Request`: Uma solicitação de entrada, que, quando aceita, é propagada nos servidores.<br />
> `CheckIn`: Uma operação executada a cada X tempo, para sincronizar os eventos acima.<br />
> `Upsert`: Uma operação enviada ao servidor de reconhecimento, que, insere um elemento se não existir, ou altera caso exista.<br />
> `Delete`: Uma operação enviada ao servidor de reconhecimento, que, deleta um elemento.<br />
### Diagrama de sequência
![Diagrama](docs/diagrama.png)
---

## ⚠️ Nota sobre Persistência de Dados
> **Importante:** Conforme alinhado e autorizado pelo professor, este projeto **não utiliza JPA/Hibernate com banco relacional**.
>
> Em substituição, utilizamos o **Google Firestore**, um banco de dados NoSQL orientado a documentos. Portanto, as anotações `@Entity`, `@Table` e interfaces `JpaRepository` foram substituídas pelas implementações do SDK do Firebase Admin e anotações de serialização nativas.

---

## 🚀 Instruções de Instalação e Execução

### Pré-requisitos
* Java JDK 21 instalado.
* Arquivo `firebase.json` (Credenciais de serviço do Google) na pasta `src/main/resources/`.
* Variáveis de ambiente configuradas (ou hardcoded no `application.properties` para dev).

### Passos para Executar

1.  **Clonar o repositório:**
    ```bash
    git clone https://github.com/riqalves/campus-face-springboot-kotlin.git
    cd campus-face-springboot-kotlin
    ```

2.  **Configurar Variáveis:**
    Certifique-se de que o `application.properties` ou variáveis de sistema contenham:
    * `CLOUDINARY_URL`
    * `JWT_SECRET`

3.  **Compilar e Rodar:**
    Via terminal (Linux/Mac):
    ```bash
    ./gradlew bootRun
    ```
    Via terminal (Windows):
    ```bash
    .\gradlew.bat bootRun
    ```

4.  **Acessar a Documentação (Swagger):**
    Após iniciar, acesse: `http://localhost:8080/swagger-ui.html`

---

## ✅ Atendimento aos Requisitos do Projeto

Abaixo detalhamos como cada item da entrega foi implementado no código:

### 1) Projeto Base Spring Boot com Kotlin
O projeto foi criado utilizando Gradle com Kotlin DSL, contendo dependências `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-security` e `springdoc-openapi`.

#### a) Implementação de Entidades
**Como alinhado e autorizado previamente** foram criadas 7 entidades, todas com ID e variados tipos de dados (String, Instant, Enum, Boolean, List):

1.  **User** :
    * Campos: `id` (String), `fullName` (String), `email` (String), `createdAt` (Instant).
2.  **Organization**:
    * Campos: `id` (String), `adminIds` (List), `createdAt` (Instant).
3.  **EntryRequest**:
    * Campos: `status` (Enum RequestStatus), `requestedAt` (Instant).
4.  **AuthCode**:
    * Campos: `valid` (**Boolean**), `expirationTime` (Instant), `code` (String).
5.  **OrganizationMember**:
    * Campos: `role` (Enum Role), `status` (Enum MemberStatus).
6.  **ChangeRequest**:
    * Campos: `newFaceImageId` (String), `status` (Enum RequestStatus), `requestedAt` (Instant).
7.  **RegisteredClient**:
    * Campos: `ipAddress` (String), `lastCheckin` (Instant), `status` (Enum ClientStatus).
#### b) Repositórios (Adaptação Firestore)
Como utilizamos Firestore, os repositórios são classes anotadas com `@Repository` que manipulam as `Collections`.
* Exemplo: `UserRepository`  define métodos como `save`, `findById`, `findByEmail`.
* Exemplo: `OrganizationRepository` manipula a coleção `"organizations"`.

#### c) Classes de Serviços (CRUD)
Lógica de negócios implementada nas classes `@Service`:
* **Criar:** `OrganizationService.createOrganization`, `UserService.createUser`.
* **Ler:** `OrganizationMemberService.getAllMembers`, `UserService.getUserById`.
* **Atualizar:** `OrganizationService.updateOrganization`, `OrganizationMemberService.updateMemberRole`.
* **Remover:** `OrganizationController.delete`, `OrganizationMemberService.removeMember`.

#### d) Controladores REST
Controladores expõem rotas completas. O destaque é o **`OrganizationController`**):
* `POST /organizations`: Criação.
* `GET /organizations`: **Listar todos**.
* `GET /organizations/{id}`: **Pegar por ID**.
* `PUT /organizations/{id}`: Atualização.
* `DELETE /organizations/{id}`: Remoção.

#### e) Validações com Bean Validation
Utilizamos anotações do `jakarta.validation` nos DTOs e `@Valid` nos Controllers.


#### f) Documentação API (Swagger/OpenAPI)
A documentação é gerada automaticamente pelo `springdoc-openapi`.
* Configuração: `/configuration/OpenApiConfig.kt`.
* Acesso: `/swagger-ui.html`

* As capturas de tela dos endpoints funcionando (requests e responses) devem ser anexadas separadamente na entrega conforme solicitado.



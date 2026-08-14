# Fortis Trust — Banking Management System

A full-stack banking application with secure authentication, real-time transactions, fraud detection, and analytics dashboards for both users and admins.

Built as a two-part project:

* **`bankingapp/`** — Java Spring Boot REST API
* **`banking-frontend/`** — React (Vite) single-page app

\---

## ✨ Features

### Authentication \& Account Security

* Email OTP-based registration and verification
* JWT-based login (1-hour token expiry, role-based access: `USER` / `ADMIN`)
* Forgot password flow via OTP
* Automatic account lockout after 5 failed login attempts (with time-based decay)
* Account unlock via OTP or admin override
* Separate 6-digit **transaction PIN** (hashed independently from the login password) required for withdrawals and transfers

### Core Banking Operations

* Deposit, withdraw, and transfer funds
* Pessimistic row-locking on balances to prevent race conditions on concurrent transactions
* Full transaction ledger per account

### Fraud Detection

* **Velocity check** — auto-locks an account if 5+ transactions occur within 1 minute
* **Drain check** — auto-locks an account if >90% of balance is withdrawn within 2 minutes
* Email alerts sent automatically when an account is restricted

### Analytics

**User dashboard**

* Spending totals, credit vs. debit breakdown, transaction frequency, and a derived "financial health score"

**Admin dashboard**

* Total bank balance, user growth, transaction volume, cash flow, top spenders, wealthiest accounts, account distribution by balance tier, and a searchable/sortable/paginated transaction ledger

\---

## 🛠️ Tech Stack

|Layer|Technology|
|-|-|
|Backend|Java, Spring Boot, Spring Security, Spring Data JPA (Hibernate)|
|Auth|JWT (`jjwt`), BCrypt password hashing|
|Database|MySQL / PostgreSQL (relational)|
|Email|Spring Mail (`JavaMailSender`)|
|Frontend|React 19, Vite 8|
|Routing|React Router v7|
|Styling|Tailwind CSS v4|
|HTTP Client|Axios|
|Charts|Chart.js + react-chartjs-2|
|Icons|lucide-react|

\---

## 📂 Project Structure

```
Banking-management-system/
├── bankingapp/                     # Spring Boot backend
│   └── src/main/java/com/shravya/bankingapp/
│       ├── config/                 # Security, JWT, app-level config
│       ├── controller/             # REST endpoints
│       ├── service/                # Business logic
│       ├── repository/             # Spring Data JPA repositories
│       ├── entity/                 # JPA entities (User, Account, Transaction)
│       ├── dto/                    # Response DTOs
│       └── exception/              # Custom exceptions + global handler
│
└── banking-frontend/                # React frontend
    └── src/
        ├── pages/                   # Login, Register, Dashboards, Analytics, etc.
        └── api.js                   # Centralized Axios instance
```

\---

## 🚀 Getting Started

### Prerequisites

* **JDK 17+**
* **Maven** (or the Maven wrapper, if included)
* **Node.js 18+** and npm
* **MySQL** or **PostgreSQL** running locally
* An SMTP-capable email account (for sending OTPs and alerts)

### 1\. Backend Setup

```bash
cd bankingapp
```

Create/edit `src/main/resources/application.properties` with your own values:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/banking\_db
spring.datasource.username=YOUR\_DB\_USERNAME
spring.datasource.password=YOUR\_DB\_PASSWORD
spring.jpa.hibernate.ddl-auto=update

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR\_EMAIL
spring.mail.password=YOUR\_APP\_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Run the server:

```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080` by default.

### 2\. Frontend Setup

```bash
cd banking-frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`.

> The backend's CORS config currently allows only `http://localhost:5173` — update `SecurityConfig.java` if you run the frontend on a different port.

\---

## 🔑 Environment \& Security Notes

* The JWT signing secret is currently defined directly in `JwtUtil.java`. For any real deployment, move it to an environment variable or secrets manager instead of hardcoding it.
* Database and mail credentials should never be committed — keep `application.properties` out of version control (use `application.properties.example` as a template instead) or use environment variables.

\---

## 📡 API Overview

|Area|Base Path|Access|
|-|-|-|
|Auth (login, password reset)|`/auth/\*\*`|Public (login/reset endpoints)|
|User registration \& self-service|`/users/\*\*`|Mixed (register/verify public, rest authenticated)|
|Account operations (deposit, withdraw, transfer)|`/accounts/\*\*`|`USER`, `ADMIN`|
|Transaction history|`/transactions/\*\*`|`USER`, `ADMIN`|
|Admin analytics \& user management|`/admin/\*\*`|`ADMIN` only|

\---

## 🧭 Roadmap Ideas

* Move secrets to environment variables / a `.env` + Spring profiles setup
* Add refresh tokens (current JWT expires in 1 hour with no refresh flow)
* Add automated tests (unit tests for `AccountService`, `FraudDetectionService`)
* Add a `pom.xml`/dependency list and Docker setup for easier onboarding

\---

## 📄 License



This project is licensed under the MIT License — see the \[LICENSE](LICENSE) file for details.


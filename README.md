# 📝 TODO Microservices System

Este repositorio contiene una arquitectura basada en microservicios para una aplicación de gestión de tareas. Está desarrollada usando **Java 17** y **Maven 3.9.9**.

## 🧱 Microservicios incluidos

| Servicio            | Descripción |
|---------------------|-------------|
| `config-server`     | Centraliza la configuración de todos los microservicios mediante Spring Cloud Config. |
| `demoEurekaServer`  | Registro de servicios con Eureka Server para descubrimiento de servicios. |
| `demoApiGateway`    | API Gateway basado en Spring Cloud Gateway que enruta las solicitudes a los microservicios. |
| `To-do-service`     | Servicio principal para la gestión de tareas: crear, leer, actualizar y eliminar tareas. |
| `auth-service`      | Proporciona autenticación y autorización de usuarios, posiblemente con JWT. |
| `admin-server`      | Monitoreo y gestión del sistema usando Spring Boot Admin. |
| `analytics_service` | Provee análisis y reportes basados en las tareas gestionadas por los usuarios. |

---

## 🚀 Tecnologías utilizadas

- **Java 17**
- **Maven 3.9.9**
- **Spring Boot**
- **Spring Cloud (Eureka, Config, Gateway)**
- **Spring Security**
- **Spring Boot Admin**
- **JWT (para autenticación)**

---

## 📦 Requisitos

- Java 17
- Maven 3.9.9
- Git

---

## 🔧 Cómo ejecutar

1. Clona el repositorio:

```bash
git clone https://github.com/kair069/TODO.git
cd TODO

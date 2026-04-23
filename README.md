# TINGESO-TravelAgency-Backend

Backend Spring Boot configurado como Resource Server OAuth2 con Keycloak.

## Variables importantes

Configurar en `src/main/resources/application.properties`:

- `spring.security.oauth2.resourceserver.jwt.issuer-uri`: issuer del realm.
- `keycloak.auth-server-url`: URL base de Keycloak.
- `keycloak.realm`: realm de negocio.
- `keycloak.admin.client-id`: cliente confidencial con service account para usar Admin API.
- `keycloak.admin.client-secret`: secreto del cliente confidencial.

## Modelo de usuarios en Keycloak

-- Roles de realm: `CLIENTE`, `ADMIN`.
-- Campos de nombre:
	- `firstName` = nombres
	- `lastName` = `apellidoPaterno apellidoMaterno`
-- Atributos custom: `rut`, `nacionalidad`, `phone`, `apellidoPaterno`, `apellidoMaterno`, `admin`, `accountActive`.

## Endpoints IAM del backend

- `GET /api/iam/me`: perfil del usuario autenticado.
- `PUT /api/iam/me`: actualización de perfil propio.
- `PUT /api/iam/admin/users/{userId}`: actualización de datos por ADMIN.
- `PATCH /api/iam/admin/users/{userId}/deactivate`: desactiva cuenta (enabled=false y `accountActive=0`).

El registro de nuevos clientes se realiza directamente en la pantalla de login de Keycloak (User Registration habilitado en el realm).

## Politicas requeridas en Keycloak (Realm Settings)

Configurar en `Realm Settings > Security Defenses > Brute Force Detection`:

- `Brute Force Detection`: ON
- `Max Login Failures`: 5
- `Quick Login Check Milli Seconds`: 1000 (o valor por defecto)
- `Wait Increment Seconds`: 60
- `Max Wait`: 60

Con esta configuracion, tras 5 intentos fallidos seguidos se bloquea temporalmente por 1 minuto.

## Nota de arquitectura

La entidad local de usuario y sus capas (`Entity/Repository/Service/Controller`) fueron retiradas para evitar duplicacion de identidad con Keycloak. El backend no registra usuarios; solo consulta/actualiza usuarios existentes en Keycloak.

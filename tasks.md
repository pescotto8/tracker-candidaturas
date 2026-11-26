# Tareas v2.0 (JDBC + JavaFX)

Código generado con ayuda de Claude (Anthropic) y revisado por el equipo. Cada tarea va en su rama y se integra en `main` con pull request. Base de datos: MySQL.

## Acceso a Datos (Pablo)

| Rama | Contenido |
|------|-----------|
| feature/v2-base-datos | schema.sql, driver MySQL, configuración externalizada, Database y transacciones |
| feature/v2-modelo | Empresa, Oferta, Entrevista y sus enums |
| feature/v2-repositorios | Repositorios JDBC, filtros combinados, JOIN, cambio de estado |
| feature/v2-servicios | Servicios, validaciones, importación del CSV y operaciones transaccionales |
| feature/v2-tests-datos | Tests con JUnit 5, Mockito y H2 |
| refactor/retirar-cli | Eliminar la CLI y el repositorio de archivos de la v1.0 |

## Desarrollo de Interfaces (David)

| Rama | Contenido |
|------|-----------|
| feature/v2-ui-base | JavaFX en el pom, MainApp, navegación con FXMLLoader, estilos del manual, barra lateral |
| feature/v2-ui-ofertas | Listado con filtros, detalle y formulario de oferta |
| feature/v2-ui-empresas | Listado y formulario de empresa |
| feature/v2-ui-entrevistas | Panel de entrevistas en el detalle y formulario |

## Cierre

| Rama | Contenido |
|------|-----------|
| docs/readme-v2 | README con instrucciones de la v2.0 y tabla de contribuciones |

Después: vídeo de la aplicación JavaFX y tag `v2.0` en `main`.

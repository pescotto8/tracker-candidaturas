# Tareas v2.0 (JDBC + JavaFX)

Código generado con ayuda de Claude (Anthropic) y revisado por el equipo. Cada tarea va en su rama y se integra en `main` con pull request. Base de datos: MySQL.

| Rama | Responsable | Contenido |
|------|-------------|-----------|
| feature/v2-base-datos | Pablo | schema.sql inicial, driver MySQL, configuración externalizada (hecho, sustituido después) |
| feature/v2-modelo | Pablo | Modelo inicial de empresas, ofertas y entrevistas (hecho, sustituido después) |
| feature/v2-repositorios | Pablo | Primeros repositorios JDBC con filtros y JOIN (hecho, sustituido después) |
| feature/v2-servicios | Pablo | Primeros servicios, validaciones e importación (hecho, sustituido después) |
| refactor/retirar-v1 | Pablo | Eliminar la CLI y el código anterior |
| feature/v2-ui | David | Aplicación JavaFX: pantallas FXML, controladores, repositorios y servicios |
| feature/v2-importador-csv | Pablo | Esquema idempotente, importación del CSV de la v1.0 sin duplicados, configuración |
| feature/v2-tests | Pablo | Tests de validaciones e importación |
| docs/readme-v2 | Pablo y David | README y tabla de contribuciones |

Después: vídeo de la aplicación y tag `v2.0` en `main`.

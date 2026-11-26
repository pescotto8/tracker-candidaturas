# Impulso · Tracker de Candidaturas Laborales

Aplicación de escritorio para gestionar candidaturas laborales: empresas, ofertas y entrevistas.
Ejercicio integrador 1er trimestre (Acceso a Datos + Desarrollo de Interfaces).

Equipo: Pablo Escotto Pérez y David Trigo Sánchez

La versión 2.0 guarda los datos en **MySQL mediante JDBC** y ofrece una interfaz **JavaFX** (FXML, diseñada para Scene Builder) basada en el prototipo de Figma. La versión 1.0 (CLI sobre archivo de texto) está en la etiqueta `v1.0`.

## Tecnologías

- Java 24, Maven
- JavaFX 21 (FXML, Scene Builder)
- MySQL 8 + JDBC (`PreparedStatement`, transacciones)
- JUnit 5

## Puesta en marcha

1. Arrancar MySQL. Con Docker:
   ```
   docker run --name impulso-mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8.4
   ```
2. Crear el esquema (es idempotente, se puede ejecutar varias veces):
   ```
   mysql -uroot -proot < database/schema.sql
   ```
   Si antes había una versión anterior del esquema, ejecutar primero `DROP DATABASE impulso;`.
3. Copiar `config.properties.example` como `config.properties` y ajustar usuario, contraseña y puerto. El archivo no se sube a Git.
   Cada valor se puede sustituir con las variables de entorno `IMPULSO_DB_URL`, `IMPULSO_DB_USER` e `IMPULSO_DB_PASSWORD`, que tienen prioridad.
4. Ejecutar la aplicación:
   ```
   mvn javafx:run
   ```
   Desde IntelliJ se puede lanzar la clase `com.impulso.Launcher`.
5. En **Candidaturas → Importar CSV** elegir `data/candidaturas_prueba.csv`.

Tests (no necesitan base de datos):

```
mvn test
```

## Estructura

```
database/schema.sql          esquema MySQL (empresa, oferta, entrevista)
config.properties.example    plantilla de la conexión externalizada
data/candidaturas_prueba.csv datos iniciales (CSV de la v1.0)
src/main/java/com/impulso
  model/        Empresa, Oferta, Entrevista, enums y OfertaFilter
  db/           Database (conexiones y transacciones), DataAccessException
  repository/   JDBC con PreparedStatement, JOIN y filtros dinámicos
  service/      ImpulsoService (transacciones), Validador, CsvImporter
  controller/   controladores FXML y Navigator (FXMLLoader)
  util/         Config, Fmt, Dialogs
src/main/resources/com/impulso/view/*.fxml   pantallas (se abren en Scene Builder)
src/main/resources/com/impulso/css/impulso.css
```

## Requisitos de la v2.0

| Requisito | Dónde |
|---|---|
| Esquema con claves foráneas | `database/schema.sql` |
| Configuración externalizada | `util/Config`, `config.properties`, variables de entorno |
| Operaciones de la v1.0 con JDBC | `repository/*`, `ImpulsoService` |
| Filtros combinables (empresa, estado, fechas, salario, texto) | `OfertaRepository.where` |
| CRUD de empresas y entrevistas | `EmpresaRepository`, `EntrevistaRepository` y sus pantallas |
| Cambiar el estado de una oferta | listado y detalle → `ImpulsoService.cambiarEstado` |
| Importar el CSV inicial | `CsvImporter` |
| Consultas con JOIN | `OfertaRepository` (oferta y empresa), `EmpresaRepository` (LEFT JOIN y COUNT), estadísticas |
| Transacciones | importar CSV, eliminar empresa u oferta, guardar entrevista y avanzar el estado |
| Pantallas FXML y navegación con FXMLLoader | `view/*.fxml`, `controller/*`, `Navigator` |
| Validación en formularios | `Validador` |

La importación es atómica: si una línea del CSV no es válida no se guarda nada y el mensaje indica la línea.
Importar dos veces el mismo archivo no duplica datos: las candidaturas con la misma empresa, título y fecha se omiten.
Las empresas que solo aparecen en el CSV se crean con el sector "Sin especificar".
Un filtro por salario solo devuelve candidaturas que tienen salario.

## Diseño

- Prototipo Figma (solo lectura)
- Manual de estilo

## Contribuciones

| Miembro | v1.0 | v2.0 | v3.0 |
|---------|------|------|------|
| Pablo Escotto Pérez | Modelo, persistencia, servicio, datos de prueba, README | Esquema SQL, configuración externalizada, importación del CSV, tests, integración, README | |
| David Trigo Sánchez | CLI, tests | Interfaz JavaFX (FXML, controladores, estilos), repositorios JDBC y servicios | |

Código desarrollado con ayuda de Claude (Anthropic) y revisado por el equipo.

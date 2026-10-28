# Tracker de Candidaturas Laborales

Aplicación Java para gestionar candidaturas laborales. Ejercicio integrador 1er trimestre (Acceso a Datos + Desarrollo de Interfaces).

Equipo: Pablo Escotto Pérez, David Trigo

## Tecnologías (v1.0)

- Java 24
- Maven
- Persistencia en archivo de texto plano (CSV con `;`)
- JUnit 5 + Mockito
- Figma (prototipo y manual de estilo)

## Compilar y ejecutar (v1.0)

```
mvn package
java -jar target/tracker.jar -importar data/candidaturas_prueba.csv
java -jar target/tracker.jar -listar
java -jar target/tracker.jar -listar --estado pendiente --empresa Indra
java -jar target/tracker.jar -añadir --titulo "Dev Java" --empresa Indra --estado pendiente --fecha 28/10/2026
java -jar target/tracker.jar -ver 13
java -jar target/tracker.jar -borrar 13
```

Los datos se guardan en `data/candidaturas.txt`. Se puede cambiar con la variable de entorno `TRACKER_DATA`.
Si la consola no muestra bien la `ñ`, ejecutar con `java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar ...` o usar `-anadir`.

## Datos de prueba

[data/candidaturas_prueba.csv](data/candidaturas_prueba.csv)

## Diseño

- Prototipo Figma (solo lectura): _enlace_
- Manual de estilo: _enlace al PDF_

## Contribuciones

| Miembro | v1.0 | v2.0 | v3.0 |
|---------|------|------|------|
| Pablo Escotto Pérez | Modelo, persistencia, servicio, datos de prueba, README | | |
| David Trigo | CLI, tests | | |

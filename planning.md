# Planning v1.0

Código generado con ayuda de Claude (Anthropic) y revisado por el equipo. Cada paso se integra con su propia rama y commits.

## Flujo de trabajo

- `main` siempre estable; cada tarea en una rama `feature/*`, `docs/*` o `design/*` y se une a `main` con pull request.
- Mensajes de commit en imperativo y descriptivos.
- Cada miembro del equipo hace sus propios commits en las ramas que le toquen.

## Ramas y tareas

| Rama | Contenido |
|------|-----------|
| feature/modelo | Estado, Candidatura, TrackerException |
| feature/persistencia | CandidaturaCodec, CandidaturaRepository |
| feature/servicio | CandidaturaService |
| feature/cli | Cli, Main |
| feature/tests | Tests de servicio y CLI |
| docs/datos-readme | CSV de prueba y README |
| design/manual-estilo | Manual de estilo (Figma + PDF) |
| design/prototipo | Prototipo interactivo (Figma + PDF) |

## Entrega

Tag `v1.0` en `main`.

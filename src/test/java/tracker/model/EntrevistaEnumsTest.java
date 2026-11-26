package tracker.model;

import org.junit.jupiter.api.Test;
import tracker.exception.TrackerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntrevistaEnumsTest {

    @Test
    void typeParsesWithAndWithoutAccents() {
        assertEquals(TipoEntrevista.TELEFONICA, TipoEntrevista.desdeTexto("Telefónica"));
        assertEquals(TipoEntrevista.TECNICA, TipoEntrevista.desdeTexto("tecnica"));
        assertEquals(TipoEntrevista.ONLINE, TipoEntrevista.desdeTexto(" ONLINE "));
    }

    @Test
    void typeKeepsStoredLabelAndDisplayName() {
        assertEquals("tecnica", TipoEntrevista.TECNICA.etiqueta());
        assertEquals("Técnica", TipoEntrevista.TECNICA.toString());
    }

    @Test
    void invalidTypeFailsWithClearMessage() {
        TrackerException error = assertThrows(TrackerException.class, () -> TipoEntrevista.desdeTexto("grupal"));

        assertEquals("Tipo de entrevista no válido: 'grupal'. Valores posibles: telefonica, tecnica, presencial, online",
                error.getMessage());
    }

    @Test
    void resultParsesAndKeepsLabels() {
        assertEquals(ResultadoEntrevista.POSITIVO, ResultadoEntrevista.desdeTexto("Positivo"));
        assertEquals("negativo", ResultadoEntrevista.NEGATIVO.etiqueta());
        assertEquals("Pendiente", ResultadoEntrevista.PENDIENTE.toString());
    }

    @Test
    void invalidResultFails() {
        assertThrows(TrackerException.class, () -> ResultadoEntrevista.desdeTexto("quizá"));
    }
}

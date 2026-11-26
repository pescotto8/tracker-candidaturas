package tracker.model;

import org.junit.jupiter.api.Test;
import tracker.exception.TrackerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoTest {

    @Test
    void parsesIgnoringCaseAndSpaces() {
        assertEquals(Estado.PENDIENTE, Estado.desdeTexto("PENDIENTE"));
        assertEquals(Estado.ENTREVISTA, Estado.desdeTexto("  entrevista "));
        assertEquals(Estado.ACEPTADO, Estado.desdeTexto("Aceptado"));
    }

    @Test
    void invalidTextListsValidValues() {
        TrackerException error = assertThrows(TrackerException.class, () -> Estado.desdeTexto("raro"));

        assertTrue(error.getMessage().contains("'raro'"));
        assertTrue(error.getMessage().contains("pendiente, entrevista, rechazado, aceptado"));
    }

    @Test
    void nullTextIsInvalid() {
        assertThrows(TrackerException.class, () -> Estado.desdeTexto(null));
    }

    @Test
    void labelIsStoredValueAndToStringIsDisplayName() {
        assertEquals("rechazado", Estado.RECHAZADO.etiqueta());
        assertEquals("Rechazado", Estado.RECHAZADO.toString());
    }
}

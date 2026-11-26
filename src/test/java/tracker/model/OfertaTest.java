package tracker.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfertaTest {

    private final Empresa empresa = new Empresa(3, "Indra", "Tecnología", "indracompany.com", LocalDate.of(2026, 4, 2));
    private final Oferta nueva = new Oferta(0, empresa, "Desarrollador Java", "Backend", Estado.PENDIENTE,
            LocalDate.of(2026, 10, 20), "https://indra.example/java", new BigDecimal("32000.00"), "Contacto por LinkedIn");

    @Test
    void newOfferHasNoId() {
        assertTrue(nueva.esNueva());
        assertFalse(nueva.conId(7).esNueva());
    }

    @Test
    void withIdKeepsRestOfFields() {
        Oferta guardada = nueva.conId(7);

        assertEquals(7, guardada.id());
        assertEquals(nueva.empresa(), guardada.empresa());
        assertEquals(nueva.titulo(), guardada.titulo());
        assertEquals(nueva.salario(), guardada.salario());
    }

    @Test
    void withStateChangesOnlyTheState() {
        Oferta aceptada = nueva.conEstado(Estado.ACEPTADO);

        assertEquals(Estado.ACEPTADO, aceptada.estado());
        assertEquals(Estado.PENDIENTE, nueva.estado());
        assertEquals(nueva.conEstado(Estado.PENDIENTE), nueva);
    }

    @Test
    void companyAndInterviewIdsWork() {
        assertTrue(empresa.conId(0).esNueva());
        assertEquals("Indra", empresa.toString());
        Entrevista entrevista = new Entrevista(0, 7, LocalDate.of(2026, 11, 2), null,
                TipoEntrevista.ONLINE, "", ResultadoEntrevista.PENDIENTE);
        assertEquals(5, entrevista.conId(5).id());
        assertEquals(7, entrevista.conId(5).idOferta());
    }
}

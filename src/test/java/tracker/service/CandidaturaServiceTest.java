package tracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tracker.exception.TrackerException;
import tracker.model.Candidatura;
import tracker.model.Estado;
import tracker.repository.CandidaturaRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidaturaServiceTest {

    @Mock
    private CandidaturaRepository repositorio;

    private static Candidatura candidatura(long id, String empresa, Estado estado) {
        return new Candidatura(id, "Dev", empresa, estado, LocalDate.of(2026, 10, 1), "", "");
    }

    private List<Candidatura> datos() {
        return new ArrayList<>(List.of(
                candidatura(1, "Indra", Estado.PENDIENTE),
                candidatura(2, "Indra", Estado.ENTREVISTA),
                candidatura(7, "Accenture", Estado.PENDIENTE)));
    }

    @Test
    void listarFiltraPorEstadoYEmpresa() {
        when(repositorio.buscarTodas()).thenReturn(datos());
        CandidaturaService servicio = new CandidaturaService(repositorio);

        List<Candidatura> resultado = servicio.listar(Estado.PENDIENTE, "indra");

        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).id());
    }

    @Test
    void anadirAsignaSiguienteIdYGuarda() {
        when(repositorio.buscarTodas()).thenReturn(datos());
        CandidaturaService servicio = new CandidaturaService(repositorio);

        Candidatura nueva = servicio.anadir("Dev; Java", "Indra", Estado.PENDIENTE, LocalDate.of(2026, 10, 28), null, "nota");

        assertEquals(8, nueva.id());
        assertEquals("Dev, Java", nueva.titulo());
        ArgumentCaptor<List<Candidatura>> captor = ArgumentCaptor.forClass(List.class);
        verify(repositorio).guardarTodas(captor.capture());
        assertEquals(4, captor.getValue().size());
    }

    @Test
    void anadirSinTituloLanzaErrorYNoGuarda() {
        CandidaturaService servicio = new CandidaturaService(repositorio);

        assertThrows(TrackerException.class,
                () -> servicio.anadir(" ", "Indra", Estado.PENDIENTE, LocalDate.now(), "", ""));
        verify(repositorio, never()).guardarTodas(anyList());
    }

    @Test
    void verConIdInexistenteLanzaError() {
        when(repositorio.buscarTodas()).thenReturn(datos());
        CandidaturaService servicio = new CandidaturaService(repositorio);

        assertThrows(TrackerException.class, () -> servicio.ver(99));
    }

    @Test
    void borrarEliminaLaCandidaturaYGuarda() {
        when(repositorio.buscarTodas()).thenReturn(datos());
        CandidaturaService servicio = new CandidaturaService(repositorio);

        servicio.borrar(2);

        ArgumentCaptor<List<Candidatura>> captor = ArgumentCaptor.forClass(List.class);
        verify(repositorio).guardarTodas(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    @Test
    void borrarConIdInexistenteLanzaError() {
        when(repositorio.buscarTodas()).thenReturn(datos());
        CandidaturaService servicio = new CandidaturaService(repositorio);

        assertThrows(TrackerException.class, () -> servicio.borrar(99));
        verify(repositorio, never()).guardarTodas(anyList());
    }
}

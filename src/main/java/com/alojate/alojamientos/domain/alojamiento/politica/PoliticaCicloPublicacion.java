package com.alojate.alojamientos.domain.alojamiento.politica;

import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoPublicacion;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

/**
 * Strategy: ciclo distinto para oferta única (casa/depto) vs establecimiento (hotel/hostal).
 */
public sealed interface PoliticaCicloPublicacion
        permits PoliticaCicloPublicacion.OfertaUnica, PoliticaCicloPublicacion.Establecimiento {

    static PoliticaCicloPublicacion desdeNombreTipo(String nombreTipo) {
        if (nombreTipo == null || nombreTipo.isBlank()) {
            throw new ReglaNegocioException("CATALOGO_INVALIDO", "El tipo de propiedad no existe");
        }
        String n = nombreTipo.toLowerCase();
        if (n.contains("hotel") || n.contains("hostal")) {
            return Establecimiento.INSTANCE;
        }
        if (n.contains("casa") || n.contains("departamento")) {
            return OfertaUnica.INSTANCE;
        }
        return Establecimiento.INSTANCE;
    }

    boolean esOfertaUnica();

    boolean esEstablecimiento();

    boolean listoParaFinalizarRegistro(boolean establecimientoCompleto, boolean tieneOfertaLista);

    EstadoPublicacion estadoTrasFinalizarRegistro();

    /** Quién puede enviar a moderación a nivel alojamiento. */
    boolean admiteEnviarARevision(EstadoPublicacion estado);

    boolean listoParaRevision(boolean establecimientoCompleto, boolean tieneOfertaLista);

    final class OfertaUnica implements PoliticaCicloPublicacion {
        public static final OfertaUnica INSTANCE = new OfertaUnica();

        private OfertaUnica() {
        }

        @Override
        public boolean esOfertaUnica() {
            return true;
        }

        @Override
        public boolean esEstablecimiento() {
            return false;
        }

        @Override
        public boolean listoParaFinalizarRegistro(boolean establecimientoCompleto, boolean tieneOfertaLista) {
            return establecimientoCompleto && tieneOfertaLista;
        }

        @Override
        public EstadoPublicacion estadoTrasFinalizarRegistro() {
            return EstadoPublicacion.EN_REVISION;
        }

        @Override
        public boolean admiteEnviarARevision(EstadoPublicacion estado) {
            return estado == EstadoPublicacion.BORRADOR || estado == EstadoPublicacion.RECHAZADO;
        }

        @Override
        public boolean listoParaRevision(boolean establecimientoCompleto, boolean tieneOfertaLista) {
            return establecimientoCompleto && tieneOfertaLista;
        }
    }

    final class Establecimiento implements PoliticaCicloPublicacion {
        public static final Establecimiento INSTANCE = new Establecimiento();

        private Establecimiento() {
        }

        @Override
        public boolean esOfertaUnica() {
            return false;
        }

        @Override
        public boolean esEstablecimiento() {
            return true;
        }

        @Override
        public boolean listoParaFinalizarRegistro(boolean establecimientoCompleto, boolean tieneOfertaLista) {
            return establecimientoCompleto;
        }

        @Override
        public EstadoPublicacion estadoTrasFinalizarRegistro() {
            return EstadoPublicacion.ACTIVO;
        }

        @Override
        public boolean admiteEnviarARevision(EstadoPublicacion estado) {
            // La moderación es por habitación, no por hotel.
            return false;
        }

        @Override
        public boolean listoParaRevision(boolean establecimientoCompleto, boolean tieneOfertaLista) {
            return establecimientoCompleto && tieneOfertaLista;
        }
    }
}

package com.alojate.alojamientos.application.ports.input;

import com.alojate.alojamientos.application.dto.AgregarUnidadCommand;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse;
import com.alojate.alojamientos.application.dto.FinalizarRegistroCommand;
import com.alojate.alojamientos.application.dto.GuardarFotografiasCommand;
import com.alojate.alojamientos.application.dto.GuardarOfertaUnicaCommand;
import com.alojate.alojamientos.application.dto.GuardarPoliticasCommand;
import com.alojate.alojamientos.application.dto.GuardarPresentacionCommand;
import com.alojate.alojamientos.application.dto.GuardarServiciosCommand;
import com.alojate.alojamientos.application.dto.GuardarUbicacionCommand;
import com.alojate.alojamientos.application.dto.ImagenPublicadaResponse;
import com.alojate.alojamientos.application.dto.PublicarFotografiaCommand;
import com.alojate.alojamientos.application.dto.RegistrarAlojamientoCommand;

public interface RegistrarAlojamiento {

    AlojamientoRegistradoResponse registrar(RegistrarAlojamientoCommand comando);

    AlojamientoRegistradoResponse guardarUbicacion(GuardarUbicacionCommand comando);

    AlojamientoRegistradoResponse guardarPresentacion(GuardarPresentacionCommand comando);

    AlojamientoRegistradoResponse guardarPoliticas(GuardarPoliticasCommand comando);

    AlojamientoRegistradoResponse guardarServicios(GuardarServiciosCommand comando);

    AlojamientoRegistradoResponse guardarFotografias(GuardarFotografiasCommand comando);

    AlojamientoRegistradoResponse publicarFotografia(PublicarFotografiaCommand comando);

    /**
     * Sube una imagen al almacén sin asociarla al establecimiento.
     * Útil para portadas de habitación/unidad.
     */
    ImagenPublicadaResponse publicarImagen(PublicarFotografiaCommand comando);

    /** Crea un solo tipo de habitación sin reemplazar las existentes. */
    AlojamientoRegistradoResponse agregarUnidad(AgregarUnidadCommand comando);

    AlojamientoRegistradoResponse guardarOfertaUnica(GuardarOfertaUnicaCommand comando);

    /**
     * Cierra el wizard según strategy: casa/depto → EN_REVISION; hotel/hostal → ACTIVO.
     */
    AlojamientoRegistradoResponse finalizarRegistro(FinalizarRegistroCommand comando);
}

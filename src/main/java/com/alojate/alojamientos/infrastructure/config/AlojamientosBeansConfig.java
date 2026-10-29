package com.alojate.alojamientos.infrastructure.config;

import com.alojate.alojamientos.application.ports.input.AdministrarAlojamientos;
import com.alojate.alojamientos.application.ports.input.ConsultarCatalogosAlojamiento;
import com.alojate.alojamientos.application.ports.input.ExplorarCatalogo;
import com.alojate.alojamientos.application.ports.input.GestionarMisAlojamientos;
import com.alojate.alojamientos.application.ports.input.RegistrarAlojamiento;
import com.alojate.alojamientos.application.ports.output.AccesoAdministrador;
import com.alojate.alojamientos.application.ports.output.AccesoAnfitrion;
import com.alojate.alojamientos.application.ports.output.AccesoHuesped;
import com.alojate.alojamientos.application.ports.output.AlmacenDeImagenes;
import com.alojate.alojamientos.application.ports.output.Geocodificador;
import com.alojate.alojamientos.application.ports.output.NombreAnfitrion;
import com.alojate.alojamientos.application.ports.output.Reloj;
import com.alojate.alojamientos.application.ports.output.UnitOfWork;
import com.alojate.alojamientos.application.services.AlojamientosDelAnfitrion;
import com.alojate.alojamientos.application.services.administracion.AdministrarAlojamientosService;
import com.alojate.alojamientos.application.services.administracion.PresentadorModeracion;
import com.alojate.alojamientos.application.services.catalogo.ExplorarCatalogoService;
import com.alojate.alojamientos.application.services.catalogo.PresentadorCatalogo;
import com.alojate.alojamientos.application.services.gestion.GestionarMisAlojamientosService;
import com.alojate.alojamientos.application.services.registro.CompletarUbicacion;
import com.alojate.alojamientos.application.services.registro.ConsultarCatalogosAlojamientoService;
import com.alojate.alojamientos.application.services.registro.PresentadorAlojamiento;
import com.alojate.alojamientos.application.services.registro.RegistrarAlojamientoService;
import com.alojate.alojamientos.domain.alojamiento.repository.AlojamientoRepository;
import com.alojate.alojamientos.domain.catalogo.repository.CatalogoAlojamientoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlojamientosBeansConfig {

    @Bean
    AlojamientosDelAnfitrion alojamientosDelAnfitrion(
            AlojamientoRepository alojamientos, AccesoAnfitrion acceso) {
        return new AlojamientosDelAnfitrion(alojamientos, acceso);
    }

    @Bean
    PresentadorAlojamiento presentadorAlojamiento(CatalogoAlojamientoRepository catalogos) {
        return new PresentadorAlojamiento(catalogos);
    }

    @Bean
    RegistrarAlojamiento registrarAlojamiento(
            AlojamientoRepository alojamientos,
            CatalogoAlojamientoRepository catalogos,
            AlojamientosDelAnfitrion propios,
            AlmacenDeImagenes almacen,
            Geocodificador geocodificador,
            PresentadorAlojamiento presentador,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        return new RegistrarAlojamientoService(
                alojamientos,
                catalogos,
                propios,
                almacen,
                new CompletarUbicacion(geocodificador),
                presentador,
                reloj,
                unitOfWork);
    }

    @Bean
    GestionarMisAlojamientos gestionarMisAlojamientos(
            AlojamientoRepository alojamientos,
            CatalogoAlojamientoRepository catalogos,
            AlojamientosDelAnfitrion propios,
            PresentadorAlojamiento presentador,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        return new GestionarMisAlojamientosService(
                alojamientos, catalogos, propios, presentador, reloj, unitOfWork);
    }

    @Bean
    ExplorarCatalogo explorarCatalogo(
            AlojamientoRepository alojamientos,
            AccesoHuesped acceso,
            CatalogoAlojamientoRepository catalogos,
            UnitOfWork unitOfWork) {
        return new ExplorarCatalogoService(
                alojamientos, acceso, new PresentadorCatalogo(catalogos), unitOfWork);
    }

    @Bean
    AdministrarAlojamientos administrarAlojamientos(
            AlojamientoRepository alojamientos,
            AccesoAdministrador acceso,
            CatalogoAlojamientoRepository catalogos,
            NombreAnfitrion nombres,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        return new AdministrarAlojamientosService(
                alojamientos,
                catalogos,
                acceso,
                new PresentadorModeracion(catalogos, nombres),
                reloj,
                unitOfWork);
    }

    @Bean
    ConsultarCatalogosAlojamiento consultarCatalogosAlojamiento(
            CatalogoAlojamientoRepository catalogos, AccesoAnfitrion acceso) {
        return new ConsultarCatalogosAlojamientoService(catalogos, acceso);
    }
}

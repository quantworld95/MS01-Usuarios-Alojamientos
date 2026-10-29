package com.alojate.identidad.infrastructure.config;

import com.alojate.identidad.application.ports.input.GestionarRoles;
import com.alojate.identidad.application.ports.input.IniciarSesion;
import com.alojate.identidad.application.ports.input.GestionarUsuarioCorporativo;
import com.alojate.identidad.application.ports.input.GestionarUsuarioPublico;
import com.alojate.identidad.application.ports.input.RegistrarUsuarioPublico;
import com.alojate.identidad.application.ports.input.ReenviarVerificacion;
import com.alojate.identidad.application.ports.input.VerificarCorreo;
import com.alojate.identidad.application.ports.output.AuditoriaPort;
import com.alojate.identidad.application.ports.output.EmisorDeSesion;
import com.alojate.identidad.application.ports.output.EmailPort;
import com.alojate.identidad.application.ports.output.PasswordHasherPort;
import com.alojate.identidad.application.ports.output.Reloj;
import com.alojate.identidad.application.ports.output.UnitOfWork;
import com.alojate.identidad.application.services.rol.GestionarRolesService;
import com.alojate.identidad.application.services.usuario.IniciarSesionService;
import com.alojate.identidad.application.services.usuario.GestionarUsuarioCorporativoService;
import com.alojate.identidad.application.services.usuario.GestionarUsuarioPublicoService;
import com.alojate.identidad.application.services.usuario.RegistrarUsuarioPublicoService;
import com.alojate.identidad.application.services.usuario.ReenviarVerificacionService;
import com.alojate.identidad.application.services.usuario.VerificarCorreoService;
import com.alojate.identidad.domain.modulo.repository.ModuloRepository;
import com.alojate.identidad.domain.restriccion.repository.RestriccionRepository;
import com.alojate.identidad.domain.rol.repository.RolRepository;
import com.alojate.identidad.domain.usuario.factory.UsuarioFactory;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.infrastructure.adapters.output.persistence.mapper.UsuarioMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentidadBeansConfig {

    @Bean
    UsuarioFactory usuarioFactory() {
        return new UsuarioFactory();
    }

    @Bean
    UsuarioMapper usuarioMapper() {
        return new UsuarioMapper();
    }

    @Bean
    RegistrarUsuarioPublico registrarUsuarioPublico(
            UsuarioRepository usuarios,
            UsuarioFactory fabrica,
            PasswordHasherPort cifrador,
            EmailPort correo,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        return new RegistrarUsuarioPublicoService(usuarios, fabrica, cifrador, correo, reloj, unitOfWork);
    }

    @Bean
    GestionarUsuarioCorporativo gestionarUsuarioCorporativo(
            UsuarioRepository usuarios,
            RolRepository roles,
            UsuarioFactory fabrica,
            PasswordHasherPort cifrador,
            Reloj reloj,
            UnitOfWork unitOfWork,
            AuditoriaPort auditoria) {
        return new GestionarUsuarioCorporativoService(
                usuarios, roles, fabrica, cifrador, reloj, unitOfWork, auditoria);
    }

    @Bean
    VerificarCorreo verificarCorreo(
            UsuarioRepository usuarios,
            RolRepository roles,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        return new VerificarCorreoService(usuarios, roles, reloj, unitOfWork);
    }

    @Bean
    IniciarSesion iniciarSesion(
            UsuarioRepository usuarios,
            RolRepository roles,
            ModuloRepository modulos,
            RestriccionRepository restricciones,
            PasswordHasherPort cifrador,
            EmisorDeSesion sesiones,
            Reloj reloj) {
        return new IniciarSesionService(usuarios, roles, modulos, restricciones, cifrador, sesiones, reloj);
    }

    @Bean
    GestionarUsuarioPublico gestionarUsuarioPublico(
            UsuarioRepository usuarios,
            RolRepository roles,
            RestriccionRepository restricciones,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        return new GestionarUsuarioPublicoService(usuarios, roles, restricciones, reloj, unitOfWork);
    }

    @Bean
    GestionarRoles gestionarRoles(
            ModuloRepository modulos,
            RolRepository roles,
            UnitOfWork unitOfWork,
            AuditoriaPort auditoria) {
        return new GestionarRolesService(modulos, roles, unitOfWork, auditoria);
    }

    @Bean
    ReenviarVerificacion reenviarVerificacion(
            UsuarioRepository usuarios,
            EmailPort correo,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        return new ReenviarVerificacionService(usuarios, correo, reloj, unitOfWork);
    }
}

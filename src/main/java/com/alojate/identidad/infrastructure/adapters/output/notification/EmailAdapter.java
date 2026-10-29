package com.alojate.identidad.infrastructure.adapters.output.notification;

import com.alojate.identidad.application.ports.output.EmailPort;
import com.alojate.identidad.domain.usuario.valueobject.CodigoVerificacion;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailAdapter implements EmailPort {

    private static final Logger REGISTRO = LoggerFactory.getLogger(EmailAdapter.class);

    private final JavaMailSender correo;
    private final boolean habilitado;
    private final String remitente;

    public EmailAdapter(
            ObjectProvider<JavaMailSender> correo,
            @Value("${alojate.mail.enabled:false}") boolean habilitado,
            @Value("${alojate.mail.from:}") String remitente) {
        this.correo = correo.getIfAvailable();
        this.habilitado = habilitado;
        this.remitente = remitente;
    }

    @Override
    public void enviarCodigoVerificacion(Email email, CodigoVerificacion codigo) {
        if (!habilitado || correo == null || remitente == null || remitente.isBlank()) {
            REGISTRO.info("Codigo de verificacion para {}: {}", email.valor(), codigo.codigo());
            return;
        }

        try {
            var mensaje = correo.createMimeMessage();
            var helper = new MimeMessageHelper(mensaje, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(email.valor());
            helper.setSubject("Codigo de verificacion - Alojate");
            helper.setText(
                    """
                    Tu codigo de verificacion es: %s

                    Si no solicitaste este codigo, ignora este mensaje.
                    """.formatted(codigo.codigo()),
                    false);
            correo.send(mensaje);
            REGISTRO.info("Codigo de verificacion enviado a {}", email.valor());
        } catch (MessagingException | MailException excepcion) {
            REGISTRO.error("No se pudo enviar correo a {}", email.valor(), excepcion);
            throw new IllegalStateException("No se pudo enviar el correo", excepcion);
        }
    }
}

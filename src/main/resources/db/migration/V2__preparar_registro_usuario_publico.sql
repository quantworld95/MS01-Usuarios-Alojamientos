ALTER TABLE usuario
    ALTER COLUMN id_rol DROP NOT NULL;

ALTER TABLE usuario
    ADD COLUMN codigo_verificacion VARCHAR(64),
    ADD COLUMN codigo_verificacion_vence TIMESTAMPTZ;

ALTER TABLE usuario
    ADD CONSTRAINT ck_usuario_rol_segun_estado CHECK (
        (estado = 'PENDIENTE_VERIFICACION' AND id_rol IS NULL)
        OR (estado <> 'PENDIENTE_VERIFICACION' AND id_rol IS NOT NULL)
    );

CREATE UNIQUE INDEX uq_usuario_email_tipo_cuenta
    ON usuario (lower(email), tipo_cuenta);

INSERT INTO rol (nombre, ambito, es_sistema, activo)
VALUES ('USUARIO_PUBLICO', 'PUBLICO', TRUE, TRUE);

ALTER TABLE alojamiento
    ADD COLUMN politicas TEXT;

ALTER TABLE fotografia
    ALTER COLUMN id_alojamiento DROP NOT NULL;

ALTER TABLE fotografia
    ADD COLUMN id_unidad UUID;

ALTER TABLE fotografia
    DROP CONSTRAINT IF EXISTS uq_fotografia_orden;

ALTER TABLE fotografia
    ADD CONSTRAINT fk_fotografia_unidad
        FOREIGN KEY (id_unidad) REFERENCES unidad_reservable (id_unidad);

ALTER TABLE fotografia
    ADD CONSTRAINT ck_fotografia_dueno CHECK (
        (id_alojamiento IS NOT NULL AND id_unidad IS NULL)
        OR (id_alojamiento IS NULL AND id_unidad IS NOT NULL)
    );

CREATE UNIQUE INDEX uq_fotografia_orden_alojamiento
    ON fotografia (id_alojamiento, orden)
    WHERE id_alojamiento IS NOT NULL;

CREATE UNIQUE INDEX uq_fotografia_orden_unidad
    ON fotografia (id_unidad, orden)
    WHERE id_unidad IS NOT NULL;

CREATE INDEX idx_fotografia_unidad ON fotografia (id_unidad);

CREATE TABLE unidad_servicio (
    id_unidad UUID NOT NULL,
    id_servicio INTEGER NOT NULL,
    CONSTRAINT pk_unidad_servicio PRIMARY KEY (id_unidad, id_servicio),
    CONSTRAINT fk_unidad_servicio_unidad FOREIGN KEY (id_unidad) REFERENCES unidad_reservable (id_unidad),
    CONSTRAINT fk_unidad_servicio_servicio FOREIGN KEY (id_servicio) REFERENCES servicio (id_servicio)
);

CREATE INDEX idx_unidad_servicio_servicio ON unidad_servicio (id_servicio);

ALTER TABLE historial_publicacion
    ALTER COLUMN id_alojamiento DROP NOT NULL;

ALTER TABLE historial_publicacion
    ADD COLUMN id_unidad UUID;

ALTER TABLE historial_publicacion
    ADD CONSTRAINT fk_historial_unidad
        FOREIGN KEY (id_unidad) REFERENCES unidad_reservable (id_unidad);

ALTER TABLE historial_publicacion
    ADD CONSTRAINT ck_historial_dueno CHECK (
        (id_alojamiento IS NOT NULL AND id_unidad IS NULL)
        OR (id_alojamiento IS NULL AND id_unidad IS NOT NULL)
    );

CREATE INDEX idx_historial_unidad ON historial_publicacion (id_unidad);

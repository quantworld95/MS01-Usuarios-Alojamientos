ALTER TABLE fotografia
    ADD COLUMN identificador_almacen VARCHAR(255);

UPDATE fotografia
SET identificador_almacen = id_fotografia::text
WHERE identificador_almacen IS NULL;

ALTER TABLE fotografia
    ALTER COLUMN identificador_almacen SET NOT NULL;

ALTER TABLE fotografia
    ADD CONSTRAINT uq_fotografia_almacen UNIQUE (identificador_almacen);

UPDATE tipo_propiedad
SET nombre = 'Casa completa'
WHERE lower(nombre) = 'casa';

UPDATE tipo_propiedad
SET nombre = 'Departamento completo'
WHERE lower(nombre) = 'departamento';

ALTER TABLE alojamiento
    ADD COLUMN caracteristicas_destacadas TEXT,
    ADD COLUMN zona VARCHAR(120),
    ADD COLUMN horario_entrada VARCHAR(10),
    ADD COLUMN horario_salida VARCHAR(10);

ALTER TABLE unidad_reservable
    ADD COLUMN estadia_minima INTEGER,
    ADD COLUMN descuento_semanal NUMERIC(5, 2),
    ADD COLUMN descuento_mensual NUMERIC(5, 2);

UPDATE unidad_reservable
SET estadia_minima = 1
WHERE estadia_minima IS NULL;

ALTER TABLE unidad_reservable
    ALTER COLUMN estadia_minima SET NOT NULL,
    ALTER COLUMN estadia_minima SET DEFAULT 1;

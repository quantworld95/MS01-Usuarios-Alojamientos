ALTER TABLE unidad_reservable
    ADD COLUMN descripcion TEXT;

UPDATE unidad_reservable
SET descripcion = nombre
WHERE descripcion IS NULL;

ALTER TABLE unidad_reservable
    ALTER COLUMN descripcion SET NOT NULL;

INSERT INTO tipo_espacio (nombre)
SELECT 'Estandar'
WHERE NOT EXISTS (SELECT 1 FROM tipo_espacio WHERE lower(nombre) = lower('Estandar'));

INSERT INTO tipo_espacio (nombre)
SELECT 'Superior'
WHERE NOT EXISTS (SELECT 1 FROM tipo_espacio WHERE lower(nombre) = lower('Superior'));

INSERT INTO tipo_espacio (nombre)
SELECT 'Deluxe'
WHERE NOT EXISTS (SELECT 1 FROM tipo_espacio WHERE lower(nombre) = lower('Deluxe'));

INSERT INTO tipo_espacio (nombre)
SELECT 'Suite'
WHERE NOT EXISTS (SELECT 1 FROM tipo_espacio WHERE lower(nombre) = lower('Suite'));

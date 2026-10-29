ALTER TABLE alojamiento
    DROP COLUMN IF EXISTS caracteristicas_destacadas,
    DROP COLUMN IF EXISTS zona;

ALTER TABLE unidad_reservable
    DROP COLUMN IF EXISTS estadia_minima,
    DROP COLUMN IF EXISTS descuento_semanal,
    DROP COLUMN IF EXISTS descuento_mensual;

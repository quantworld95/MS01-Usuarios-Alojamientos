-- Amplía estados de unidad al ciclo de publicación (habitación).
-- Alinea DESACTIVADO (antes PAUSADO / INACTIVA) y limpia hoteles en EN_REVISION/PUBLICADO.

ALTER TABLE unidad_reservable
    ALTER COLUMN estado TYPE VARCHAR(30);

UPDATE unidad_reservable
SET estado = 'PUBLICADO'
WHERE UPPER(estado) = 'ACTIVA';

UPDATE unidad_reservable
SET estado = 'DESACTIVADO'
WHERE UPPER(estado) IN ('INACTIVA', 'PAUSADO');

UPDATE alojamiento
SET estado_publicacion = 'DESACTIVADO'
WHERE UPPER(estado_publicacion) = 'PAUSADO';

-- Hoteles/hostales que quedaron en ciclo de oferta: vuelven a ACTIVO (operativo).
UPDATE alojamiento a
SET estado_publicacion = 'ACTIVO'
FROM tipo_propiedad tp
WHERE a.id_tipo_propiedad = tp.id_tipo_propiedad
  AND (
        LOWER(tp.nombre) LIKE '%hotel%'
     OR LOWER(tp.nombre) LIKE '%hostal%'
  )
  AND UPPER(a.estado_publicacion) IN ('EN_REVISION', 'PUBLICADO', 'RECHAZADO');

UPDATE historial_publicacion
SET estado = 'DESACTIVADO'
WHERE UPPER(estado) = 'PAUSADO';

UPDATE historial_publicacion
SET estado = 'PUBLICADO'
WHERE UPPER(estado) = 'ACTIVA';

UPDATE historial_publicacion
SET estado = 'DESACTIVADO'
WHERE UPPER(estado) = 'INACTIVA';

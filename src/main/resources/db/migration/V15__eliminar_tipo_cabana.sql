-- Quita el tipo "Cabaña" y todos los alojamientos asociados (y sus dependencias).

CREATE TEMP TABLE tmp_cabana_alojamientos ON COMMIT DROP AS
SELECT a.id_alojamiento
FROM alojamiento a
JOIN tipo_propiedad tp ON tp.id_tipo_propiedad = a.id_tipo_propiedad
WHERE lower(tp.nombre) LIKE '%caba%';

CREATE TEMP TABLE tmp_cabana_unidades ON COMMIT DROP AS
SELECT u.id_unidad
FROM unidad_reservable u
WHERE u.id_alojamiento IN (SELECT id_alojamiento FROM tmp_cabana_alojamientos);

DELETE FROM fotografia
WHERE id_unidad IN (SELECT id_unidad FROM tmp_cabana_unidades)
   OR id_alojamiento IN (SELECT id_alojamiento FROM tmp_cabana_alojamientos);

DELETE FROM unidad_servicio
WHERE id_unidad IN (SELECT id_unidad FROM tmp_cabana_unidades);

DELETE FROM historial_publicacion
WHERE id_unidad IN (SELECT id_unidad FROM tmp_cabana_unidades)
   OR id_alojamiento IN (SELECT id_alojamiento FROM tmp_cabana_alojamientos);

DELETE FROM unidad_reservable
WHERE id_unidad IN (SELECT id_unidad FROM tmp_cabana_unidades);

DELETE FROM alojamiento_servicio
WHERE id_alojamiento IN (SELECT id_alojamiento FROM tmp_cabana_alojamientos);

DELETE FROM alojamiento
WHERE id_alojamiento IN (SELECT id_alojamiento FROM tmp_cabana_alojamientos);

DELETE FROM tipo_propiedad
WHERE lower(nombre) LIKE '%caba%';

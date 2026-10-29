INSERT INTO modulo (nombre, descripcion, orden)
VALUES
    ('HUESPED', 'Funciones comunes de un huésped', 2),
    ('ANFITRION', 'Funciones comunes de un anfitrión público', 3);

INSERT INTO permiso (id_modulo, codigo, nombre, descripcion, asignable)
SELECT m.id_modulo, v.codigo, v.nombre, v.descripcion, FALSE
FROM modulo m
JOIN (VALUES
    ('HUESPED', 'BUSCAR_ALOJAMIENTOS', 'Buscar alojamientos', 'Consultar la oferta publicada'),
    ('HUESPED', 'RESERVAR_ALOJAMIENTO', 'Reservar alojamiento', 'Crear una reserva'),
    ('HUESPED', 'CALIFICAR_ESTANCIA', 'Calificar estancia', 'Dejar una calificación después de la estancia'),
    ('ANFITRION', 'PUBLICAR_ALOJAMIENTO', 'Publicar alojamiento', 'Ofrecer un alojamiento'),
    ('ANFITRION', 'GESTIONAR_RESERVAS_PROPIAS', 'Gestionar reservas propias', 'Atender las reservas del alojamiento')
) AS v(modulo, codigo, nombre, descripcion) ON m.nombre = v.modulo;

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
JOIN permiso p ON p.codigo IN (
    'BUSCAR_ALOJAMIENTOS',
    'RESERVAR_ALOJAMIENTO',
    'CALIFICAR_ESTANCIA',
    'PUBLICAR_ALOJAMIENTO',
    'GESTIONAR_RESERVAS_PROPIAS')
WHERE r.nombre = 'USUARIO_PUBLICO';

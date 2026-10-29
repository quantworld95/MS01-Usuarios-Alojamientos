INSERT INTO modulo (nombre, descripcion, orden)
VALUES ('ADMINISTRACION', 'Funciones de la plataforma', 4);

INSERT INTO permiso (id_modulo, codigo, nombre, descripcion, asignable)
SELECT m.id_modulo, 'MODERAR_ALOJAMIENTOS', 'Moderar alojamientos', 'Aceptar, rechazar y suspender alojamientos', FALSE
FROM modulo m
WHERE m.nombre = 'ADMINISTRACION';

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
JOIN permiso p ON p.codigo = 'MODERAR_ALOJAMIENTOS'
WHERE r.nombre = 'SUPERADMIN';

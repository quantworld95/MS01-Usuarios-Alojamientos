ALTER TABLE rol
    ADD COLUMN descripcion VARCHAR(500);

CREATE UNIQUE INDEX uq_rol_nombre ON rol (lower(nombre));
CREATE UNIQUE INDEX uq_permiso_codigo ON permiso (codigo);
CREATE UNIQUE INDEX uq_modulo_nombre ON modulo (lower(nombre));

UPDATE rol
SET descripcion = 'Rol fijo de las cuentas públicas'
WHERE nombre = 'USUARIO_PUBLICO';

INSERT INTO modulo (nombre, descripcion, orden)
VALUES ('SEGURIDAD', 'Roles, trabajadores y usuarios públicos', 1);

INSERT INTO permiso (id_modulo, codigo, nombre, descripcion, asignable)
SELECT m.id_modulo, v.codigo, v.nombre, v.descripcion, TRUE
FROM modulo m
JOIN (VALUES
    ('ADMINISTRAR_ROLES', 'Administrar roles', 'Crear y modificar roles y sus permisos'),
    ('ADMINISTRAR_TRABAJADORES', 'Administrar trabajadores', 'Incorporar y gestionar cuentas corporativas'),
    ('ADMINISTRAR_USUARIOS_PUBLICOS', 'Administrar usuarios públicos', 'Consultar cuentas públicas y sus restricciones')
) AS v(codigo, nombre, descripcion) ON TRUE
WHERE m.nombre = 'SEGURIDAD';

INSERT INTO rol (nombre, descripcion, ambito, es_sistema, activo)
VALUES ('SUPERADMIN', 'Administración total de la plataforma', 'CORPORATIVO', TRUE, TRUE);

INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
JOIN permiso p ON p.codigo IN (
    'ADMINISTRAR_ROLES',
    'ADMINISTRAR_TRABAJADORES',
    'ADMINISTRAR_USUARIOS_PUBLICOS')
WHERE r.nombre = 'SUPERADMIN';

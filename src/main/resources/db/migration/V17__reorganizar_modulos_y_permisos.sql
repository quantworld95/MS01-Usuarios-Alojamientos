-- Catálogo MVP: IDENTIDAD + ALOJAMIENTOS (sin permisos de reservas/pagos).

UPDATE modulo
SET nombre = 'IDENTIDAD',
    descripcion = 'Gestión de identidad y acceso: roles, trabajadores y usuarios públicos',
    orden = 1
WHERE nombre = 'SEGURIDAD';

INSERT INTO modulo (nombre, descripcion, orden)
SELECT 'ALOJAMIENTOS',
       'Publicación, consulta y moderación de alojamientos de la plataforma',
       2
WHERE NOT EXISTS (SELECT 1 FROM modulo WHERE nombre = 'ALOJAMIENTOS');

UPDATE permiso p
SET id_modulo = m.id_modulo,
    nombre = 'Buscar alojamientos',
    descripcion = 'Consultar el catálogo de alojamientos publicados'
FROM modulo m
WHERE p.codigo = 'BUSCAR_ALOJAMIENTOS'
  AND m.nombre = 'ALOJAMIENTOS';

UPDATE permiso p
SET id_modulo = m.id_modulo,
    nombre = 'Publicar alojamiento',
    descripcion = 'Registrar, completar y operar alojamientos propios: listar, ver detalle, editar, enviar a revisión, activar o desactivar'
FROM modulo m
WHERE p.codigo = 'PUBLICAR_ALOJAMIENTO'
  AND m.nombre = 'ALOJAMIENTOS';

UPDATE permiso p
SET id_modulo = m.id_modulo,
    nombre = 'Moderar alojamientos',
    descripcion = 'Revisar alojamientos de la plataforma: listar, ver detalle y decidir (aceptar, rechazar, suspender o levantar suspensión)',
    asignable = TRUE
FROM modulo m
WHERE p.codigo = 'MODERAR_ALOJAMIENTOS'
  AND m.nombre = 'ALOJAMIENTOS';

UPDATE permiso p
SET nombre = 'Administrar roles',
    descripcion = 'Crear y modificar roles corporativos y asignarles permisos del catálogo'
WHERE p.codigo = 'ADMINISTRAR_ROLES';

UPDATE permiso p
SET nombre = 'Administrar trabajadores',
    descripcion = 'Incorporar y gestionar cuentas corporativas del personal de la plataforma'
WHERE p.codigo = 'ADMINISTRAR_TRABAJADORES';

UPDATE permiso p
SET nombre = 'Administrar usuarios públicos',
    descripcion = 'Consultar cuentas públicas y gestionar restricciones sobre sus permisos'
WHERE p.codigo = 'ADMINISTRAR_USUARIOS_PUBLICOS';

-- Permisos de MS-02 fuera del catálogo activo de MS-01.
DELETE FROM rol_permiso
WHERE id_permiso IN (
    SELECT id_permiso FROM permiso
    WHERE codigo IN (
        'RESERVAR_ALOJAMIENTO',
        'CALIFICAR_ESTANCIA',
        'GESTIONAR_RESERVAS_PROPIAS'));

DELETE FROM restriccion_usuario
WHERE id_permiso IN (
    SELECT id_permiso FROM permiso
    WHERE codigo IN (
        'RESERVAR_ALOJAMIENTO',
        'CALIFICAR_ESTANCIA',
        'GESTIONAR_RESERVAS_PROPIAS'));

DELETE FROM permiso
WHERE codigo IN (
    'RESERVAR_ALOJAMIENTO',
    'CALIFICAR_ESTANCIA',
    'GESTIONAR_RESERVAS_PROPIAS');

DELETE FROM modulo m
WHERE m.nombre IN ('HUESPED', 'ANFITRION', 'ADMINISTRACION')
  AND NOT EXISTS (SELECT 1 FROM permiso p WHERE p.id_modulo = m.id_modulo);

-- SUPERADMIN conserva moderación (por si se recreara el vínculo).
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
JOIN permiso p ON p.codigo = 'MODERAR_ALOJAMIENTOS'
WHERE r.nombre = 'SUPERADMIN'
  AND NOT EXISTS (
      SELECT 1
      FROM rol_permiso rp
      WHERE rp.id_rol = r.id_rol
        AND rp.id_permiso = p.id_permiso);

-- USUARIO_PUBLICO: solo capacidades de alojamientos del MVP.
INSERT INTO rol_permiso (id_rol, id_permiso)
SELECT r.id_rol, p.id_permiso
FROM rol r
JOIN permiso p ON p.codigo IN ('BUSCAR_ALOJAMIENTOS', 'PUBLICAR_ALOJAMIENTO')
WHERE r.nombre = 'USUARIO_PUBLICO'
  AND NOT EXISTS (
      SELECT 1
      FROM rol_permiso rp
      WHERE rp.id_rol = r.id_rol
        AND rp.id_permiso = p.id_permiso);

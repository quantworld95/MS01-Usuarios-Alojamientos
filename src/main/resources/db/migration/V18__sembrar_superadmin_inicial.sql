-- Cuenta corporativa inicial para administrar la plataforma.
-- Correo: superadmin@alojate.bo
-- Contraseña: secreta123
INSERT INTO usuario (
    id_usuario,
    id_rol,
    tipo_cuenta,
    nombre,
    apellido,
    email,
    telefono,
    password_hash,
    estado,
    fecha_registro
)
SELECT
    '00000000-0000-4000-8000-000000000001'::uuid,
    r.id_rol,
    'CORPORATIVA',
    'Super',
    'Admin',
    'superadmin@alojate.bo',
    '70000000',
    '$2a$10$isih8q4/FcafgBBbLcPyJOgkjwlqnXxFnvGLCfNFXTLFM/m1rmuNC',
    'ACTIVA',
    NOW()
FROM rol r
WHERE r.nombre = 'SUPERADMIN'
  AND NOT EXISTS (
      SELECT 1
      FROM usuario u
      WHERE lower(u.email) = lower('superadmin@alojate.bo')
        AND u.tipo_cuenta = 'CORPORATIVA'
  );

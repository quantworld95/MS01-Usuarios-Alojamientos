INSERT INTO servicio (nombre, categoria)
SELECT 'Recepción', 'Acceso'
WHERE NOT EXISTS (
    SELECT 1 FROM servicio WHERE lower(nombre) = lower('Recepción')
);

INSERT INTO servicio (nombre, categoria)
SELECT 'Restaurante', 'Comidas'
WHERE NOT EXISTS (
    SELECT 1 FROM servicio WHERE lower(nombre) = lower('Restaurante')
);

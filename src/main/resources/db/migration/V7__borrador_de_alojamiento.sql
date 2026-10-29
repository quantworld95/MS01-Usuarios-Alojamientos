ALTER TABLE alojamiento
    ALTER COLUMN titulo DROP NOT NULL,
    ALTER COLUMN descripcion DROP NOT NULL,
    ALTER COLUMN direccion DROP NOT NULL,
    ALTER COLUMN ciudad DROP NOT NULL,
    ALTER COLUMN departamento DROP NOT NULL,
    ALTER COLUMN latitud DROP NOT NULL,
    ALTER COLUMN longitud DROP NOT NULL;

INSERT INTO tipo_propiedad (nombre) VALUES
    ('Casa'),
    ('Departamento'),
    ('Habitación'),
    ('Hotel'),
    ('Cabaña'),
    ('Hostal');

INSERT INTO tipo_espacio (nombre) VALUES
    ('Alojamiento completo'),
    ('Habitación privada'),
    ('Habitación compartida');

INSERT INTO servicio (nombre, categoria) VALUES
    ('Wifi', 'Conectividad'),
    ('Estacionamiento', 'Acceso'),
    ('Cocina', 'Comodidades'),
    ('Piscina', 'Comodidades'),
    ('Desayuno', 'Comidas'),
    ('Aire acondicionado', 'Comodidades'),
    ('Admite mascotas', 'Normas'),
    ('Lavandería', 'Comodidades');

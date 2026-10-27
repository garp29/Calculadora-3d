-- ============================================================
-- seed.sql  |  Datos semilla de la calculadora
-- Ejecutar DESPUÉS de create_schema.sql
-- ============================================================

PRAGMA foreign_keys = ON;

-- ------------------------------------------------------------
-- Categorías
-- ------------------------------------------------------------
INSERT INTO categoria (nombre, descripcion) VALUES
    ('Básica',    'Operaciones aritméticas fundamentales'),
    ('Potencias', 'Potencias, raíces y exponentes'),
    ('Avanzada',  'Módulo, porcentaje, factorial y valor absoluto');

-- ------------------------------------------------------------
-- Catálogo inicial: las 10 operaciones
-- ------------------------------------------------------------
INSERT INTO operacion (nombre, simbolo, descripcion, aridad, id_categoria) VALUES
    ('Suma',            '+',    'Suma dos números',                        2, (SELECT id_categoria FROM categoria WHERE nombre = 'Básica')),
    ('Resta',           '-',    'Resta el segundo número al primero',      2, (SELECT id_categoria FROM categoria WHERE nombre = 'Básica')),
    ('Multiplicación',  '*',    'Multiplica dos números',                  2, (SELECT id_categoria FROM categoria WHERE nombre = 'Básica')),
    ('División',        '/',    'Divide el primer número entre el segundo',2, (SELECT id_categoria FROM categoria WHERE nombre = 'Básica')),
    ('Potencia',        '^',    'Eleva la base al exponente',              2, (SELECT id_categoria FROM categoria WHERE nombre = 'Potencias')),
    ('Raíz cuadrada',   'sqrt', 'Raíz cuadrada de un número',              1, (SELECT id_categoria FROM categoria WHERE nombre = 'Potencias')),
    ('Módulo',          '%',    'Residuo de la división entera',           2, (SELECT id_categoria FROM categoria WHERE nombre = 'Avanzada')),
    ('Porcentaje',      'pct',  'Calcula el porcentaje B de A',            2, (SELECT id_categoria FROM categoria WHERE nombre = 'Avanzada')),
    ('Factorial',       '!',    'Factorial de un entero no negativo',      1, (SELECT id_categoria FROM categoria WHERE nombre = 'Avanzada')),
    ('Valor absoluto',  'abs',  'Valor absoluto de un número',             1, (SELECT id_categoria FROM categoria WHERE nombre = 'Avanzada'));

-- ------------------------------------------------------------
-- Historial de ejemplo (para probar las consultas con datos)
-- ------------------------------------------------------------
INSERT INTO historial (id_operacion, operando_a, operando_b, resultado, error, fecha) VALUES
    ((SELECT id_operacion FROM operacion WHERE simbolo = '+'),    5,   3,    8,    NULL, '2026-10-20 09:00:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = '+'),    12,  8,    20,    NULL, '2026-10-20 09:05:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = '+'),    100, 250, 350,    NULL, '2026-10-21 10:15:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = '-'),    10,  4,    6,     NULL, '2026-10-21 10:20:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = '-'),    3,   9,   -6,     NULL, '2026-10-21 11:00:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = '*'),    7,   6,    42,    NULL, '2026-10-22 14:00:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = '*'),    2.5, 4,    10,    NULL, '2026-10-22 14:10:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = '/'),    20,  4,    5,     NULL, '2026-10-23 08:30:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = '/'),    9,   0,    NULL, 'División por cero', '2026-10-23 08:35:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = '^'),    2,   10, 1024,    NULL, '2026-10-24 16:00:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = 'sqrt'), 144, NULL, 12,    NULL, '2026-10-24 16:10:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = 'sqrt'), -4,  NULL, NULL, 'Raíz de número negativo', '2026-10-24 16:12:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = '%'),    17,  5,     2,    NULL, '2026-10-25 12:00:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = 'pct'),  200, 15,   30,    NULL, '2026-10-26 09:45:00'),
    ((SELECT id_operacion FROM operacion WHERE simbolo = '!'),    5,   NULL, 120,   NULL, '2026-10-26 17:00:00');
-- Nota: 'abs' queda sin uso a propósito, para probar el LEFT JOIN.

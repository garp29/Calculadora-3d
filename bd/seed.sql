-- =========================================================================
-- seed.sql - Datos semilla para poblar el catálogo de operaciones (Clase 2)
-- =========================================================================

PRAGMA foreign_keys = ON;

-- Limpiar datos previos si se reejecuta
DELETE FROM operacion;

-- Operaciones Básicas (Implementadas en Java con POO)
INSERT INTO operacion (id, simbolo, nombre, tipo, lenguaje, aridad) VALUES 
(1, '+', 'Suma', 'BASICA', 'JAVA', 2),
(2, '-', 'Resta', 'BASICA', 'JAVA', 2),
(3, '*', 'Multiplicación', 'BASICA', 'JAVA', 2),
(4, '/', 'División', 'BASICA', 'JAVA', 2);

-- Operaciones Trigonométricas (Calculadas mediante trig.py en Python)
INSERT INTO operacion (id, simbolo, nombre, tipo, lenguaje, aridad) VALUES 
(5, 'sin', 'Seno', 'TRIGONOMETRICA', 'PYTHON', 1),
(6, 'cos', 'Coseno', 'TRIGONOMETRICA', 'PYTHON', 1),
(7, 'tan', 'Tangente', 'TRIGONOMETRICA', 'PYTHON', 1),
(8, 'asin', 'Arco Seno', 'TRIGONOMETRICA', 'PYTHON', 1),
(9, 'acos', 'Arco Coseno', 'TRIGONOMETRICA', 'PYTHON', 1),
(10, 'atan', 'Arco Tangente', 'TRIGONOMETRICA', 'PYTHON', 1);

-- Mensaje de confirmación
SELECT 'Seed aplicado correctamente: 10 operaciones registradas en el catálogo.' AS estado;

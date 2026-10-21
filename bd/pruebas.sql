-- =========================================================================
-- pruebas.sql - 5 Pruebas de validación e integridad de la Base de Datos
-- Requisito R8: La base rechaza datos incoherentes (tipos, llaves, restricciones)
-- =========================================================================

PRAGMA foreign_keys = ON;

-- -------------------------------------------------------------------------
-- Prueba 1: Rechazar tipo de operación inválido (Violación de CHECK en tipo)
-- -------------------------------------------------------------------------
SELECT '--- PRUEBA 1: Tipo de operación inválido (Debe fallar) ---' AS prueba;
INSERT INTO operacion (simbolo, nombre, tipo, lenguaje, aridad) 
VALUES ('log', 'Logaritmo', 'CIENTIFICA', 'JAVA', 1);

-- -------------------------------------------------------------------------
-- Prueba 2: Rechazar cálculo asociado a una operación inexistente (Llave Foránea)
-- -------------------------------------------------------------------------
SELECT '--- PRUEBA 2: Cálculo con operacion_id que no existe en el catálogo (Debe fallar) ---' AS prueba;
INSERT INTO calculo (operacion_id, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error)
VALUES (99, 10.0, 5.0, 'grados', NULL, 0, 'Operacion no encontrada');

-- -------------------------------------------------------------------------
-- Prueba 3: Rechazar éxito incoherente (Cálculo exitoso con resultado NULL)
-- Nota: Aunque SQLite permite NULL a menos que haya un CHECK, simulamos lógica de negocio
-- o validamos el CHECK de exito in (0, 1). 
-- Probamos aquí un modo de ángulo no permitido.
-- -------------------------------------------------------------------------
SELECT '--- PRUEBA 3: Modo de ángulo inválido en cálculo (Debe fallar) ---' AS prueba;
INSERT INTO calculo (operacion_id, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error)
VALUES (1, 5.0, 3.0, 'grados_centesimales', 8.0, 1, NULL);

-- -------------------------------------------------------------------------
-- Prueba 4: Rechazar paso menor o igual a cero en la definición de una serie (CHECK paso > 0)
-- -------------------------------------------------------------------------
SELECT '--- PRUEBA 4: Serie con paso negativo o cero (Debe fallar) ---' AS prueba;
INSERT INTO serie (nombre, operacion_id, x_min, x_max, paso, modo_angulo)
VALUES ('Serie Invalida', 5, -180.0, 180.0, 0.0, 'grados');

-- -------------------------------------------------------------------------
-- Prueba 5: Demostración de integridad referencial al intentar eliminar una operación en uso
-- -------------------------------------------------------------------------
SELECT '--- PRUEBA 5: Eliminar operación con historial asociado usando ON DELETE RESTRICT (Debe fallar) ---' AS prueba;
-- Insertamos primero una operación temporal y un cálculo asociado
INSERT INTO operacion (id, simbolo, nombre, tipo, lenguaje, aridad) VALUES (999, 'test', 'Test', 'BASICA', 'JAVA', 1);
INSERT INTO calculo (operacion_id, operando_a, exito, resultado) VALUES (999, 2.0, 1, 4.0);
-- Intentar borrar la operación 999 debe ser bloqueada por la llave foránea restrictiva
DELETE FROM operacion WHERE id = 999;
-- Limpieza del registro de prueba auxiliar si la restricción funcionó
DELETE FROM calculo WHERE operacion_id = 999;
DELETE FROM operacion WHERE id = 999;

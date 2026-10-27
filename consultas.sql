-- ============================================================
-- consultas.sql  |  SELECT, JOIN y GROUP BY de la calculadora
-- Ejecutar DESPUÉS de create_schema.sql y seed.sql
-- ============================================================

.headers on
.mode column

-- ------------------------------------------------------------
-- 1) SELECT básico: catálogo completo de operaciones
-- ------------------------------------------------------------
SELECT id_operacion, nombre, simbolo, aridad
FROM operacion
ORDER BY id_operacion;

-- ------------------------------------------------------------
-- 2) SELECT con filtro: solo operaciones unarias (1 operando)
-- ------------------------------------------------------------
SELECT nombre, simbolo
FROM operacion
WHERE aridad = 1;

-- ------------------------------------------------------------
-- 3) INNER JOIN: operaciones con el nombre de su categoría
-- ------------------------------------------------------------
SELECT o.nombre   AS operacion,
       o.simbolo,
       c.nombre   AS categoria
FROM operacion o
INNER JOIN categoria c ON c.id_categoria = o.id_categoria
ORDER BY c.nombre, o.nombre;

-- ------------------------------------------------------------
-- 4) JOIN: historial legible (qué se calculó y cuándo)
-- ------------------------------------------------------------
SELECT h.id_historial,
       o.nombre    AS operacion,
       h.operando_a,
       h.operando_b,
       h.resultado,
       COALESCE(h.error, 'OK') AS estado,
       h.fecha
FROM historial h
JOIN operacion o ON o.id_operacion = h.id_operacion
ORDER BY h.fecha DESC;

-- ------------------------------------------------------------
-- 5) GROUP BY: cuántas veces se usó cada operación
-- ------------------------------------------------------------
SELECT o.nombre AS operacion,
       COUNT(h.id_historial) AS veces_usada
FROM operacion o
LEFT JOIN historial h ON h.id_operacion = o.id_operacion
GROUP BY o.id_operacion, o.nombre
ORDER BY veces_usada DESC, o.nombre;

-- ------------------------------------------------------------
-- 6) GROUP BY con 2 tablas: cálculos por categoría
-- ------------------------------------------------------------
SELECT c.nombre AS categoria,
       COUNT(h.id_historial) AS total_calculos
FROM categoria c
JOIN operacion o        ON o.id_categoria = c.id_categoria
LEFT JOIN historial h   ON h.id_operacion = o.id_operacion
GROUP BY c.id_categoria, c.nombre
ORDER BY total_calculos DESC;

-- ------------------------------------------------------------
-- 7) GROUP BY + HAVING: operaciones usadas 2 o más veces
-- ------------------------------------------------------------
SELECT o.nombre AS operacion,
       COUNT(*) AS veces_usada
FROM historial h
JOIN operacion o ON o.id_operacion = h.id_operacion
GROUP BY o.id_operacion, o.nombre
HAVING COUNT(*) >= 2
ORDER BY veces_usada DESC;

-- ------------------------------------------------------------
-- 8) Funciones de agregación: resultado promedio/mín/máx por operación
-- ------------------------------------------------------------
SELECT o.nombre AS operacion,
       ROUND(AVG(h.resultado), 2) AS promedio,
       MIN(h.resultado)           AS minimo,
       MAX(h.resultado)           AS maximo
FROM historial h
JOIN operacion o ON o.id_operacion = h.id_operacion
WHERE h.error IS NULL
GROUP BY o.id_operacion, o.nombre
ORDER BY o.nombre;

-- ------------------------------------------------------------
-- 9) Operaciones que NUNCA se han usado (LEFT JOIN + IS NULL)
-- ------------------------------------------------------------
SELECT o.nombre AS operacion_sin_uso
FROM operacion o
LEFT JOIN historial h ON h.id_operacion = o.id_operacion
WHERE h.id_historial IS NULL;

-- ------------------------------------------------------------
-- 10) Cálculos que terminaron en error, por tipo de error
-- ------------------------------------------------------------
SELECT h.error,
       COUNT(*) AS cantidad
FROM historial h
WHERE h.error IS NOT NULL
GROUP BY h.error
ORDER BY cantidad DESC;

-- ------------------------------------------------------------
-- 11) Últimos 5 cálculos realizados
-- ------------------------------------------------------------
SELECT o.simbolo, h.operando_a, h.operando_b, h.resultado, h.fecha
FROM historial h
JOIN operacion o ON o.id_operacion = h.id_operacion
ORDER BY h.fecha DESC
LIMIT 5;

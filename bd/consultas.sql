-- =========================================================================
-- consultas.sql - Consultas analíticas del historial y operaciones (Clase 2)
-- =========================================================================

PRAGMA foreign_keys = ON;

-- 1. Ver el historial completo de cálculos realizados uniendo la operación
SELECT 
    c.id, 
    o.simbolo AS operacion, 
    c.operando_a, 
    c.operando_b, 
    c.modo_angulo, 
    c.resultado, 
    CASE WHEN c.exito = 1 THEN 'EXITOSO' ELSE 'FALLIDO' END AS estado,
    c.mensaje_error,
    c.creado_en
FROM calculo c
JOIN operacion o ON c.operacion_id = o.id
ORDER BY c.creado_en DESC;

-- 2. Consultar únicamente los cálculos que fallaron (división por cero, tan 90°, etc.)
SELECT 
    c.id, 
    o.simbolo, 
    c.operando_a, 
    c.operando_b, 
    c.mensaje_error, 
    c.creado_en
FROM calculo c
JOIN operacion o ON c.operacion_id = o.id
WHERE c.exito = 0
ORDER BY c.creado_en DESC;

-- 3. Agrupamiento (GROUP BY): Estadísticas de uso por cada operación del catálogo
SELECT 
    o.simbolo, 
    o.nombre, 
    o.tipo, 
    COUNT(c.id) AS total_usos,
    SUM(CASE WHEN c.exito = 1 THEN 1 ELSE 0 END) AS total_exitos,
    SUM(CASE WHEN c.exito = 0 THEN 1 ELSE 0 END) AS total_fallos
FROM operacion o
LEFT JOIN calculo c ON o.id = c.operacion_id
GROUP BY o.id, o.simbolo, o.nombre, o.tipo
ORDER BY total_usos DESC;

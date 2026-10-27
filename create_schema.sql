-- ============================================================
-- create_schema.sql  |  Calculadora con base de datos (SQLite)
-- Crea las tablas necesarias. Se puede ejecutar varias veces.
-- ============================================================

PRAGMA foreign_keys = ON;

-- Se borra en orden inverso a las dependencias para poder re-ejecutar
DROP TABLE IF EXISTS historial;
DROP TABLE IF EXISTS operacion;
DROP TABLE IF EXISTS categoria;

-- ------------------------------------------------------------
-- Categoría de operaciones (Básica, Avanzada, ...)
-- ------------------------------------------------------------
CREATE TABLE categoria (
    id_categoria  INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre        TEXT NOT NULL UNIQUE,
    descripcion   TEXT
);

-- ------------------------------------------------------------
-- Catálogo de operaciones que soporta la calculadora
-- aridad: cuántos operandos necesita (1 = unaria, 2 = binaria)
-- ------------------------------------------------------------
CREATE TABLE operacion (
    id_operacion  INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre        TEXT NOT NULL UNIQUE,
    simbolo       TEXT NOT NULL UNIQUE,
    descripcion   TEXT,
    aridad        INTEGER NOT NULL DEFAULT 2 CHECK (aridad IN (1, 2)),
    id_categoria  INTEGER NOT NULL,
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- ------------------------------------------------------------
-- Historial de cálculos realizados
-- operando_b es NULL en operaciones unarias (raíz, factorial...)
-- ------------------------------------------------------------
CREATE TABLE historial (
    id_historial  INTEGER PRIMARY KEY AUTOINCREMENT,
    id_operacion  INTEGER NOT NULL,
    operando_a    REAL NOT NULL,
    operando_b    REAL,
    resultado     REAL,
    error         TEXT,                              -- ej. 'División por cero'
    fecha         TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (id_operacion) REFERENCES operacion(id_operacion)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- Índices para acelerar los JOIN y las búsquedas por fecha
CREATE INDEX idx_historial_operacion ON historial(id_operacion);
CREATE INDEX idx_historial_fecha     ON historial(fecha);

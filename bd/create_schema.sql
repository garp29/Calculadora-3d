-- =========================================================================
-- Esquema DDL: Base de Datos de la Calculadora Integrada (SQLite)
-- Cumple con Normalización hasta 3FN y restricciones de integridad (R8)
-- =========================================================================

-- Habilitar llaves foráneas estrictas en SQLite
PRAGMA foreign_keys = ON;

-- 1. Tabla de Catálogo de Operaciones (Básicas y Trigonométricas)
CREATE TABLE IF NOT EXISTS operacion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    simbolo TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    tipo TEXT NOT NULL CHECK (tipo IN ('BASICA', 'TRIGONOMETRICA')),
    lenguaje TEXT NOT NULL CHECK (lenguaje IN ('JAVA', 'PYTHON')),
    aridad INTEGER NOT NULL CHECK (aridad IN (1, 2))
);

-- 2. Tabla de Historial de Cálculos (Guarda éxitos y fallos - Requisitos R2, R3)
CREATE TABLE IF NOT EXISTS calculo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    operacion_id INTEGER NOT NULL,
    operando_a REAL NOT NULL,
    operando_b REAL, -- Puede ser NULL si la aridad es 1 (ej. funciones unarias)
    modo_angulo TEXT CHECK (modo_angulo IN ('grados', 'radianes') OR modo_angulo IS NULL),
    resultado REAL, -- NULL si el cálculo falló
    exito INTEGER NOT NULL CHECK (exito IN (0, 1)),
    mensaje_error TEXT,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (operacion_id) REFERENCES operacion(id) ON DELETE RESTRICT
);

-- 3. Tabla de Metadatos de Series (Para graficación - Requisito R5)
CREATE TABLE IF NOT EXISTS serie (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    operacion_id INTEGER NOT NULL,
    x_min REAL NOT NULL,
    x_max REAL NOT NULL,
    paso REAL NOT NULL CHECK (paso > 0),
    modo_angulo TEXT NOT NULL CHECK (modo_angulo IN ('grados', 'radianes')),
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (operacion_id) REFERENCES operacion(id) ON DELETE RESTRICT
);

-- 4. Tabla de Puntos de una Serie (Soporta puntos sin valor/asíntotas con y NULL - Requisito R6)
CREATE TABLE IF NOT EXISTS punto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    serie_id INTEGER NOT NULL,
    x REAL NOT NULL,
    y REAL, -- NULL permitido para representar asíntotas o discontinuidades (ej. tan 90°)
    FOREIGN KEY (serie_id) REFERENCES serie(id) ON DELETE CASCADE
);

-- Índices de rendimiento recomendados para las consultas del historial
CREATE INDEX IF NOT EXISTS idx_calculo_operacion ON calculo(operacion_id);
CREATE INDEX IF NOT EXISTS idx_calculo_exito ON calculo(exito);
CREATE INDEX IF NOT EXISTS idx_punto_serie ON punto(serie_id);

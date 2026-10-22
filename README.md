# Proyecto Integrado: Calculadora Científica y Gráfica
### Java + Python + SQLite

Este proyecto integra **Bases de Datos** y **Programación Orientada a Objetos 2 (POO2)** mediante una arquitectura modular donde tres tecnologías se comunican por contratos técnicos formales:
- **Java (POO2):** Interfaz común (`IOperacion`), herencia, polimorfismo, manejo de excepciones y persistencia JDBC mediante el patrón DAO (`CalculoDAO`). Orquesta el visor gráfico en tiempo real (`VisorGrafica`) con pestañas 2D y 3D.
- **Python:** Motor trigonométrico (`trig.py`), generador de gráficas de curvas 2D (`graficar.py`) y motor de renderizado de superficies tridimensionales $z = f(x, y)$ con `mplot3d` (`graficar_3d.py`), invocados mediante `ProcessBuilder`.
- **SQLite (3FN):** Catálogo de operaciones, historial completo de cálculos (éxitos y fallos) y persistencia transaccional de series y puntos para graficación.

---

## 🏛️ Arquitectura del Sistema

```mermaid
graph TD
    A[Usuario / Consola / GUI Swing] --> B[Java - Main / VisorGrafica]
    B -->|Operaciones Básicas| C[Java POO - OperacionBasica / Division]
    B -->|ProcessBuilder Trig| D[Python - trig.py]
    B -->|ProcessBuilder 3D Surface| G[Python - graficar_3d.py]
    B -->|JDBC DAO| E[(SQLite - calculadora.db)]
    E -->|Lectura de puntos 2D| F[Python - graficar.py]
    F -->|PNG 2D generado| B
    G -->|PNG 3D generado| B
```

---

## 📁 Estructura del Repositorio

```text
├── bd/
│   ├── calculadora.db          # Base de datos SQLite activa
│   ├── create_schema.sql       # DDL normalizado en 3FN con restricciones CHECK y FK
│   ├── seed.sql                # Catálogo de 10 operaciones (Básicas y Trigonométricas)
│   ├── pruebas.sql             # 5 pruebas de integridad referencial y de dominio
│   ├── consultas.sql           # Consultas analíticas, historial y GROUP BY
│   └── sql.py                  # CLI auxiliar en Python para ejecutar SQL en Windows
├── python/
│   ├── trig.py                 # Motor de funciones trigonométricas y series (sin, cos, tan, asin, acos, atan)
│   ├── graficar.py             # Generador de gráficas 2D PNG leyendo puntos desde SQLite
│   └── graficar_3d.py          # Motor de renderizado de superficies 3D (mplot3d, NumPy)
├── lib/
│   ├── sqlite-jdbc-3.45.3.0.jar # Driver JDBC oficial de SQLite
│   ├── slf4j-api-2.0.13.jar    # API de logging
│   └── slf4j-simple-2.0.13.jar # Implementación de logging
├── src/
│   └── calculadora/
│       ├── Main.java           # Menú interactivo de consola y orquestador (opciones 2D y 3D)
│       ├── dao/
│       │   └── CalculoDAO.java # Persistencia JDBC y transacciones de series
│       ├── modelo/
│       │   ├── IOperacion.java
│       │   ├── OperacionBasica.java
│       │   ├── Division.java
│       │   ├── Suma.java
│       │   ├── Resta.java
│       │   ├── Multiplicacion.java
│       │   ├── OperacionTrigonometrica.java
│       │   └── Graficador3D.java # Orquestador Java para el motor 3D
│       └── vista/
│           └── VisorGrafica.java # Ventana interactiva con pestañas 2D y 3D (Swing)
├── compilar.bat                # Script para compilar las clases Java
├── ejecutar_calculadora.bat    # Script para ejecutar la calculadora interactiva
├── pruebas.bat                 # Suite de automatización de base de datos
├── pruebas_python.bat          # Suite de verificación de contratos Python
├── subir_a_github.bat          # Automatización de subida a GitHub
└── README.md
```

---

## 🚀 Requisitos y Configuración

1. **Java:** JDK 17 o superior.
2. **Python:** Python 3.10 o superior (disponible con `py` o `python`).
3. **Librerías Python:**
   ```powershell
   py -m pip install matplotlib
   ```

---

## 🛠️ Compilación y Ejecución

* **Compilar:**
  ```powershell
  .\compilar.bat
  ```

* **Ejecutar la Calculadora con Visor Gráfico en Tiempo Real:**
  ```powershell
  .\ejecutar_calculadora.bat
  ```

* **Ejecutar Pruebas Automatizadas de la Base de Datos:**
  ```powershell
  .\pruebas.bat
  ```

* **Ejecutar Pruebas de Contratos de Python:**
  ```powershell
  .\pruebas_python.bat
  ```

---

## 📋 Contratos Técnicos Implementados

1. **Cálculo Único:** `py python\trig.py <fn> <valor> [--modo grados|radianes]`
   - Salida: `OK;<resultado>` o `ERROR;<mensaje>`
2. **Generación de Series:** `py python\trig.py serie <fn> <xmin> <xmax> <paso> [--modo grados|radianes]`
   - Salida: líneas `x;y` (o `x;` en indeterminaciones como $\tan(90^\circ)$).
3. **Renderizado de Gráficas 2D:** `py python\graficar.py <ruta_db> <serie_id> <salida.png> [--punto <x> <y>]`
   - Lee las coordenadas desde la base de datos y genera el PNG resaltando el punto evaluado.
4. **Renderizado de Superficies 3D:** `py python\graficar_3d.py <fn> <xmin> <xmax> <ymin> <ymax> <paso> <salida.png> [--cmap <color>]`
   - Calcula mallas bidimensionales mediante NumPy y renderiza superficies $z = f(x, y)$ con curvas de nivel proyectadas y barras de color en Matplotlib `mplot3d`.
   - Soporta superficies analíticas: `ondas` ($z = \sin(x)\cos(y)$), `sombrero` ($z = \sin(r)/r$), `paraboloide` ($z = x^2+y^2$), `silla` ($z = x^2-y^2$), `ripple` ($z = \sin(10(x^2+y^2))/10$) y `gaussiana` ($z = \exp(-(x^2+y^2))$).


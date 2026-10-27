# Proyecto Integrado: Calculadora Científica y Gráfica
### Java + Python + SQLite

Este proyecto integra **Bases de Datos** y **Programación Orientada a Objetos 2 (POO2)** mediante una arquitectura modular donde tres tecnologías se comunican por contratos técnicos formales:
- **Java (POO2):** Interfaz común (`IOperacion`), herencia, polimorfismo, manejo de excepciones y persistencia JDBC mediante el patrón DAO (`CalculoDAO`). Orquesta el visor gráfico en tiempo real (`VisorGrafica`) con pestañas 2D y 3D.
- **Python:** Motor trigonométrico (`trig.py`), generador de gráficas de curvas 2D (`graficar.py`) y motor de renderizado de superficies tridimensionales $z = f(x, y)$ con `mplot3d` (`graficar_3d.py`), invocados mediante `ProcessBuilder`.
- **SQLite (3FN):** Catálogo de operaciones, historial completo de cálculos (éxitos y fallos) y persistencia transaccional de series y puntos para graficación.

---


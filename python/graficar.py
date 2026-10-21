#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Archivo: python/graficar.py
Propósito: Leer los puntos de una serie almacenada en SQLite y renderizar
           la gráfica en un archivo PNG usando matplotlib.
Contrato:
  python graficar.py <ruta_db> <serie_id> <salida.png>
"""

import sys
import os
import sqlite3

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

try:
    import matplotlib
    # Backend no interactivo 'Agg': evita que se abran ventanas que bloqueen a Java ProcessBuilder
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
except ImportError:
    print("ERROR;La libreria matplotlib no esta instalada. Ejecute: py -m pip install matplotlib")
    sys.exit(1)


def generar_grafica() -> None:
    if len(sys.argv) not in (4, 7):
        print("ERROR;Uso esperado: python graficar.py <ruta_db> <serie_id> <salida.png>")
        return

    ruta_db = os.path.abspath(sys.argv[1])
    serie_id_raw = sys.argv[2]
    ruta_salida = os.path.abspath(sys.argv[3])

    punto_destacado = None
    if len(sys.argv) == 7 and sys.argv[4] == "--punto":
        try:
            punto_destacado = (float(sys.argv[5]), float(sys.argv[6]))
        except ValueError:
            punto_destacado = None

    try:
        serie_id = int(serie_id_raw)
    except ValueError:
        print(f"ERROR;El identificador de serie '{serie_id_raw}' debe ser un numero entero")
        return

    if not os.path.isfile(ruta_db):
        print(f"ERROR;No se encontro el archivo de base de datos en: {ruta_db}")
        return

    try:
        with sqlite3.connect(ruta_db) as conexion:
            cursor = conexion.cursor()
            cursor.execute("PRAGMA foreign_keys = ON;")

            # 1. Consultar metadatos de la serie junto con el símbolo y nombre de la operación
            cursor.execute(
                """
                SELECT s.nombre, o.simbolo, o.nombre, s.x_min, s.x_max, s.paso, s.modo_angulo
                FROM serie s
                INNER JOIN operacion o ON s.operacion_id = o.id
                WHERE s.id = ?
                """,
                (serie_id,),
            )
            metadatos = cursor.fetchone()
            if metadatos is None:
                print(f"ERROR;No existe ninguna serie registrada con id={serie_id}")
                return

            nombre_serie, simbolo_op, nombre_op, x_min, x_max, paso, modo_angulo = metadatos

            # 2. Consultar la colección de puntos (x, y) ordenados ascendentemente por x
            cursor.execute(
                """
                SELECT x, y
                FROM punto
                WHERE serie_id = ?
                ORDER BY x ASC
                """,
                (serie_id,),
            )
            filas_puntos = cursor.fetchall()

            if not filas_puntos:
                print(f"ERROR;La serie id={serie_id} no contiene puntos asociados en la tabla 'punto'")
                return

            # 3. Mapear coordenadas convirtiendo NULL de SQLite en NaN (huecos en matplotlib)
            eje_x = []
            eje_y = []
            puntos_indefinidos = []

            for x_val, y_val in filas_puntos:
                x_num = float(x_val)
                eje_x.append(x_num)
                if y_val is None:
                    eje_y.append(float("nan"))
                    puntos_indefinidos.append(x_num)
                else:
                    eje_y.append(float(y_val))

            # 4. Configurar y trazar la figura
            fig, ax = plt.subplots(figsize=(9, 5.2), dpi=120)

            ax.plot(
                eje_x,
                eje_y,
                color="#1d4ed8",
                linewidth=1.8,
                marker="o",
                markersize=3.5,
                label=f"{simbolo_op}(x)",
            )

            # Si existen asíntotas o puntos sin valor, marcar líneas verticales punteadas suaves
            for x_indef in puntos_indefinidos:
                ax.axvline(x=x_indef, color="#ef4444", linestyle="--", linewidth=0.9, alpha=0.7)

            # Ejes cartesianos en 0
            ax.axhline(0, color="#475569", linewidth=0.8)
            ax.axvline(0, color="#475569", linewidth=0.8)

            # Si se pasó un punto específico evaluado en tiempo real, resaltarlo sobre la curva
            if punto_destacado is not None:
                px, py = punto_destacado
                ax.plot(
                    [px],
                    [py],
                    marker="o",
                    markersize=9,
                    color="#dc2626",
                    markeredgecolor="#ffffff",
                    markeredgewidth=1.5,
                    zorder=5,
                    label=f"Punto evaluado: ({px:g}, {py:g})",
                )
                ax.annotate(
                    f"({px:g}, {py:g})",
                    xy=(px, py),
                    xytext=(10, 10),
                    textcoords="offset points",
                    fontsize=9,
                    fontweight="bold",
                    color="#991b1b",
                    bbox=dict(boxstyle="round,pad=0.3", fc="#fef2f2", ec="#dc2626", alpha=0.9),
                    zorder=6,
                )

            unidad = modo_angulo if modo_angulo else "rad/grad"
            titulo_grafica = (
                nombre_serie
                if nombre_serie
                else f"Gráfica de {nombre_op} ({simbolo_op}) [{x_min} a {x_max}, paso {paso} - {unidad}]"
            )

            ax.set_title(titulo_grafica, fontsize=11, fontweight="bold", pad=12)
            ax.set_xlabel(f"x ({unidad})", fontsize=10)
            ax.set_ylabel(f"f(x) = {simbolo_op}(x)", fontsize=10)
            ax.grid(True, which="both", linestyle=":", alpha=0.6)
            ax.legend(loc="upper right")

            plt.tight_layout()

            # Crear carpeta contenedora si no existe (soporta rutas con espacios en Windows)
            carpeta_destino = os.path.dirname(ruta_salida)
            if carpeta_destino and not os.path.exists(carpeta_destino):
                os.makedirs(carpeta_destino, exist_ok=True)

            plt.savefig(ruta_salida, format="png")
            plt.close(fig)

            print(f"OK;{ruta_salida}")

    except sqlite3.Error as db_err:
        print(f"ERROR;Error de base de datos en graficar.py: {db_err}")
    except Exception as exc:
        print(f"ERROR;Fallo inesperado al generar la grafica: {exc}")


if __name__ == "__main__":
    generar_grafica()

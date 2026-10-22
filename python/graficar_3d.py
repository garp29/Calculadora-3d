#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Archivo: python/graficar_3d.py
Propósito: Renderizar superficies matemáticas tridimensionales z = f(x, y)
           usando matplotlib y numpy, invocado desde Java mediante ProcessBuilder.
Contrato CLI:
  py graficar_3d.py <funcion> <xmin> <xmax> <ymin> <ymax> <paso> <salida.png> [--cmap <color>]
"""

import sys
import os

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

try:
    import numpy as np
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    from matplotlib import cm
except ImportError as err:
    print(f"ERROR;Librería requerida no encontrada: {err}")
    sys.exit(1)

FUNCIONES_3D = {
    "ondas": ("Ondas Cruzadas: z = sin(x) · cos(y)", lambda X, Y: np.sin(X) * np.cos(Y)),
    "sombrero": ("Sombrero / Sinc 3D: z = sin(r)/r", lambda X, Y: np.sinc(np.sqrt(X**2 + Y**2) / np.pi)),
    "paraboloide": ("Paraboloide Elíptico: z = x² + y²", lambda X, Y: X**2 + Y**2),
    "silla": ("Silla de Montar: z = x² - y²", lambda X, Y: X**2 - Y**2),
    "ripple": ("Ondulaciones Circulares: z = cos(x) + sin(y)", lambda X, Y: np.cos(X) + np.sin(Y)),
    "gaussiana": ("Campana de Gauss 3D: z = exp(-(x²+y²))", lambda X, Y: np.exp(-(X**2 + Y**2))),
}


def calcular_superficie(nombre_fn: str, X: np.ndarray, Y: np.ndarray):
    fn_key = nombre_fn.lower().strip()
    if fn_key in FUNCIONES_3D:
        titulo, formula = FUNCIONES_3D[fn_key]
        Z = formula(X, Y)
        return titulo, Z
    else:
        # Intento de evaluar expresión segura con numpy
        # Ej: "sin(x) + cos(y)"
        try:
            expresion = fn_key.replace("x", "X").replace("y", "Y")
            contexto = {
                "X": X, "Y": Y, "np": np,
                "sin": np.sin, "cos": np.cos, "tan": np.tan,
                "exp": np.exp, "sqrt": np.sqrt, "pi": np.pi
            }
            Z = eval(expresion, {"__builtins__": {}}, contexto)
            return f"Superficie 3D: z = {nombre_fn}", Z
        except Exception as exc:
            raise ValueError(f"Función 3D no soportada '{nombre_fn}'. Opciones: {list(FUNCIONES_3D.keys())}")


def generar_grafica_3d() -> None:
    if len(sys.argv) < 8:
        print("ERROR;Uso esperado: py graficar_3d.py <funcion> <xmin> <xmax> <ymin> <ymax> <paso> <salida.png> [--cmap <color>]")
        return

    nombre_fn = sys.argv[1]
    try:
        xmin = float(sys.argv[2])
        xmax = float(sys.argv[3])
        ymin = float(sys.argv[4])
        ymax = float(sys.argv[5])
        paso = float(sys.argv[6])
    except ValueError:
        print("ERROR;Los parámetros numéricos (xmin, xmax, ymin, ymax, paso) deben ser números válidos.")
        return

    ruta_salida = os.path.abspath(sys.argv[7])

    if xmin >= xmax or ymin >= ymax:
        print("ERROR;Rango inválido: xmin debe ser menor a xmax y ymin debe ser menor a ymax.")
        return

    if paso <= 0:
        print("ERROR;El paso debe ser estrictamente mayor que cero.")
        return

    # Limitar número máximo de puntos para evitar consumo excesivo de memoria
    puntos_x = int((xmax - xmin) / paso) + 1
    puntos_y = int((ymax - ymin) / paso) + 1
    if puntos_x * puntos_y > 40000:
        paso = max((xmax - xmin), (ymax - ymin)) / 150.0

    # Mapa de color opcional
    mapa_color = "coolwarm"
    if "--cmap" in sys.argv:
        idx = sys.argv.index("--cmap")
        if idx + 1 < len(sys.argv):
            mapa_color = sys.argv[idx + 1]

    try:
        x = np.arange(xmin, xmax + (paso * 0.5), paso)
        y = np.arange(ymin, ymax + (paso * 0.5), paso)
        X, Y = np.meshgrid(x, y)

        titulo, Z = calcular_superficie(nombre_fn, X, Y)

        # Configurar figura 3D de alta definición
        fig = plt.figure(figsize=(10, 6.5), dpi=120)
        ax = fig.add_subplot(111, projection="3d")

        surf = ax.plot_surface(
            X, Y, Z,
            cmap=mapa_color,
            edgecolor="none",
            alpha=0.92,
            antialiased=True,
            rcount=80,
            ccount=80
        )

        # Curvas de nivel proyectadas en la base para mayor profundidad estética
        offset_z = np.nanmin(Z) - (np.nanmax(Z) - np.nanmin(Z)) * 0.1
        ax.contour(X, Y, Z, zdir='z', offset=offset_z, cmap=mapa_color, alpha=0.5, linestyles="dashed")
        ax.set_zlim(offset_z, np.nanmax(Z))

        ax.set_title(titulo, fontsize=12, fontweight="bold", pad=14)
        ax.set_xlabel("Eje X", fontsize=10, labelpad=8)
        ax.set_ylabel("Eje Y", fontsize=10, labelpad=8)
        ax.set_zlabel("Eje Z = f(x, y)", fontsize=10, labelpad=8)

        # Ajuste de vista inicial
        ax.view_init(elev=28, azim=-55)

        # Barra de color con gradiente
        cbar = fig.colorbar(surf, ax=ax, shrink=0.55, aspect=12, pad=0.08)
        cbar.set_label("Magnitud Z", fontsize=9)

        plt.tight_layout()

        carpeta_salida = os.path.dirname(ruta_salida)
        if carpeta_salida and not os.path.exists(carpeta_salida):
            os.makedirs(carpeta_salida, exist_ok=True)

        plt.savefig(ruta_salida, format="png", bbox_inches="tight")
        plt.close(fig)

        print(f"OK;{ruta_salida}")

    except Exception as exc:
        print(f"ERROR;Fallo al renderizar superficie 3D: {exc}")


if __name__ == "__main__":
    generar_grafica_3d()

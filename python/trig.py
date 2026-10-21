#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Archivo: python/trig.py
Propósito: Motor de cálculo trigonométrico y generador de series para ser
           invocado por Java mediante ProcessBuilder.
Contratos:
  1) Cálculo único: python trig.py <fn> <valor> [--modo grados|radianes]
     Salida stdout: OK;<resultado>  o  ERROR;<mensaje>
  2) Serie:         python trig.py serie <fn> <xmin> <xmax> <paso> [--modo grados|radianes]
     Salida stdout: líneas con formato x;y (o x; cuando la función se indefine)
"""

import sys
import math

# Garantizar salida en UTF-8 en consolas de Windows
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

# Constante de tolerancia para eliminar el ruido de punto flotante (IEEE 754)
EPSILON = 1e-12

FUNCIONES_SOPORTADAS = {"sin", "cos", "tan", "asin", "acos", "atan"}
MODOS_SOPORTADOS = {"grados", "radianes"}


def limpiar_punto_flotante(valor: float) -> float:
    """
    Anula valores residuales menores a 1e-12 (ej. sin(180°) = 1.2246e-16 -> 0.0)
    y redondea a 10 decimales para evitar artefactos como 0.5000000000000001.
    """
    if abs(valor) < EPSILON:
        return 0.0
    redondeado = round(valor, 10)
    if abs(redondeado - round(redondeado)) < EPSILON:
        return float(round(redondeado))
    return redondeado


def formatear_salida(valor: float) -> str:
    """Convierte un número flotante a cadena eliminando ceros decimales innecesarios."""
    limpio = limpiar_punto_flotante(valor)
    if limpio.is_integer():
        return str(int(limpio))
    return f"{limpio:.10f}".rstrip("0").rstrip(".")


def evaluar_trigonometrica(fn: str, valor: float, modo: str) -> float:
    """
    Calcula la función solicitada respetando el modo de ángulo y las reglas de dominio.
    Lanza ValueError con mensaje descriptivo si ocurre una indeterminación matemática.
    """
    fn = fn.lower()
    if fn not in FUNCIONES_SOPORTADAS:
        raise ValueError(f"Operacion inexistente: '{fn}'")

    # Caso 1: Funciones trigonométricas directas (reciben un ángulo)
    if fn in {"sin", "cos", "tan"}:
        angulo_rad = math.radians(valor) if modo == "grados" else valor
        seno = math.sin(angulo_rad)
        coseno = math.cos(angulo_rad)

        if fn == "sin":
            return limpiar_punto_flotante(seno)
        elif fn == "cos":
            return limpiar_punto_flotante(coseno)
        elif fn == "tan":
            # En 90° + k*180° (o pi/2 + k*pi), cos(x) es 0 -> Asíntota vertical
            if abs(coseno) < EPSILON:
                raise ValueError(f"Tangente indefinida en {formatear_salida(valor)}")
            return limpiar_punto_flotante(seno / coseno)

    # Caso 2: Funciones trigonométricas inversas (reciben una razón adimensional)
    elif fn in {"asin", "acos"}:
        if valor < -1.0 or valor > 1.0:
            raise ValueError(
                f"Dominio invalido para {fn}: el valor {formatear_salida(valor)} debe estar entre -1 y 1"
            )
        angulo_rad = math.asin(valor) if fn == "asin" else math.acos(valor)
        resultado = math.degrees(angulo_rad) if modo == "grados" else angulo_rad
        return limpiar_punto_flotante(resultado)

    elif fn == "atan":
        angulo_rad = math.atan(valor)
        resultado = math.degrees(angulo_rad) if modo == "grados" else angulo_rad
        return limpiar_punto_flotante(resultado)


def parsear_argumentos(argv: list) -> tuple:
    """
    Extrae el modificador opcional [--modo grados|radianes] sin importar su posición
    y devuelve la lista de argumentos posicionales limpios junto con el modo activo.
    """
    modo = "grados"  # Modo por omisión según contrato técnico
    posicionales = []
    i = 0
    while i < len(argv):
        actual = argv[i]
        if actual == "--modo":
            if i + 1 >= len(argv):
                raise ValueError("Falta especificar 'grados' o 'radianes' despues de --modo")
            modo = argv[i + 1].lower()
            if modo not in MODOS_SOPORTADOS:
                raise ValueError(f"Modo invalido '{modo}': use 'grados' o 'radianes'")
            i += 2
        else:
            posicionales.append(actual)
            i += 1
    return posicionales, modo


def modo_calculo_unico(args: list, modo: str) -> None:
    """Ejecuta el Submodo 1: python trig.py <fn> <valor> [--modo grados|radianes]"""
    if len(args) != 2:
        print("ERROR;Cantidad de argumentos invalida. Uso: python trig.py <fn> <valor> [--modo grados|radianes]")
        return

    fn = args[0].lower()
    try:
        valor = float(args[1])
    except ValueError:
        print(f"ERROR;El valor '{args[1]}' no es un numero valido")
        return

    try:
        resultado = evaluar_trigonometrica(fn, valor, modo)
        print(f"OK;{formatear_salida(resultado)}")
    except ValueError as err:
        print(f"ERROR;{err}")


def modo_serie(args: list, modo: str) -> None:
    """Ejecuta el Submodo 2: python trig.py serie <fn> <xmin> <xmax> <paso> [--modo grados|radianes]"""
    if len(args) != 4:
        print("ERROR;Uso: python trig.py serie <fn> <xmin> <xmax> <paso> [--modo grados|radianes]")
        return

    fn = args[0].lower()
    if fn not in FUNCIONES_SOPORTADAS:
        print(f"ERROR;Operacion inexistente: '{fn}'")
        return

    try:
        xmin = float(args[1])
        xmax = float(args[2])
        paso = float(args[3])
    except ValueError:
        print("ERROR;Los parametros xmin, xmax y paso deben ser valores numericos")
        return

    # Validaciones de rango y paso exigidas por el contrato
    if xmin >= xmax:
        print("ERROR;Rango invalido: xmin debe ser estrictamente menor que xmax")
        return
    if paso <= 0:
        print("ERROR;Paso invalido: el paso debe ser mayor que cero")
        return

    # Iteración mediante multiplicador entero k para evitar deriva acumulativa de coma flotante
    k = 0
    while True:
        x = xmin + (k * paso)
        if x > xmax + (EPSILON * 100):
            break
        if abs(x - xmax) < (EPSILON * 100):
            x = xmax

        x_str = formatear_salida(x)
        try:
            y = evaluar_trigonometrica(fn, x, modo)
            print(f"{x_str};{formatear_salida(y)}")
        except ValueError:
            # Si la función no existe en x (ej. tan 90°), se imprime 'x;' con y vacío
            print(f"{x_str};")

        k += 1


def main() -> None:
    try:
        posicionales, modo = parsear_argumentos(sys.argv[1:])
        if not posicionales:
            print("ERROR;No se suministraron argumentos al script trig.py")
            return

        if posicionales[0].lower() == "serie":
            modo_serie(posicionales[1:], modo)
        else:
            modo_calculo_unico(posicionales, modo)

    except Exception as exc:
        # Guardrail: ninguna excepción rompe el contrato con Traceback
        print(f"ERROR;{exc}")


if __name__ == "__main__":
    main()

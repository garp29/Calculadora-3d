#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Archivo: bd/sql.py
Propósito: Herramienta CLI para ejecutar esquemas DDL, semillas y pruebas SQL
           en SQLite bajo Windows con formato de tabla legible.
Interfaz CLI:
  py sql.py <ruta_db> -c "<consulta_sql>"
  py sql.py <ruta_db> -f "<archivo.sql>"
  py sql.py <ruta_db> "<archivo.sql>"
"""

import sys
import os
import sqlite3

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")


def formatear_tabla(cursor: sqlite3.Cursor) -> None:
    """Imprime los resultados de una sentencia SELECT en una tabla ASCII alineada."""
    if cursor.description is None:
        return

    encabezados = [col[0] for col in cursor.description]
    filas = cursor.fetchall()

    anchos = [len(h) for h in encabezados]
    filas_str = []

    for fila in filas:
        fila_convertida = ["NULL" if v is None else str(v) for v in fila]
        filas_str.append(fila_convertida)
        for idx, texto in enumerate(fila_convertida):
            if len(texto) > anchos[idx]:
                anchos[idx] = len(texto)

    plantilla = " | ".join(f"{{:<{w}}}" for w in anchos)
    linea_sep = "-+-".join("-" * w for w in anchos)

    print(plantilla.format(*encabezados))
    print(linea_sep)
    for f in filas_str:
        print(plantilla.format(*f))
    print(f"({len(filas_str)} fila(s))\n")


def dividir_sentencias_sql(contenido_sql: str) -> list:
    """
    Separa un script SQL en sentencias individuales completas respetando
    comentarios y puntos y coma utilizando sqlite3.complete_statement.
    """
    sentencias = []
    buffer_actual = []

    for linea in contenido_sql.splitlines():
        linea_sin_comentario = linea.split("--")[0].strip()
        if not linea_sin_comentario:
            continue
        buffer_actual.append(linea)
        candidato = "\n".join(buffer_actual)
        if sqlite3.complete_statement(candidato):
            sentencias.append(candidato.strip())
            buffer_actual = []

    if buffer_actual:
        resto = "\n".join(buffer_actual).strip()
        if resto:
            sentencias.append(resto)

    return sentencias


def ejecutar_bloque_sql(conexion: sqlite3.Connection, texto_sql: str) -> None:
    """
    Ejecuta cada sentencia del bloque activando llaves foráneas.
    Si una sentencia falla por restricción de integridad (ej. en pruebas.sql),
    reporta el error exacto y continúa para permitir auditar todas las pruebas.
    """
    cursor = conexion.cursor()
    cursor.execute("PRAGMA foreign_keys = ON;")

    sentencias = dividir_sentencias_sql(texto_sql)
    for num, sentencia in enumerate(sentencias, start=1):
        try:
            cursor.execute(sentencia)
            if cursor.description is not None:
                formatear_tabla(cursor)
            else:
                conexion.commit()
                verbo = sentencia.split()[0].upper() if sentencia.split() else "SQL"
                print(f"[OK - Sentencia #{num}] {verbo} ejecutado correctamente (filas afectadas: {cursor.rowcount}).")
        except sqlite3.Error as err:
            conexion.rollback()
            print(f"ERROR;Rechazo de SQLite en sentencia #{num}: {err}")


def main() -> None:
    if len(sys.argv) < 3:
        print("ERROR;Uso esperado: py sql.py <ruta_db> [-c \"<consulta_sql>\" | -f \"<archivo.sql>\"]")
        sys.exit(1)

    ruta_db = os.path.abspath(sys.argv[1])
    bandera = sys.argv[2]

    try:
        # Asegurar directorio para la base de datos
        dir_db = os.path.dirname(ruta_db)
        if dir_db and not os.path.exists(dir_db):
            os.makedirs(dir_db, exist_ok=True)

        with sqlite3.connect(ruta_db) as conexion:
            if bandera == "-c":
                if len(sys.argv) < 4:
                    print("ERROR;Falta proporcionar la consulta SQL despues de la opcion -c")
                    sys.exit(1)
                ejecutar_bloque_sql(conexion, sys.argv[3])

            elif bandera == "-f":
                if len(sys.argv) < 4:
                    print("ERROR;Falta proporcionar la ruta del archivo .sql despues de la opcion -f")
                    sys.exit(1)
                ruta_archivo = os.path.abspath(sys.argv[3])
                if not os.path.isfile(ruta_archivo):
                    print(f"ERROR;No existe el archivo SQL: {ruta_archivo}")
                    sys.exit(1)
                with open(ruta_archivo, "r", encoding="utf-8") as archivo:
                    ejecutar_bloque_sql(conexion, archivo.read())

            else:
                # Soporte directo: py sql.py calculadora.db archivo.sql
                ruta_archivo = os.path.abspath(bandera)
                if not os.path.isfile(ruta_archivo):
                    print(f"ERROR;Bandera desconocida o archivo inexistente: {bandera}")
                    sys.exit(1)
                with open(ruta_archivo, "r", encoding="utf-8") as archivo:
                    ejecutar_bloque_sql(conexion, archivo.read())

    except Exception as exc:
        print(f"ERROR;Fallo general en sql.py: {exc}")
        sys.exit(1)


if __name__ == "__main__":
    main()

package calculadora.modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquestador en Java para invocar el motor de renderizado de superficies 3D en Python.
 * Cumple con los principios de ProcessBuilder (G1: rutas con espacios en Windows, G3: control de errores).
 */
public class Graficador3D {

    public static final String[] FUNCIONES_DISPONIBLES = {
            "ondas", "sombrero", "paraboloide", "silla", "ripple", "gaussiana"
    };

    public static final String[] PALETAS_COLOR = {
            "coolwarm", "viridis", "plasma", "magma", "cividis"
    };

    /**
     * Invoca python/graficar_3d.py para renderizar una superficie z = f(x, y).
     *
     * @param funcion Nombre clave de la superficie o fórmula (ej. "ondas", "sombrero", "paraboloide").
     * @param xMin Límite inferior en X.
     * @param xMax Límite superior en X.
     * @param yMin Límite inferior en Y.
     * @param yMax Límite superior en Y.
     * @param paso Resolución de la malla en X e Y.
     * @param rutaSalida Ruta destino del PNG.
     * @param paletaColor Mapa de colores de Matplotlib (ej. "coolwarm", "viridis").
     * @return Ruta absoluta del archivo PNG generado.
     * @throws Exception Si ocurre un fallo en los parámetros o en el subproceso Python.
     */
    public static String generarSuperficie3D(String funcion, double xMin, double xMax,
                                             double yMin, double yMax, double paso,
                                             String rutaSalida, String paletaColor) throws Exception {

        List<String> comando = new ArrayList<>();
        comando.add("py");
        comando.add("python" + File.separator + "graficar_3d.py");
        comando.add(funcion);
        comando.add(String.valueOf(xMin));
        comando.add(String.valueOf(xMax));
        comando.add(String.valueOf(yMin));
        comando.add(String.valueOf(yMax));
        comando.add(String.valueOf(paso));
        comando.add(rutaSalida);

        if (paletaColor != null && !paletaColor.isBlank()) {
            comando.add("--cmap");
            comando.add(paletaColor.trim());
        }

        ProcessBuilder pb = new ProcessBuilder(comando);
        pb.redirectErrorStream(true);
        Process proceso = pb.start();

        String respuestaLinea = null;
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8))) {
            respuestaLinea = lector.readLine();
        }

        int exitCode = proceso.waitFor();

        if (respuestaLinea == null || respuestaLinea.isBlank()) {
            throw new Exception("El script 3D no devolvió respuesta (código: " + exitCode + ").");
        }

        if (respuestaLinea.startsWith("OK;")) {
            return respuestaLinea.substring(3).trim();
        } else if (respuestaLinea.startsWith("ERROR;")) {
            throw new Exception("Error al renderizar 3D -> " + respuestaLinea.substring(6));
        } else {
            throw new Exception("Respuesta inesperada del motor 3D: " + respuestaLinea);
        }
    }
}

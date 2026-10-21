package calculadora.modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OperacionTrigonometrica implements IOperacion {
    private String simbolo; // Ej: "sin", "cos", "tan", etc.
    private String nombre;  // Ej: "Seno", "Coseno", etc.
    private String modoAngulo; // "grados" o "radianes"

    public OperacionTrigonometrica(String simbolo, String nombre, String modoAngulo) {
        this.simbolo = simbolo;
        this.nombre = nombre;
        this.modoAngulo = (modoAngulo != null) ? modoAngulo : "grados";
    }

    @Override
    public String getSimbolo() {
        return this.simbolo;
    }

    @Override
    public String getNombre() {
        return this.nombre;
    }

    @Override
    public String getTipo() {
        return "TRIGONOMETRICA";
    }

    /**
     * Método polimórfico de la interfaz. Para operaciones unarias, 
     * 'a' es el valor de entrada y 'b' se ignora (o se pasa 0).
     */
    @Override
    public double calcular(double a, double b) throws Exception {
        return ejecutarPythonTrig(a, this.modoAngulo);
    }

    /**
     * Invoca el script python/trig.py utilizando ProcessBuilder de manera robusta.
     */
    public double ejecutarPythonTrig(double valor, String modo) throws Exception {
        List<String> comando = new ArrayList<>();
        
        // En Windows se usa "python" o "py" según la configuración del entorno
        comando.add("py");
        
        // Ruta relativa del script entregado por el docente
        comando.add("python" + File.separator + "trig.py");
        
        // Argumentos individuales del contrato (evita problemas de rutas con espacios)
        comando.add(this.simbolo);
        comando.add(String.valueOf(valor));
        comando.add("--modo");
        comando.add(modo);

        ProcessBuilder pb = new ProcessBuilder(comando);
        // Redirigir el flujo de errores estándar al flujo de entrada para capturarlo todo junto
        pb.redirectErrorStream(true);

        Process proceso = pb.start();

        // Leer la respuesta de Python asegurando codificación UTF-8
        String respuestaLinea = null;
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8))) {
            respuestaLinea = lector.readLine();
        }

        proceso.waitFor();

        if (respuestaLinea == null || respuestaLinea.isEmpty()) {
            throw new Exception("El script Python no devolvió ninguna respuesta.");
        }

        // Evaluar el contrato técnico de Python: OK;<resultado> o ERROR;<mensaje>
        if (respuestaLinea.startsWith("OK;")) {
            String valorStr = respuestaLinea.substring(3);
            return Double.parseDouble(valorStr);
        } else if (respuestaLinea.startsWith("ERROR;")) {
            String mensajeError = respuestaLinea.substring(6);
            // Requisito G3: Tratar el error como un resultado controlado sin que Java se caiga
            throw new Exception("Error devuelto por Python -> " + mensajeError);
        } else {
            throw new Exception("Respuesta con formato desconocido de Python: " + respuestaLinea);
        }
    }

    /**
     * Invoca python/trig.py en modo 'serie' y devuelve la lista de puntos (x, y).
     * Cuando la función se indefine (ej. tan 90°), el valor y queda como null (Requisito R6).
     */
    public List<Double[]> generarSeriePython(double xMin, double xMax, double paso, String modo) throws Exception {
        String modoEfectivo = (modo != null && !modo.isBlank()) ? modo : this.modoAngulo;
        List<String> comando = new ArrayList<>();
        comando.add("py");
        comando.add("python" + File.separator + "trig.py");
        comando.add("serie");
        comando.add(this.simbolo);
        comando.add(String.valueOf(xMin));
        comando.add(String.valueOf(xMax));
        comando.add(String.valueOf(paso));
        comando.add("--modo");
        comando.add(modoEfectivo);

        ProcessBuilder pb = new ProcessBuilder(comando);
        pb.redirectErrorStream(true);
        Process proceso = pb.start();

        List<Double[]> puntos = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;
                }
                if (linea.startsWith("ERROR;")) {
                    throw new Exception("Error en serie Python -> " + linea.substring(6));
                }
                String[] partes = linea.split(";", -1);
                if (partes.length >= 2) {
                    double x = Double.parseDouble(partes[0]);
                    Double y = partes[1].isEmpty() ? null : Double.parseDouble(partes[1]);
                    puntos.add(new Double[]{x, y});
                }
            }
        }
        proceso.waitFor();

        if (puntos.isEmpty()) {
            throw new Exception("El script Python no generó ningún punto para la serie.");
        }
        return puntos;
    }

    /**
     * Invoca python/graficar.py para renderizar desde SQLite la curva PNG de una serie.
     * Opcionalmente recibe un punto (puntoX, puntoY) para resaltarlo en tiempo real sobre la curva.
     */
    public static String ejecutarGraficarPython(String rutaDb, int serieId, String rutaSalidaPng,
                                                Double puntoX, Double puntoY) throws Exception {
        List<String> comando = new ArrayList<>();
        comando.add("py");
        comando.add("python" + File.separator + "graficar.py");
        comando.add(rutaDb);
        comando.add(String.valueOf(serieId));
        comando.add(rutaSalidaPng);

        if (puntoX != null && puntoY != null) {
            comando.add("--punto");
            comando.add(String.valueOf(puntoX));
            comando.add(String.valueOf(puntoY));
        }

        ProcessBuilder pb = new ProcessBuilder(comando);
        pb.redirectErrorStream(true);
        Process proceso = pb.start();

        String respuestaLinea = null;
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8))) {
            respuestaLinea = lector.readLine();
        }
        proceso.waitFor();

        if (respuestaLinea == null || respuestaLinea.isEmpty()) {
            throw new Exception("El script graficar.py no devolvió respuesta.");
        }
        if (respuestaLinea.startsWith("OK;")) {
            return respuestaLinea.substring(3).trim();
        } else if (respuestaLinea.startsWith("ERROR;")) {
            throw new Exception("Error al graficar -> " + respuestaLinea.substring(6));
        } else {
            throw new Exception("Respuesta inesperada de graficar.py: " + respuestaLinea);
        }
    }
}

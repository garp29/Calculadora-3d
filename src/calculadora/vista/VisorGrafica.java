package calculadora.vista;

import calculadora.dao.CalculoDAO;
import calculadora.modelo.OperacionTrigonometrica;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Ventana interactiva en tiempo real que muestra la gráfica PNG generada por Python
 * desde SQLite y permite ingresar operaciones en vivo.
 */
public class VisorGrafica extends JFrame {

    private static VisorGrafica instancia;

    private final CalculoDAO dao;
    private final JLabel lblEstado;
    private final JLabel lblRutaArchivo;
    private final PanelImagen panelImagen;

    // Controles interactivos en tiempo real dentro de la ventana
    private final JComboBox<String> cmbFuncion;
    private final JTextField txtValor;
    private final JComboBox<String> cmbModo;
    private final JTextField txtXMin;
    private final JTextField txtXMax;
    private final JTextField txtPaso;

    public VisorGrafica(CalculoDAO dao) {
        super("Calculadora Integrada — Visor Gráfico en Tiempo Real (Java + Python + SQLite)");
        this.dao = dao;

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(980, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(248, 250, 252));

        // 1. Panel superior: Controles interactivos en tiempo real
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelControles.setBackground(new Color(30, 41, 59));
        panelControles.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel lblFn = crearEtiquetaBlanca("Función:");
        cmbFuncion = new JComboBox<>(new String[]{"sin", "cos", "tan", "asin", "acos", "atan"});
        cmbFuncion.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel lblVal = crearEtiquetaBlanca("Valor/Ángulo:");
        txtValor = new JTextField("90", 6);
        txtValor.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel lblModo = crearEtiquetaBlanca("Modo:");
        cmbModo = new JComboBox<>(new String[]{"grados", "radianes"});
        cmbModo.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JLabel lblMin = crearEtiquetaBlanca("xMin:");
        txtXMin = new JTextField("-360", 5);
        JLabel lblMax = crearEtiquetaBlanca("xMax:");
        txtXMax = new JTextField("360", 5);
        JLabel lblPaso = crearEtiquetaBlanca("Paso:");
        txtPaso = new JTextField("15", 4);

        JButton btnGraficar = new JButton("Calcular y Graficar en Vivo");
        btnGraficar.setBackground(new Color(37, 99, 235));
        btnGraficar.setForeground(Color.WHITE);
        btnGraficar.setFocusPainted(false);
        btnGraficar.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Ajustar rango sugerido automáticamente al cambiar de función o modo
        cmbFuncion.addActionListener(e -> ajustarRangoPorDefecto());
        cmbModo.addActionListener(e -> ajustarRangoPorDefecto());

        // Ejecutar en tiempo real al presionar Enter o el botón
        Runnable accionEnVivo = this::ejecutarDesdeVentana;
        btnGraficar.addActionListener(e -> accionEnVivo.run());
        txtValor.addActionListener(e -> accionEnVivo.run());
        txtXMin.addActionListener(e -> accionEnVivo.run());
        txtXMax.addActionListener(e -> accionEnVivo.run());
        txtPaso.addActionListener(e -> accionEnVivo.run());

        panelControles.add(lblFn);
        panelControles.add(cmbFuncion);
        panelControles.add(lblVal);
        panelControles.add(txtValor);
        panelControles.add(lblModo);
        panelControles.add(cmbModo);
        panelControles.add(lblMin);
        panelControles.add(txtXMin);
        panelControles.add(lblMax);
        panelControles.add(txtXMax);
        panelControles.add(lblPaso);
        panelControles.add(txtPaso);
        panelControles.add(btnGraficar);

        add(panelControles, BorderLayout.NORTH);

        // 2. Panel central: Lienzo de la imagen PNG
        panelImagen = new PanelImagen();
        add(panelImagen, BorderLayout.CENTER);

        // 3. Panel inferior: Barra de estado con resultado y ruta donde queda guardada
        JPanel panelInferior = new JPanel(new BorderLayout(6, 4));
        panelInferior.setBackground(new Color(241, 245, 249));
        panelInferior.setBorder(new EmptyBorder(8, 14, 8, 14));

        lblEstado = new JLabel("Listo. Ingrese una operación en la consola o en la barra superior para graficar en tiempo real.");
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblEstado.setForeground(new Color(15, 23, 42));

        lblRutaArchivo = new JLabel("Archivo PNG: bd\\grafica_serie_1.png");
        lblRutaArchivo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lblRutaArchivo.setForeground(new Color(71, 85, 105));

        panelInferior.add(lblEstado, BorderLayout.NORTH);
        panelInferior.add(lblRutaArchivo, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);

        // Si existe una gráfica previa en bd\grafica_actual.png, mostrarla de inmediato al abrir
        File imgInicial = new File("bd" + File.separator + "grafica_actual.png");
        if (imgInicial.exists()) {
            try {
                panelImagen.setImagen(ImageIO.read(imgInicial));
                lblRutaArchivo.setText("Archivo PNG activo: " + imgInicial.getAbsolutePath());
            } catch (Exception ignored) {
            }
        }
    }

    private JLabel crearEtiquetaBlanca(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        return lbl;
    }

    private void ajustarRangoPorDefecto() {
        String fn = (String) cmbFuncion.getSelectedItem();
        String modo = (String) cmbModo.getSelectedItem();
        if ("asin".equals(fn) || "acos".equals(fn)) {
            txtXMin.setText("-1");
            txtXMax.setText("1");
            txtPaso.setText("0.05");
        } else if ("atan".equals(fn)) {
            txtXMin.setText("-10");
            txtXMax.setText("10");
            txtPaso.setText("0.5");
        } else if ("radianes".equals(modo)) {
            txtXMin.setText("-6.28");
            txtXMax.setText("6.28");
            txtPaso.setText("0.25");
        } else {
            txtXMin.setText("-360");
            txtXMax.setText("360");
            txtPaso.setText("15");
        }
    }

    private void ejecutarDesdeVentana() {
        String fn = (String) cmbFuncion.getSelectedItem();
        String modo = (String) cmbModo.getSelectedItem();
        try {
            double valor = Double.parseDouble(txtValor.getText().trim());
            double xMin = Double.parseDouble(txtXMin.getText().trim());
            double xMax = Double.parseDouble(txtXMax.getText().trim());
            double paso = Double.parseDouble(txtPaso.getText().trim());

            // Ejecutar en hilo separado para no bloquear la interfaz Swing
            new Thread(() -> {
                procesarYGraficarEnTiempoReal(dao, fn, valor, xMin, xMax, paso, modo, true);
            }).start();

        } catch (NumberFormatException ex) {
            actualizarMensajeError("Error de formato: verifique que Valor, xMin, xMax y Paso sean números válidos.");
        }
    }

    /**
     * Carga y muestra en tiempo real la imagen PNG generada por Python.
     */
    public void actualizarGrafica(String rutaPng, String mensajeResumen, String fn, Double valor, String modo,
                                  double xMin, double xMax, double paso) {
        SwingUtilities.invokeLater(() -> {
            try {
                File archivo = new File(rutaPng);
                if (archivo.exists()) {
                    BufferedImage img = ImageIO.read(archivo);
                    panelImagen.setImagen(img);
                }
                if (fn != null) {
                    cmbFuncion.setSelectedItem(fn);
                }
                if (modo != null) {
                    cmbModo.setSelectedItem(modo);
                }
                if (valor != null) {
                    txtValor.setText(String.valueOf(valor));
                }
                txtXMin.setText(String.valueOf(xMin));
                txtXMax.setText(String.valueOf(xMax));
                txtPaso.setText(String.valueOf(paso));

                lblEstado.setText(mensajeResumen);
                lblEstado.setForeground(new Color(22, 101, 52));
                lblRutaArchivo.setText("Guardado en SQLite (bd\\calculadora.db) y renderizado en: " + archivo.getAbsolutePath());

                if (!isVisible()) {
                    setVisible(true);
                }
                toFront();
            } catch (Exception e) {
                lblEstado.setText("Error al cargar imagen PNG: " + e.getMessage());
                lblEstado.setForeground(new Color(185, 28, 28));
            }
        });
    }

    public void actualizarMensajeError(String mensajeError) {
        SwingUtilities.invokeLater(() -> {
            lblEstado.setText(mensajeError);
            lblEstado.setForeground(new Color(185, 28, 28));
            if (!isVisible()) {
                setVisible(true);
            }
            toFront();
        });
    }

    /**
     * Obtiene o crea la instancia única del visor gráfico si el entorno soporta interfaz gráfica.
     */
    public static synchronized VisorGrafica obtenerVisor(CalculoDAO dao) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        if (instancia == null) {
            instancia = new VisorGrafica(dao);
        }
        return instancia;
    }

    /**
     * Orquesta el flujo completo en tiempo real:
     * 1) Calcula el valor puntual (si se suministra) y lo guarda en la tabla 'calculo'.
     * 2) Genera la serie de puntos con trig.py y la guarda transaccionalmente en 'serie' y 'punto'.
     * 3) Renderiza el archivo PNG con graficar.py marcando el punto evaluado.
     * 4) Muestra/actualiza la gráfica en vivo en la ventana interactiva.
     */
    public static void procesarYGraficarEnTiempoReal(CalculoDAO dao, String fn, Double valorPuntual,
                                                     double xMin, double xMax, double paso,
                                                     String modo, boolean registrarCalculoPuntual) {
        int operacionIdDb = switch (fn) {
            case "sin" -> 5;
            case "cos" -> 6;
            case "tan" -> 7;
            case "asin" -> 8;
            case "acos" -> 9;
            case "atan" -> 10;
            default -> 0;
        };

        if (operacionIdDb == 0) {
            System.out.println("ERROR;Función trigonométrica desconocida: " + fn);
            return;
        }

        OperacionTrigonometrica trigOp = new OperacionTrigonometrica(fn, fn, modo);
        Double resultadoPuntual = null;
        String errorPuntual = null;

        // 1. Si hay un valor puntual, calcularlo y guardarlo en SQLite
        if (valorPuntual != null) {
            try {
                resultadoPuntual = trigOp.calcular(valorPuntual, 0);
                if (registrarCalculoPuntual) {
                    dao.guardarCalculo(operacionIdDb, valorPuntual, null, modo, resultadoPuntual, 1, null);
                }
            } catch (Exception ex) {
                errorPuntual = ex.getMessage();
                if (registrarCalculoPuntual) {
                    dao.guardarCalculo(operacionIdDb, valorPuntual, null, modo, null, 0, errorPuntual);
                }
            }
        }

        // Asegurar que el rango [xMin, xMax] incluya al valorPuntual para que sea visible en la gráfica
        if (valorPuntual != null && resultadoPuntual != null) {
            if (valorPuntual < xMin) {
                xMin = valorPuntual - paso * 2;
            }
            if (valorPuntual > xMax) {
                xMax = valorPuntual + paso * 2;
            }
        }

        // 2. Generar la serie en Python y guardarla en SQLite
        try {
            List<Double[]> puntos = trigOp.generarSeriePython(xMin, xMax, paso, modo);

            String tituloSerie;
            if (valorPuntual != null && resultadoPuntual != null) {
                tituloSerie = String.format("Función %s(x) | Evaluación: %s(%s) = %s [%s]",
                        fn, fn, formatearNum(valorPuntual), formatearNum(resultadoPuntual), modo);
            } else if (valorPuntual != null && errorPuntual != null) {
                tituloSerie = String.format("Función %s(x) | Indefinido en x=%s [%s]",
                        fn, formatearNum(valorPuntual), modo);
            } else {
                tituloSerie = String.format("Serie de %s(x) de %s a %s (paso %s, %s)",
                        fn, formatearNum(xMin), formatearNum(xMax), formatearNum(paso), modo);
            }

            int serieId = dao.guardarSerieConPuntos(tituloSerie, operacionIdDb, xMin, xMax, paso, modo, puntos);
            if (serieId <= 0) {
                System.out.println("ERROR;No se pudo guardar la serie en la base de datos.");
                return;
            }

            String rutaDb = "bd" + File.separator + "calculadora.db";
            String rutaPng = "bd" + File.separator + "grafica_serie_" + serieId + ".png";
            String rutaPngActual = "bd" + File.separator + "grafica_actual.png";

            // 3. Invocar graficar.py (genera tanto grafica_serie_<id>.png como grafica_actual.png)
            String rutaGenerada = OperacionTrigonometrica.ejecutarGraficarPython(
                    rutaDb, serieId, rutaPng, valorPuntual, resultadoPuntual
            );
            OperacionTrigonometrica.ejecutarGraficarPython(
                    rutaDb, serieId, rutaPngActual, valorPuntual, resultadoPuntual
            );

            String resumen;
            if (valorPuntual != null && resultadoPuntual != null) {
                resumen = String.format("Resultado: %s(%s %s) = %s  |  Serie ID #%d (%d puntos)",
                        fn, formatearNum(valorPuntual), modo, formatearNum(resultadoPuntual), serieId, puntos.size());
            } else if (errorPuntual != null) {
                resumen = String.format("Evaluación en x=%s: %s  |  Serie ID #%d graficada",
                        formatearNum(valorPuntual), errorPuntual, serieId);
            } else {
                resumen = String.format("Serie ID #%d generada con éxito (%d puntos)", serieId, puntos.size());
            }

            System.out.println("📈 Gráfica guardada en: " + rutaGenerada);
            System.out.println("📈 Copia rápida actualizada en: " + new File(rutaPngActual).getAbsolutePath());

            // 4. Actualizar visor en tiempo real
            VisorGrafica visor = obtenerVisor(dao);
            if (visor != null) {
                visor.actualizarGrafica(rutaGenerada, resumen, fn, valorPuntual, modo, xMin, xMax, paso);
            }

        } catch (Exception e) {
            System.out.println("ERROR al graficar serie: " + e.getMessage());
            VisorGrafica visor = obtenerVisor(dao);
            if (visor != null) {
                visor.actualizarMensajeError("ERROR: " + e.getMessage());
            }
        }
    }

    private static String formatearNum(double v) {
        if (v == (long) v) {
            return String.format("%d", (long) v);
        }
        return String.valueOf(v);
    }

    /**
     * Panel personalizado que escala la imagen PNG manteniendo su proporción y nitidez.
     */
    private static class PanelImagen extends JPanel {
        private BufferedImage imagen;

        public PanelImagen() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        }

        public void setImagen(BufferedImage img) {
            this.imagen = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagen == null) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(100, 116, 139));
                g2.setFont(new Font("SansSerif", Font.ITALIC, 15));
                String msg = "La gráfica aparecerá aquí en tiempo real en cuanto ingreses una operación...";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = getHeight() / 2;
                g2.drawString(msg, Math.max(20, x), y);
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int panelW = getWidth() - 20;
            int panelH = getHeight() - 20;
            int imgW = imagen.getWidth();
            int imgH = imagen.getHeight();

            double escala = Math.min((double) panelW / imgW, (double) panelH / imgH);
            int nuevoW = (int) (imgW * escala);
            int nuevoH = (int) (imgH * escala);
            int x = (getWidth() - nuevoW) / 2;
            int y = (getHeight() - nuevoH) / 2;

            g2.drawImage(imagen, x, y, nuevoW, nuevoH, null);
        }
    }
}

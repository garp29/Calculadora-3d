package calculadora.vista;

import calculadora.dao.CalculoDAO;
import calculadora.modelo.Graficador3D;
import calculadora.modelo.OperacionTrigonometrica;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Ventana interactiva en tiempo real que muestra gráficas 2D y superficies 3D
 * generadas por Python e integradas con Java y SQLite.
 */
public class VisorGrafica extends JFrame {

    private static VisorGrafica instancia;

    private final CalculoDAO dao;
    private final JLabel lblEstado;
    private final JLabel lblRutaArchivo;
    private final PanelImagen panelImagen;

    // Controles 2D
    private final JComboBox<String> cmbFuncion2D;
    private final JTextField txtValor2D;
    private final JComboBox<String> cmbModo2D;
    private final JTextField txtXMin2D;
    private final JTextField txtXMax2D;
    private final JTextField txtPaso2D;

    // Controles 3D
    private final JComboBox<String> cmbFuncion3D;
    private final JTextField txtXMin3D;
    private final JTextField txtXMax3D;
    private final JTextField txtYMin3D;
    private final JTextField txtYMax3D;
    private final JTextField txtPaso3D;
    private final JComboBox<String> cmbPaleta3D;

    public VisorGrafica(CalculoDAO dao) {
        super("Calculadora 3D Integrada — Visor Gráfico en Tiempo Real (Java + Python + SQLite)");
        this.dao = dao;

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(1020, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(248, 250, 252));

        // Pestañas superiores para alternar entre Modo 2D y Modo 3D
        JTabbedPane tabsModos = new JTabbedPane();
        tabsModos.setFont(new Font("SansSerif", Font.BOLD, 12));
        tabsModos.setBackground(new Color(241, 245, 249));

        // ==========================================
        // Pestaña 1: Controles 2D
        // ==========================================
        JPanel panel2D = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel2D.setBackground(new Color(30, 41, 59));
        panel2D.setBorder(new EmptyBorder(4, 8, 4, 8));

        panel2D.add(crearEtiquetaBlanca("Función:"));
        cmbFuncion2D = new JComboBox<>(new String[]{"sin", "cos", "tan", "asin", "acos", "atan"});
        cmbFuncion2D.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel2D.add(cmbFuncion2D);

        panel2D.add(crearEtiquetaBlanca("Ángulo/Valor:"));
        txtValor2D = new JTextField("90", 5);
        panel2D.add(txtValor2D);

        panel2D.add(crearEtiquetaBlanca("Modo:"));
        cmbModo2D = new JComboBox<>(new String[]{"grados", "radianes"});
        panel2D.add(cmbModo2D);

        panel2D.add(crearEtiquetaBlanca("xMin:"));
        txtXMin2D = new JTextField("-360", 4);
        panel2D.add(txtXMin2D);

        panel2D.add(crearEtiquetaBlanca("xMax:"));
        txtXMax2D = new JTextField("360", 4);
        panel2D.add(txtXMax2D);

        panel2D.add(crearEtiquetaBlanca("Paso:"));
        txtPaso2D = new JTextField("15", 3);
        panel2D.add(txtPaso2D);

        JButton btnGraficar2D = new JButton("Calcular y Graficar 2D");
        btnGraficar2D.setBackground(new Color(37, 99, 235));
        btnGraficar2D.setForeground(Color.WHITE);
        btnGraficar2D.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnGraficar2D.setFocusPainted(false);
        panel2D.add(btnGraficar2D);

        cmbFuncion2D.addActionListener(e -> ajustarRangoPorDefecto2D());
        cmbModo2D.addActionListener(e -> ajustarRangoPorDefecto2D());

        Runnable accion2D = this::ejecutarDesdeVentana2D;
        btnGraficar2D.addActionListener(e -> accion2D.run());
        txtValor2D.addActionListener(e -> accion2D.run());
        txtXMin2D.addActionListener(e -> accion2D.run());
        txtXMax2D.addActionListener(e -> accion2D.run());
        txtPaso2D.addActionListener(e -> accion2D.run());

        tabsModos.addTab("📈 Curvas 2D (Seno, Coseno, Tangente)", panel2D);

        // ==========================================
        // Pestaña 2: Controles 3D
        // ==========================================
        JPanel panel3D = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel3D.setBackground(new Color(15, 23, 42));
        panel3D.setBorder(new EmptyBorder(4, 8, 4, 8));

        panel3D.add(crearEtiquetaBlanca("Superficie 3D:"));
        cmbFuncion3D = new JComboBox<>(Graficador3D.FUNCIONES_DISPONIBLES);
        cmbFuncion3D.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel3D.add(cmbFuncion3D);

        panel3D.add(crearEtiquetaBlanca("X:"));
        txtXMin3D = new JTextField("-5", 4);
        txtXMax3D = new JTextField("5", 4);
        panel3D.add(txtXMin3D);
        panel3D.add(new JLabel("a") {{ setForeground(Color.WHITE); }});
        panel3D.add(txtXMax3D);

        panel3D.add(crearEtiquetaBlanca("Y:"));
        txtYMin3D = new JTextField("-5", 4);
        txtYMax3D = new JTextField("5", 4);
        panel3D.add(txtYMin3D);
        panel3D.add(new JLabel("a") {{ setForeground(Color.WHITE); }});
        panel3D.add(txtYMax3D);

        panel3D.add(crearEtiquetaBlanca("Paso:"));
        txtPaso3D = new JTextField("0.25", 4);
        panel3D.add(txtPaso3D);

        panel3D.add(crearEtiquetaBlanca("Paleta:"));
        cmbPaleta3D = new JComboBox<>(Graficador3D.PALETAS_COLOR);
        panel3D.add(cmbPaleta3D);

        JButton btnGraficar3D = new JButton("Renderizar Superficie 3D");
        btnGraficar3D.setBackground(new Color(220, 38, 38));
        btnGraficar3D.setForeground(Color.WHITE);
        btnGraficar3D.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnGraficar3D.setFocusPainted(false);
        panel3D.add(btnGraficar3D);

        cmbFuncion3D.addActionListener(e -> ajustarRangoPorDefecto3D());
        Runnable accion3D = this::ejecutarDesdeVentana3D;
        btnGraficar3D.addActionListener(e -> accion3D.run());
        txtXMin3D.addActionListener(e -> accion3D.run());
        txtXMax3D.addActionListener(e -> accion3D.run());
        txtYMin3D.addActionListener(e -> accion3D.run());
        txtYMax3D.addActionListener(e -> accion3D.run());
        txtPaso3D.addActionListener(e -> accion3D.run());

        tabsModos.addTab("🌐 Superficies 3D: z = f(x, y)", panel3D);

        add(tabsModos, BorderLayout.NORTH);

        // 2. Panel central: Lienzo de la imagen
        panelImagen = new PanelImagen();
        add(panelImagen, BorderLayout.CENTER);

        // 3. Panel inferior: Barra de estado
        JPanel panelInferior = new JPanel(new BorderLayout(6, 4));
        panelInferior.setBackground(new Color(241, 245, 249));
        panelInferior.setBorder(new EmptyBorder(8, 14, 8, 14));

        lblEstado = new JLabel("Listo. Seleccione una operación en 2D o 3D para graficar en tiempo real.");
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblEstado.setForeground(new Color(15, 23, 42));

        lblRutaArchivo = new JLabel("Archivo PNG activo: bd\\grafica_actual.png");
        lblRutaArchivo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lblRutaArchivo.setForeground(new Color(71, 85, 105));

        panelInferior.add(lblEstado, BorderLayout.NORTH);
        panelInferior.add(lblRutaArchivo, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);

        // Cargar imagen inicial si existe
        File imgInicial = new File("bd" + File.separator + "grafica_actual.png");
        if (!imgInicial.exists()) {
            imgInicial = new File("bd" + File.separator + "grafica_3d_test.png");
        }
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

    private void ajustarRangoPorDefecto2D() {
        String fn = (String) cmbFuncion2D.getSelectedItem();
        String modo = (String) cmbModo2D.getSelectedItem();
        if ("asin".equals(fn) || "acos".equals(fn)) {
            txtXMin2D.setText("-1");
            txtXMax2D.setText("1");
            txtPaso2D.setText("0.05");
        } else if ("atan".equals(fn)) {
            txtXMin2D.setText("-10");
            txtXMax2D.setText("10");
            txtPaso2D.setText("0.5");
        } else if ("radianes".equals(modo)) {
            txtXMin2D.setText("-6.28");
            txtXMax2D.setText("6.28");
            txtPaso2D.setText("0.25");
        } else {
            txtXMin2D.setText("-360");
            txtXMax2D.setText("360");
            txtPaso2D.setText("15");
        }
    }

    private void ajustarRangoPorDefecto3D() {
        String fn = (String) cmbFuncion3D.getSelectedItem();
        if ("sombrero".equals(fn)) {
            txtXMin3D.setText("-8");
            txtXMax3D.setText("8");
            txtYMin3D.setText("-8");
            txtYMax3D.setText("8");
            txtPaso3D.setText("0.25");
        } else if ("gaussiana".equals(fn)) {
            txtXMin3D.setText("-3");
            txtXMax3D.setText("3");
            txtYMin3D.setText("-3");
            txtYMax3D.setText("3");
            txtPaso3D.setText("0.15");
        } else {
            txtXMin3D.setText("-5");
            txtXMax3D.setText("5");
            txtYMin3D.setText("-5");
            txtYMax3D.setText("5");
            txtPaso3D.setText("0.25");
        }
    }

    private void ejecutarDesdeVentana2D() {
        String fn = (String) cmbFuncion2D.getSelectedItem();
        String modo = (String) cmbModo2D.getSelectedItem();
        try {
            double valor = Double.parseDouble(txtValor2D.getText().trim());
            double xMin = Double.parseDouble(txtXMin2D.getText().trim());
            double xMax = Double.parseDouble(txtXMax2D.getText().trim());
            double paso = Double.parseDouble(txtPaso2D.getText().trim());

            new Thread(() -> procesarYGraficarEnTiempoReal(dao, fn, valor, xMin, xMax, paso, modo, true)).start();
        } catch (NumberFormatException ex) {
            actualizarMensajeError("Error numérico en 2D: verifique Valor, xMin, xMax y Paso.");
        }
    }

    private void ejecutarDesdeVentana3D() {
        String fn = (String) cmbFuncion3D.getSelectedItem();
        String paleta = (String) cmbPaleta3D.getSelectedItem();
        try {
            double xMin = Double.parseDouble(txtXMin3D.getText().trim());
            double xMax = Double.parseDouble(txtXMax3D.getText().trim());
            double yMin = Double.parseDouble(txtYMin3D.getText().trim());
            double yMax = Double.parseDouble(txtYMax3D.getText().trim());
            double paso = Double.parseDouble(txtPaso3D.getText().trim());

            new Thread(() -> procesarYGraficar3D(dao, fn, xMin, xMax, yMin, yMax, paso, paleta)).start();
        } catch (NumberFormatException ex) {
            actualizarMensajeError("Error numérico en 3D: verifique límites de X, Y y Paso.");
        }
    }

    public static void procesarYGraficar3D(CalculoDAO dao, String fn, double xMin, double xMax,
                                           double yMin, double yMax, double paso, String paleta) {
        try {
            String rutaSalida = "bd" + File.separator + "grafica_3d.png";
            String rutaActual = "bd" + File.separator + "grafica_actual.png";

            String rutaGenerada = Graficador3D.generarSuperficie3D(fn, xMin, xMax, yMin, yMax, paso, rutaSalida, paleta);
            Graficador3D.generarSuperficie3D(fn, xMin, xMax, yMin, yMax, paso, rutaActual, paleta);

            String resumen = String.format("Superficie 3D generada: '%s' [X: %.1f a %.1f, Y: %.1f a %.1f, paleta: %s]",
                    fn, xMin, xMax, yMin, yMax, paleta);

            System.out.println("🌐 Superficie 3D guardada en: " + rutaGenerada);
            System.out.println("🌐 Copia rápida en: " + new File(rutaActual).getAbsolutePath());

            VisorGrafica visor = obtenerVisor(dao);
            if (visor != null) {
                visor.actualizarImagenDirecta(rutaGenerada, resumen);
            }
        } catch (Exception e) {
            System.out.println("ERROR al generar superficie 3D: " + e.getMessage());
            VisorGrafica visor = obtenerVisor(dao);
            if (visor != null) {
                visor.actualizarMensajeError("ERROR 3D: " + e.getMessage());
            }
        }
    }

    public void actualizarImagenDirecta(String rutaPng, String mensajeResumen) {
        SwingUtilities.invokeLater(() -> {
            try {
                File archivo = new File(rutaPng);
                if (archivo.exists()) {
                    panelImagen.setImagen(ImageIO.read(archivo));
                }
                lblEstado.setText(mensajeResumen);
                lblEstado.setForeground(new Color(22, 101, 52));
                lblRutaArchivo.setText("Renderizado en: " + archivo.getAbsolutePath());
                if (!isVisible()) {
                    setVisible(true);
                }
                toFront();
            } catch (Exception e) {
                actualizarMensajeError("Error al cargar imagen 3D: " + e.getMessage());
            }
        });
    }

    public void actualizarGrafica(String rutaPng, String mensajeResumen, String fn, Double valor, String modo,
                                  double xMin, double xMax, double paso) {
        SwingUtilities.invokeLater(() -> {
            try {
                File archivo = new File(rutaPng);
                if (archivo.exists()) {
                    panelImagen.setImagen(ImageIO.read(archivo));
                }
                if (fn != null) cmbFuncion2D.setSelectedItem(fn);
                if (modo != null) cmbModo2D.setSelectedItem(modo);
                if (valor != null) txtValor2D.setText(String.valueOf(valor));
                txtXMin2D.setText(String.valueOf(xMin));
                txtXMax2D.setText(String.valueOf(xMax));
                txtPaso2D.setText(String.valueOf(paso));

                lblEstado.setText(mensajeResumen);
                lblEstado.setForeground(new Color(22, 101, 52));
                lblRutaArchivo.setText("Guardado en SQLite y renderizado en: " + archivo.getAbsolutePath());

                if (!isVisible()) setVisible(true);
                toFront();
            } catch (Exception e) {
                actualizarMensajeError("Error al cargar imagen PNG: " + e.getMessage());
            }
        });
    }

    public void actualizarMensajeError(String mensajeError) {
        SwingUtilities.invokeLater(() -> {
            lblEstado.setText(mensajeError);
            lblEstado.setForeground(new Color(185, 28, 28));
            if (!isVisible()) setVisible(true);
            toFront();
        });
    }

    public static synchronized VisorGrafica obtenerVisor(CalculoDAO dao) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        if (instancia == null) {
            instancia = new VisorGrafica(dao);
        }
        return instancia;
    }

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

        if (valorPuntual != null && resultadoPuntual != null) {
            if (valorPuntual < xMin) xMin = valorPuntual - paso * 2;
            if (valorPuntual > xMax) xMax = valorPuntual + paso * 2;
        }

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
        if (v == (long) v) return String.format("%d", (long) v);
        return String.valueOf(v);
    }

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
                String msg = "Seleccione una función 2D o superficie 3D para ver la gráfica en tiempo real...";
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

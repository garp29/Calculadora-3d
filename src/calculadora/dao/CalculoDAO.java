package calculadora.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class CalculoDAO {
    // Ruta de conexión JDBC para SQLite asegurando la ruta de la base de datos
    private static final String URL_DB = "jdbc:sqlite:bd\\calculadora.db";

    static {
        try {
            // Cargar explícitamente el driver de SQLite (sqlite-jdbc)
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver JDBC de SQLite: " + e.getMessage());
        }
    }

    /**
     * Guarda un cálculo realizado (exitoso o fallido) en la tabla 'calculo' de SQLite.
     * Cumple con los requisitos R2 y R3 de persistencia y fallos.
     */
    public void guardarCalculo(int operacionId, double operandoA, Double operandoB, 
                               String modoAngulo, Double resultado, int exito, String mensajeError) {
        
        String sql = "INSERT INTO calculo (operacion_id, operando_a, operando_b, modo_angulo, resultado, exito, mensaje_error) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL_DB);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Activar llaves foráneas estrictas
            try (PreparedStatement pragma = conn.prepareStatement("PRAGMA foreign_keys = ON;")) {
                pragma.execute();
            }

            pstmt.setInt(1, operacionId);
            pstmt.setDouble(2, operandoA);
            
            if (operandoB != null) {
                pstmt.setDouble(3, operandoB);
            } else {
                pstmt.setNull(3, java.sql.Types.REAL);
            }

            if (modoAngulo != null) {
                pstmt.setString(4, modoAngulo);
            } else {
                pstmt.setNull(4, java.sql.Types.VARCHAR);
            }

            if (resultado != null) {
                pstmt.setDouble(5, resultado);
            } else {
                pstmt.setNull(5, java.sql.Types.REAL);
            }

            pstmt.setInt(6, exito); // 1 = Éxito, 0 = Fallido

            if (mensajeError != null) {
                pstmt.setString(7, mensajeError);
            } else {
                pstmt.setNull(7, java.sql.Types.VARCHAR);
            }

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al guardar el cálculo en SQLite: " + e.getMessage());
        }
    }

    /**
     * Guarda de forma transaccional una serie y todos sus puntos (x, y) en SQLite.
     * Cumple con los requisitos R5, R6 (puntos sin valor con y = NULL), G5 y Transacciones (todo o nada).
     *
     * @return El ID autogenerado de la serie en la tabla 'serie', o -1 si ocurrió un error.
     */
    public int guardarSerieConPuntos(String nombreSerie, int operacionId, double xMin, double xMax,
                                     double paso, String modoAngulo, List<Double[]> puntos) {
        String sqlSerie = "INSERT INTO serie (nombre, operacion_id, x_min, x_max, paso, modo_angulo) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlPunto = "INSERT INTO punto (serie_id, x, y) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL_DB)) {
            try (PreparedStatement pragma = conn.prepareStatement("PRAGMA foreign_keys = ON;")) {
                pragma.execute();
            }

            // Iniciar transacción: se guarda la serie completa con todos sus puntos o nada
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtSerie = conn.prepareStatement(sqlSerie, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement pstmtPunto = conn.prepareStatement(sqlPunto)) {

                pstmtSerie.setString(1, nombreSerie);
                pstmtSerie.setInt(2, operacionId);
                pstmtSerie.setDouble(3, xMin);
                pstmtSerie.setDouble(4, xMax);
                pstmtSerie.setDouble(5, paso);
                pstmtSerie.setString(6, modoAngulo);
                pstmtSerie.executeUpdate();

                int serieId = -1;
                try (ResultSet keys = pstmtSerie.getGeneratedKeys()) {
                    if (keys.next()) {
                        serieId = keys.getInt(1);
                    }
                }

                if (serieId <= 0) {
                    conn.rollback();
                    return -1;
                }

                for (Double[] pt : puntos) {
                    pstmtPunto.setInt(1, serieId);
                    pstmtPunto.setDouble(2, pt[0]);
                    if (pt[1] != null) {
                        pstmtPunto.setDouble(3, pt[1]);
                    } else {
                        pstmtPunto.setNull(3, java.sql.Types.REAL);
                    }
                    pstmtPunto.addBatch();
                }

                pstmtPunto.executeBatch();
                conn.commit();
                return serieId;

            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("Error en transacción de serie (Rollback aplicado): " + ex.getMessage());
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("Error de conexión al guardar la serie en SQLite: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Consulta el historial completo de cálculos realizados ordenados por fecha cronológica.
     */
    public void consultarHistorial() {
        String sql = "SELECT c.id, o.simbolo, c.operando_a, c.operando_b, c.resultado, c.exito, c.mensaje_error, c.creado_en " +
                     "FROM calculo c JOIN operacion o ON c.operacion_id = o.id ORDER BY c.creado_en DESC;";

        try (Connection conn = DriverManager.getConnection(URL_DB);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n--- HISTORIAL DE CÁLCULOS (SQLite) ---");
            while (rs.next()) {
                int id = rs.getInt("id");
                String simbolo = rs.getString("simbolo");
                double opA = rs.getDouble("operando_a");
                double opB = rs.getDouble("operando_b");
                boolean esExito = rs.getInt("exito") == 1;
                String res = esExito ? String.valueOf(rs.getDouble("resultado")) : "FALLIDO (" + rs.getString("mensaje_error") + ")";
                String fecha = rs.getString("creado_en");

                System.out.printf("[%d] Op: %s | A: %.2f | B: %.2f | Res/Estado: %s | Fecha: %s%n",
                        id, simbolo, opA, opB, res, fecha);
            }
            System.out.println("---------------------------------------\n");

        } catch (SQLException e) {
            System.err.println("Error al consultar el historial: " + e.getMessage());
        }
    }
}

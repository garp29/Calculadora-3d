package calculadora.modelo;

/**
 * Clase base abstracta para las operaciones básicas calculadas en Java.
 * Admite constructores flexibles y permite sobrescribir tanto calcular(x, y)
 * como ejecutar(x, y) para máxima compatibilidad polimórfica.
 */
public abstract class OperacionBasica implements IOperacion {

    protected final String simbolo;
    protected final String nombre;
    protected final String tipo;

    public OperacionBasica(String simbolo, String nombre) {
        this(simbolo, nombre, "BASICA");
    }

    public OperacionBasica(String simbolo, String nombre, String tipo) {
        if (simbolo == null || simbolo.isBlank()) {
            throw new IllegalArgumentException("El símbolo no puede estar vacío");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.simbolo = simbolo.trim();
        this.nombre = nombre.trim();
        this.tipo = (tipo != null && !tipo.isBlank()) ? tipo.trim() : "BASICA";
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
        return this.tipo;
    }

    @Override
    public double calcular(double a, double b) throws Exception {
        return ejecutar(a, b);
    }

    /**
     * Algoritmo de cálculo implementado por cada subclase aritmética.
     */
    protected double ejecutar(double a, double b) throws Exception {
        return 0.0;
    }
}

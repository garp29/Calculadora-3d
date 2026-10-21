package calculadora.modelo;

/**
 * Operación aritmética de multiplicación en Java (*).
 */
public class Multiplicacion extends OperacionBasica {

    public Multiplicacion() {
        super("*", "Multiplicación");
    }

    @Override
    protected double ejecutar(double a, double b) {
        return a * b;
    }
}

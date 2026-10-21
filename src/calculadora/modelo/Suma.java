package calculadora.modelo;

/**
 * Operación aritmética de suma en Java (+).
 */
public class Suma extends OperacionBasica {

    public Suma() {
        super("+", "Suma");
    }

    @Override
    protected double ejecutar(double a, double b) {
        return a + b;
    }
}

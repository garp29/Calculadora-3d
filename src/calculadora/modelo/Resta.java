package calculadora.modelo;

/**
 * Operación aritmética de resta en Java (-).
 */
public class Resta extends OperacionBasica {

    public Resta() {
        super("-", "Resta");
    }

    @Override
    protected double ejecutar(double a, double b) {
        return a - b;
    }
}

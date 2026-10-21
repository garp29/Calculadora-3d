package calculadora.modelo;

/**
 * Operación aritmética de división en Java.
 * Implementa la validación obligatoria contra indeterminaciones por división entre cero.
 * 
 * Requisitos asociados: G2 (herencia y polimorfismo), G6 (manejo de división por cero).
 */
public class Division extends OperacionBasica {

    /**
     * Inicializa la operación de división con su símbolo y nombre estándar.
     */
    public Division() {
        super("/", "División");
    }

    @Override
    protected double ejecutar(double a, double b) throws ArithmeticException {
        if (b == 0.0) {
            throw new ArithmeticException("División por cero no permitida");
        }
        return a / b;
    }
}

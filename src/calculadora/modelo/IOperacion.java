package calculadora.modelo;

/**
 * Contrato común para todas las operaciones soportadas por la calculadora.
 * Define la interfaz polimórfica que comparten las operaciones básicas (en Java)
 * y las operaciones trigonométricas (delegadas a Python).
 */
public interface IOperacion {

    /**
     * Símbolo representativo de la operación (ej. "+", "/", "sin", "tan").
     */
    String getSimbolo();

    /**
     * Nombre descriptivo de la operación (ej. "Suma", "División", "Seno").
     */
    String getNombre();

    /**
     * Categoría de la operación: "BASICA" o "TRIGONOMETRICA".
     */
    String getTipo();

    /**
     * Ejecución polimórfica del cálculo.
     * Para operaciones binarias: recibe operando 'a' y operando 'b'.
     * Para operaciones unarias (trigonométricas): recibe 'a' y se ignora 'b'.
     * 
     * @param a Primer operando.
     * @param b Segundo operando.
     * @return Resultado numérico del cálculo.
     * @throws Exception Si ocurre un error de dominio, división por cero o fallo en Python.
     */
    double calcular(double a, double b) throws Exception;
}

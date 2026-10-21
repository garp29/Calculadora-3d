package calculadora;

import calculadora.dao.CalculoDAO;
import calculadora.modelo.Division;
import calculadora.modelo.IOperacion;
import calculadora.modelo.OperacionBasica;
import calculadora.modelo.OperacionTrigonometrica;
import calculadora.vista.VisorGrafica;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CalculoDAO dao = new CalculoDAO();

        // Inicializar visor gráfico interactivo en tiempo real (si hay entorno gráfico disponible)
        VisorGrafica visor = VisorGrafica.obtenerVisor(dao);
        if (visor != null) {
            visor.setVisible(true);
        }

        while (true) {
            System.out.println("\n==========================================================");
            System.out.println("   CALCULADORA INTEGRADA (Java + Python + SQLite)");
            System.out.println("==========================================================");
            System.out.println("1. Operaciones Básicas (+, -, *, /)");
            System.out.println("2. Funciones Trigonométricas + Gráfica en Tiempo Real");
            System.out.println("3. Ver Historial de Cálculos (SQLite)");
            System.out.println("4. Salir");
            System.out.println("5. Graficar Serie Personalizada (rango xmin, xmax, paso)");
            System.out.println("----------------------------------------------------------");
            System.out.println("💡 Tip: También puedes escribir directo ej: 'sin 90' o 'cos 180'");
            System.out.print("Seleccione una opción o escriba la operación: ");

            if (!scanner.hasNextLine()) {
                break;
            }
            String entrada = scanner.nextLine().trim();
            if (entrada.isEmpty()) {
                continue;
            }

            // Si el usuario escribió directamente una expresión como "sin 90" o "sin 90°"
            if (intentarEjecutarExpresionDirecta(entrada, dao)) {
                continue;
            }

            int opcion;
            try {
                opcion = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("ERROR;Por favor, ingrese un número válido o una expresión como 'sin 90'.");
                continue;
            }

            if (opcion == 4) {
                System.out.println("¡Saliendo de la calculadora. Hasta luego!");
                if (visor != null) {
                    visor.dispose();
                }
                break;
            }

            if (opcion == 3) {
                dao.consultarHistorial();
                continue;
            }

            if (opcion == 1) {
                // Menú de operaciones básicas
                System.out.println("\n--- OPERACIONES BÁSICAS (Java) ---");
                System.out.println("1. Suma (+)");
                System.out.println("2. Resta (-)");
                System.out.println("3. Multiplicación (*)");
                System.out.println("4. División (/)");
                System.out.print("Elija la operación: ");

                int opBasica = Integer.parseInt(scanner.nextLine().trim());

                System.out.print("Ingrese el operando A: ");
                double a = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("Ingrese el operando B: ");
                double b = Double.parseDouble(scanner.nextLine().trim());

                IOperacion operacion = null;
                int operacionIdDb = 0;

                switch (opBasica) {
                    case 1:
                        operacion = new OperacionBasica("+", "Suma", "BASICA") {
                            @Override public double calcular(double x, double y) { return x + y; }
                        };
                        operacionIdDb = 1;
                        break;
                    case 2:
                        operacion = new OperacionBasica("-", "Resta", "BASICA") {
                            @Override public double calcular(double x, double y) { return x - y; }
                        };
                        operacionIdDb = 2;
                        break;
                    case 3:
                        operacion = new OperacionBasica("*", "Multiplicación", "BASICA") {
                            @Override public double calcular(double x, double y) { return x * y; }
                        };
                        operacionIdDb = 3;
                        break;
                    case 4:
                        operacion = new Division();
                        operacionIdDb = 4;
                        break;
                    default:
                        System.out.println("ERROR;Operación básica no válida.");
                        continue;
                }

                try {
                    double resultado = operacion.calcular(a, b);
                    System.out.println("Resultado: " + resultado);
                    // Guardar éxito en SQLite
                    dao.guardarCalculo(operacionIdDb, a, b, null, resultado, 1, null);
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                    // Guardar fallo en SQLite (Requisito R3)
                    dao.guardarCalculo(operacionIdDb, a, b, null, null, 0, e.getMessage());
                }

            } else if (opcion == 2) {
                // Menú de funciones trigonométricas (Python vía ProcessBuilder) + Gráfica en Tiempo Real
                System.out.println("\n--- FUNCIONES TRIGONOMÉTRICAS + GRÁFICA EN TIEMPO REAL ---");
                System.out.print("Ingrese la función (sin, cos, tan, asin, acos, atan): ");
                String fn = scanner.nextLine().trim().toLowerCase();

                System.out.print("Ingrese el valor o ángulo: ");
                String valorRaw = scanner.nextLine().trim().replace("°", "");
                double valor = Double.parseDouble(valorRaw);

                System.out.print("Modo de ángulo ('grados' o 'radianes' [por defecto grados]): ");
                String modo = scanner.nextLine().trim().toLowerCase();
                if (modo.isEmpty()) {
                    modo = "grados";
                }

                ejecutarTrigYGraficar(fn, valor, modo, dao);

            } else if (opcion == 5) {
                // Menú de serie personalizada y gráfica (Requisito R5, R6, G5)
                System.out.println("\n--- GRAFICAR SERIE PERSONALIZADA EN TIEMPO REAL ---");
                System.out.print("Ingrese la función (sin, cos, tan, asin, acos, atan): ");
                String fn = scanner.nextLine().trim().toLowerCase();

                System.out.print("Ingrese x mínimo (ej. -360): ");
                double xMin = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Ingrese x máximo (ej. 360): ");
                double xMax = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Ingrese el paso (ej. 15): ");
                double paso = Double.parseDouble(scanner.nextLine().trim());

                System.out.print("Modo de ángulo ('grados' o 'radianes' [por defecto grados]): ");
                String modo = scanner.nextLine().trim().toLowerCase();
                if (modo.isEmpty()) {
                    modo = "grados";
                }

                VisorGrafica.procesarYGraficarEnTiempoReal(dao, fn, null, xMin, xMax, paso, modo, false);

            } else {
                System.out.println("Opción inválida.");
            }
        }
        scanner.close();
    }

    /**
     * Calcula una función trigonométrica puntual, la registra en SQLite y genera/muestra
     * inmediatamente su gráfica en tiempo real resaltando el punto evaluado.
     */
    private static void ejecutarTrigYGraficar(String fn, double valor, String modo, CalculoDAO dao) {
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
            System.out.println("ERROR;Función trigonométrica desconocida.");
            return;
        }

        OperacionTrigonometrica trigOp = new OperacionTrigonometrica(fn, fn, modo);

        try {
            double resultado = trigOp.calcular(valor, 0);
            System.out.println("Resultado (" + modo + "): " + resultado);
            // Guardar éxito en SQLite
            dao.guardarCalculo(operacionIdDb, valor, null, modo, resultado, 1, null);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            // Guardar fallo en SQLite (Requisito R3 y G3)
            dao.guardarCalculo(operacionIdDb, valor, null, modo, null, 0, e.getMessage());
        }

        // Determinar rango óptimo por defecto según la función y el modo de ángulo
        double[] rango = obtenerRangoPorDefecto(fn, modo);
        VisorGrafica.procesarYGraficarEnTiempoReal(dao, fn, valor, rango[0], rango[1], rango[2], modo, false);
    }

    private static double[] obtenerRangoPorDefecto(String fn, String modo) {
        if ("asin".equals(fn) || "acos".equals(fn)) {
            return new double[]{-1.0, 1.0, 0.05};
        } else if ("atan".equals(fn)) {
            return new double[]{-10.0, 10.0, 0.5};
        } else if ("radianes".equals(modo)) {
            return new double[]{-6.28, 6.28, 0.25};
        } else {
            return new double[]{-360.0, 360.0, 15.0};
        }
    }

    /**
     * Permite escribir directamente expresiones como "sin 90", "sin 90°", "tan 45 rad" en el menú.
     */
    private static boolean intentarEjecutarExpresionDirecta(String entrada, CalculoDAO dao) {
        String limpia = entrada.trim().toLowerCase();
        String[] tokens = limpia.split("\\s+");
        if (tokens.length < 2) {
            return false;
        }

        String fn = tokens[0];
        if (!fn.equals("sin") && !fn.equals("cos") && !fn.equals("tan")
                && !fn.equals("asin") && !fn.equals("acos") && !fn.equals("atan")) {
            return false;
        }

        try {
            String numStr = tokens[1].replace("°", "");
            double valor = Double.parseDouble(numStr);
            String modo = "grados";
            if (tokens.length >= 3 && tokens[2].startsWith("rad")) {
                modo = "radianes";
            }
            ejecutarTrigYGraficar(fn, valor, modo, dao);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

```mermaid
flowchart TD
    Inicio(["Inicio"]) --> E1[/ "Elegir función, xmin, xmax, paso y modo de ángulo" /]
    E1 --> D1{"¿Existe la función trigonométrica?"}

    %% Validación de función, rango (xmin < xmax) y paso (paso > 0)
    D1 -- No --> P_ErrFn["Asignar error de función inexistente"]
    D1 -- Sí --> D2{"¿Es xmin menor que xmax?"}

    D2 -- No --> P_ErrRango["Asignar error de rango inválido"]
    D2 -- Sí --> D3{"¿Es el paso mayor que cero?"}

    D3 -- No --> P_ErrPaso["Asignar error de paso inválido"]

    %% Petición de puntos a Python (trig.py serie)
    D3 -- Sí --> P_PedirPy["Pedir serie de puntos a Python con trig.py"]
    P_PedirPy --> D4{"¿Generó Python la serie sin errores?"}

    D4 -- No --> P_ErrPy["Capturar mensaje de error de Python"]

    %% Guardado de la serie y los puntos en la Base de Datos
    D4 -- Sí --> BD1[(Guardar serie en la base de datos)]
    BD1 --> BD2[(Guardar puntos e indefinidos en la base de datos)]

    %% Generación de la gráfica leyendo desde la Base de Datos (graficar.py)
    BD2 --> P_InvGraf["Invocar graficar.py desde Java"]
    P_InvGraf --> BD3[(Leer puntos de la serie desde la base de datos)]
    BD3 --> P_GenPNG["Generar gráfica PNG con matplotlib"]
    P_GenPNG --> D5{"¿Se guardó el archivo PNG correctamente?"}

    D5 -- No --> P_ErrGraf["Asignar error de generación de gráfica"]
    D5 -- Sí --> S_Ok[/ "Mostrar gráfica generada al usuario" /]

    %% Camino unificado de salida de errores
    P_ErrFn --> S_Err[/ "Mostrar mensaje de error al usuario" /]
    P_ErrRango --> S_Err
    P_ErrPaso --> S_Err
    P_ErrPy --> S_Err
    P_ErrGraf --> S_Err

    %% Cierre único en Fin
    S_Ok --> Fin(["Fin"])
    S_Err --> Fin

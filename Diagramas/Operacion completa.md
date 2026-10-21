```mermaid
flowchart TD
    Inicio(["Inicio"]) --> Inicializar["Inicializar"]
    Inicializar --> BD_In[(Base de datos / Datos iniciales)]
    BD_In --> LecturaDatos[/ "Leer parámetros de entrada" /]

    LecturaDatos --> D_Modo{"¿Modo / Opción?"}

    %% Ramificaciones principales de funciones trigonométricas
    D_Modo --> D_TipoFn{"¿Tipo de función?"}

    D_TipoFn -- No --> ErrFn[/ "Asignar error de función" /]
    D_TipoFn -- Sí --> ValidarParam{"¿Validar parámetros?"}

    %% Rama Izquierda / Seno-Coseno u otras operaciones
    ValidarParam --> LeerDatosNum[/ "Leer datos numéricos" /]
    LeerDatosNum --> D_Opciones{"¿Evaluar condición?"}

    D_Opciones -- Seno --> CondSeno{"¿Caso Seno?"}
    D_Opciones -- Coseno --> CondCos["Procesar Coseno"]
    D_Opciones -- Tangente --> ResTan["resultado = tan(x)"]
    D_Opciones -- Otro --> ResOtro["resultado = valor"]

    CondSeno -- Sí --> ResSeno["resultado = a / b"]
    CondSeno -- No --> ResSenoAlt["resultado = b"]

    %% Rama Derecha / Otras funciones
    ValidarParam -- Alternativa --> LeerValTrig[/ "Leer valores trigonométricos" /]
    LeerValTrig --> ConvVal["Convertir a radianes / resolución"]
    ConvVal --> D_Cond2{"¿Condición secundaria?"}

    D_Cond2 -- Tangente --> CondTan{"¿Caso Tangente?"}
    D_Cond2 -- Secante --> ResSec["resultado = secante(x)"]

    CondTan -- Sí --> ResTanOK["resultado = valor"]
    CondTan -- No --> ResTanInf[/ "resultado: tangente indefinida" /]

    %% Convergencia de resultados hacia el procesamiento central
    ResSeno --> ProcesarValores[[Procesar resultados]]
    ResSenoAlt --> ProcesarValores
    ResCos["Procesar Coseno"] --> ProcesarValores
    ResTan --> ProcesarValores
    ResOtro --> ProcesarValores
    ResSec --> ProcesarValores
    ResTanOK --> ProcesarValores
    ResTanInf --> ProcesarValores
    ResSec --> ProcesarValores

    %% Almacenamiento en Base de Datos y control de bucle
    ProcesarValores --> BD_Res[(Guardar resultados en Base de Datos)]
    BD_Res --> D_Continuar{"¿Continuar operación?"}

    D_Continuar -- Sí --> LecturaDatos
    D_Continuar -- No --> BD_Fin[(Base de datos / Cierre)]

    %% Errores y Cierre
    ErrFn --> BD_Fin
    BD_Fin --> Fin(["Fin"])

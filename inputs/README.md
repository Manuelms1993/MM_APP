# Inputs

Este directorio contiene los JSON que se empaquetan como assets dentro de la APK.

## Estructura

```text
inputs/
├── food/
│   ├── comidas.json
│   └── cenas.json
├── plants/
│   ├── albahaca.json
│   ├── romero.json
│   └── ...
└── travel/
    ├── day_01_....json
    ├── day_02_....json
    └── hotels.json
```

## Uso en la aplicacion

- `plants/`: catalogo de plantas para la seccion `Plantas`
- `food/`: planes de comidas y cenas para la seccion `Comida`
- `travel/`: itinerario y hoteles para la seccion `Viaje`

## Carga de datos

- `plants/` y `food/` se usan como fallback embebido
- esas dos secciones pueden sincronizar despues contra un repositorio remoto de GitHub configurado por propiedades de Gradle
- `travel/` se consume solo desde los assets incluidos en la APK

Propiedades de configuracion remota:

```properties
plantsInputsRepositoryTreeUrl=https://github.com/Manuelms1993/MM_APP/tree/master/inputs/plants
foodInputsRepositoryTreeUrl=https://github.com/Manuelms1993/MM_APP/tree/master/inputs/food
```

## Notas de formato

### plants

Cada fichero representa una planta. El parser tolera campos extra, pero depende al menos de:

- `planta.fechaInicio`
- `planta.id` o nombre de fichero
- `planta.nombre`
- `planta.responsable` con valor `L` o `R`

Tambien soporta reglas de riego, abono, plagas, composicion de maceta y metadatos.

### food

Los ficheros `comidas.json` y `cenas.json` definen:

- `planType`
- `fechaInicio`
- `semanas`
- `dias`
- `opciones`
- `ingredientes`

### travel

Los ficheros diarios incluyen:

- identificador de dia
- fecha
- ciudad
- resumen
- segmentos
- sugerencias de comida
- notas
- recomendaciones
- enlaces

`hotels.json` incluye reservas, direcciones, dias asociados, estado y enlace de referencia.

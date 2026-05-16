# MM_APP
Aplicacion Android en Compose que agrupa tres herramientas en una sola APK:

- `Plantas`: seguimiento de cuidados, siembra e informacion de plantas.
- `Comida`: menu rotativo de comidas y cenas con lista de la compra.
- `Viaje`: guia visual de un itinerario con dias, hoteles y enlaces.

La app tiene un contenedor comun con un menu superior para cambiar de seccion sin salir de la misma aplicacion.

## Que hace cada seccion

### 1. Plantas

La seccion de plantas trabaja con un catalogo de ficheros JSON, genera acciones pendientes por fecha y guarda un historial en base de datos local.

Funciones principales:

- pestaña `Cuidados` con historial diario de mensajes generados
- generacion manual de pendientes con `Actualizar`
- sincronizacion remota de los JSON con `Sincronizar`
- filtro por responsable activo (`L` o `R`)
- pestaña `Plantacion` con plantas activas marcadas para siembra/recoleccion
- pestaña `Info` con ficha de cada planta
- pestaña `Tiempo` con datos meteorologicos para ajustar cuidados
- recordatorio diario con `WorkManager` a las 09:00 en zona `Europe/Madrid`

Persistencia y datos:

- Room guarda conversacion, tiempo y responsable activo
- los JSON se cargan desde `inputs/plants/` como fallback incluido en la APK
- si la sincronizacion remota funciona, los JSON descargados se cachean en el dispositivo
- si la sincronizacion falla, la app sigue usando cache o datos embebidos

Notas funcionales:

- el parser tolera campos extra en los JSON
- `fechaInicio` es obligatoria
- `responsable` solo admite `L` o `R`
- el calculo diario usa reglas de riego, abono, plagas y tiempo

### 2. Comida

La seccion de comida carga dos planes ciclicos, uno para `comidas` y otro para `cenas`, y genera el menu del dia y la lista de la compra.

Funciones principales:

- pestaña `Menu` con mensajes diarios de comida y cena
- generacion manual con `Actualizar`
- sincronizacion remota con `Sincronizar`
- auto-refresco silencioso mientras la pestaña de menu esta visible
- notificaciones diarias de comida y cena
- pestaña `Compra` con lista agregada de ingredientes

Reglas relevantes:

- los planes se leen desde `inputs/food/comidas.json` y `inputs/food/cenas.json`
- cada plan define una fecha de inicio y semanas con opciones por dia
- el motor rota automaticamente sobre el ciclo completo
- la lista de la compra se calcula desde hoy hasta el sabado objetivo
- si hoy es miercoles o mas tarde, el sabado objetivo pasa a la semana siguiente

Notificaciones:

- comida a las `10:00`
- cena a las `18:00`
- reprogramadas con `AlarmManager`

### 3. Viaje

La seccion de viaje es una guia local basada en JSON embebidos en la APK.

Funciones principales:

- pestaña `Dias` con tarjetas expandibles por jornada
- bloques por alojamiento, tramos del dia, comida, notas, recomendaciones y enlaces
- pestaña `Hoteles` con resumen de reservas
- apertura de enlaces externos desde la propia UI

Datos:

- los dias se leen desde `inputs/travel/day_*.json`
- los hoteles se leen desde `inputs/travel/hotels.json`
- no hay sincronizacion remota para esta seccion
- el repositorio usa cache en memoria durante la sesion para no reprocesar el JSON continuamente

## Arquitectura

Estructura principal:

- `app/src/main/kotlin/shell/`: contenedor comun, `MainActivity` y composable raiz
- `app/src/main/kotlin/app1/`: modulo funcional de plantas
- `app/src/main/kotlin/app2/`: modulo funcional de comida
- `app/src/main/kotlin/app3/`: modulo funcional de viaje
- `app/src/test/kotlin/app1/`: tests unitarios de plantas
- `app/src/test/kotlin/app2/`: tests unitarios de comida
- `inputs/`: datos JSON embebidos como assets
- `app/legacy_src/`: codigo anterior conservado como referencia, no es la fuente principal actual

Patrones usados:

- UI en Jetpack Compose
- estado con `ViewModel` + `StateFlow`
- persistencia local con Room
- trabajos en segundo plano con WorkManager y AlarmManager
- parseo de JSON con `kotlinx.serialization`
- sincronizacion remota contra GitHub Contents API para plantas y comida

## Configuracion de entradas remotas

La app de plantas y la de comida usan una URL de arbol de GitHub para resolver el endpoint publico de contenidos.

Propiedades soportadas:

- `plantsInputsRepositoryTreeUrl`
- `foodInputsRepositoryTreeUrl`

Se pueden definir en `gradle.properties` local o por linea de comandos con `-P`.

Ejemplo:

```properties
plantsInputsRepositoryTreeUrl=https://github.com/example/mm-app-inputs/tree/main/inputs/plants
foodInputsRepositoryTreeUrl=https://github.com/example/mm-app-inputs/tree/main/inputs/food
```

Si no se configuran, el build usa placeholders genericos. En ese caso la app sigue funcionando con los assets embebidos, pero la sincronizacion remota no sera util hasta apuntar a un repositorio real.

## Compilar y ejecutar

Requisitos:

- JDK 17
- Android SDK con build tools `34.0.0`

Comandos:

```bash
./gradlew test
./gradlew assembleDebug
./gradlew assembleRelease
```

La app empaqueta `inputs/` como assets desde este bloque de Gradle:

- `inputs/plants/`
- `inputs/food/`
- `inputs/travel/`

## Firma local de release

Para compilar una release firmada en local, el proyecto admite:

- `signing/release-keystore.properties`
- el fichero `.jks` bajo `signing/`

La carpeta `signing/` esta ignorada por Git.

Variables soportadas en local/CI:

- `ANDROID_KEYSTORE_FILE`
- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

## CI y release

Hay dos workflows:

- `Android CI`: ejecuta tests y construye un APK instalable para pull requests a `master`
- `Android Release`: calcula version, ejecuta tests, genera APK firmada y publica release al hacer push a `master`

La configuracion de version de release vive en `.github/release.properties`.

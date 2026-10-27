# ControlGastos

Aplicación Android para registrar y consultar tus gastos personales en quetzales (Q). Los datos se guardan en el teléfono, así que funciona sin internet.

Proyecto del curso de Programación 2, Universidad Mariano Gálvez de Guatemala (UMG).

## Autor

- **Nombre:** Keiry Anna Lucía Pozuelos Zelada
- **Carnet:** 0910-25-18169

## Funciones

- Ver todos los gastos en una lista, con el total gastado y la cantidad de gastos en el encabezado.
- Registrar un gasto nuevo con descripción, monto y categoría.
- Editar un gasto existente tocándolo en la lista.
- Eliminar un gasto, con confirmación antes de borrarlo.
- Pantalla **Escritorio** con el balance (ingresos menos gastos), el total de gastos, el total de ingresos y los gastos por categoría. Se actualiza sola cuando cambian los datos.
- Guardado permanente en una base de datos local (SQLite con Room). Al actualizar la app, los gastos ya guardados se conservan.
- Modo claro y modo oscuro automáticos.

## Flujo básico de la app

1. **Lista de gastos (pantalla principal).** El encabezado muestra el total gastado. Cada gasto aparece como una tarjeta con su categoría, fecha y monto. Si todavía no hay gastos, se muestra un mensaje que invita a crear el primero.
2. **Nuevo gasto.** El botón dorado «Nuevo gasto» abre el formulario. Se escribe la descripción y el monto, se elige una categoría y se toca «Guardar gasto». La app valida los datos y vuelve a la lista.
3. **Editar un gasto.** Al tocar un gasto de la lista se abre el mismo formulario con sus datos. Se cambia lo necesario y se toca «Guardar cambios».
4. **Escritorio.** El botón «Escritorio» del encabezado abre el resumen con el balance, los gastos, los ingresos y el desglose por categoría. La flecha de la esquina regresa a la lista.
5. **Eliminar un gasto.** Dentro del modo editar, «Eliminar gasto» pide confirmación y borra el gasto. Esta acción no se puede deshacer.

## Tecnologías

| Área | Herramienta |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Vistas XML con View Binding, Material Components 3 y RecyclerView; la pantalla Escritorio usa Jetpack Compose |
| Base de datos | Room 2.8.5 (SQLite), procesado con KSP |
| Concurrencia | Corrutinas de Kotlin (`suspend` y `lifecycleScope`) |
| Reglas de negocio | Librería propia `gastos-core` (`umg.edu.gt.progra2:gastos-core:1.1.0`) |
| Compilación | Android Gradle Plugin 9.1.1, Gradle 9.3.1 |
| Versiones de Android | minSdk 24, targetSdk 36 |

## Estructura del proyecto

```
app/src/main/java/umg/edu/gt/controlgastos/
├── ControlGastosApp.kt            Se ejecuta al iniciar la app y prepara las dependencias
├── Dependencias.kt                Objeto compartido con ControlGastos y la base de datos
├── MainActivity.kt                Pantalla principal: total y lista de gastos
├── FormularioGastoActivity.kt     Pantalla para crear, editar y eliminar un gasto
├── EscritorioActivity.kt          Pantalla Escritorio (Compose): balance, gastos, ingresos y categorías
├── GastoAdapter.kt                Conecta la lista de gastos con el RecyclerView
├── ui/
│   └── EscritorioDashboard.kt     Tarjetas del Escritorio (Composable)
└── datos/
    ├── AppDatabase.kt             Base de datos Room (versión 2, con migración desde la 1)
    ├── GastoDao.kt                Consultas SQL; los totales del Escritorio se leen como Flow
    ├── GastoEntity.kt             Tabla «gastos» (gastos e ingresos) y conversión desde/hacia Gasto
    └── GastoRepositoryRoom.kt     Implementación del repositorio sobre Room
```

La app separa responsabilidades en capas: las pantallas hablan con `ControlGastos` (reglas de negocio de `gastos-core`), que usa un repositorio. Ese repositorio está implementado con Room, por lo que la librería no depende de Android.

## Cómo ejecutarlo

### Requisitos

- Android Studio (versión reciente, compatible con AGP 9.1.1) con JDK 11 o superior.
- Un emulador o un teléfono Android 7.0 (API 24) o superior.
- La librería `gastos-core` 1.1.0 publicada en el repositorio Maven local (`~/.m2`).

### Pasos

1. Clona el repositorio:
   ```bash
   git clone <link de GitHub>
   ```
2. Abre la carpeta en Android Studio y espera a que termine la sincronización de Gradle (necesita internet la primera vez).
3. Si Gradle no encuentra `umg.edu.gt.progra2:gastos-core:1.1.0`, publícala primero desde su proyecto con `./gradlew publishToMavenLocal` y vuelve a sincronizar. El proyecto ya incluye `mavenLocal()` entre sus repositorios.
4. Elige un dispositivo y presiona **Run**.

### Generar el APK

En Android Studio: **Build > Build APK(s)**. El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Enlaces

- **Repositorio:** <link de GitHub>
- **APK y documentación adicional (Drive):** <enlace de Drive>

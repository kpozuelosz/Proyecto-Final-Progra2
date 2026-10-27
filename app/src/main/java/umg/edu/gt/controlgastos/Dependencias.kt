package umg.edu.gt.controlgastos

import android.content.Context
import androidx.room.Room
import edu.umg.programacion2.gastos.servicio.ControlGastos
import umg.edu.gt.controlgastos.datos.AppDatabase
import umg.edu.gt.controlgastos.datos.GastoDao
import umg.edu.gt.controlgastos.datos.GastoRepositoryRoom

/**
 * Las piezas que TODA la app comparte.
 *
 * object = una clase de la que existe UN SOLO objeto. Las dos pantallas usan
 * Dependencias.control y ven los MISMOS gastos (Clase 13).
 *
 * Semana 4: para abrir la base de datos Room necesita un Context (saber "que app soy" y
 * donde esta su carpeta privada). Un object no tiene Context, asi que lo recibe UNA vez
 * en iniciar(), que se llama desde ControlGastosApp al arrancar la app.
 */
object Dependencias {

    // lateinit: "le doy valor despues, antes de usarla". private set: solo se asigna aqui adentro.
    lateinit var control: ControlGastos
        private set

    // El DAO directo: lo usa el Escritorio para escuchar los totales (Flow) sin pasar por ControlGastos.
    lateinit var gastoDao: GastoDao
        private set

    fun iniciar(context: Context) {
        // 1. Abre (o crea la primera vez) el archivo gastos.db en la carpeta privada de la app.
        //    addMigrations: al pasar de la version 1 a la 2 de la tabla, conserva los gastos ya guardados.
        val baseDeDatos = Room.databaseBuilder(context, AppDatabase::class.java, "gastos.db")
            .addMigrations(AppDatabase.MIGRACION_1_2)
            .build()
        gastoDao = baseDeDatos.gastoDao()

        // 2. EL CAMBIO DE LA SEMANA 4: antes era ControlGastos(GastoRepositoryEnMemoria()).
        control = ControlGastos(GastoRepositoryRoom(gastoDao))
    }
}

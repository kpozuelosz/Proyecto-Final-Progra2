package umg.edu.gt.controlgastos.datos

import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.umg.programacion2.gastos.modelo.Categoria
import edu.umg.programacion2.gastos.modelo.Gasto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Una FILA de la tabla "gastos" en SQLite. Ahora guarda gastos E ingresos.
 *
 * Gasto (libreria gastos-core) no conoce Android, Room ni los ingresos. Por eso esta clase
 * "espejo" convierte de ida y vuelta:
 *
 *   Gasto (libreria)  <-- aGasto() / desde() -->  GastoEntity (tabla SQLite)
 */
@Entity(tableName = "gastos")
data class GastoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val descripcion: String,
    val monto: Double, // Monto en Quetzales
    // Se guarda el NOMBRE del enum Categoria (ej. "ALIMENTACION"): es el valor mas estable.
    // No se guarda toString() ("Alimentación") porque puede cambiar.
    val categoria: String,
    val esIngreso: Boolean = false, // false = Gasto, true = Ingreso
    val fechaTimestamp: Long = System.currentTimeMillis()
) {
    /** Fila de la tabla -> objeto de la libreria. La fecha vuelve a ser "yyyy-MM-dd". */
    fun aGasto(): Gasto = Gasto(
        id = id.toInt(),
        descripcion = descripcion,
        monto = monto,
        categoria = categoriaDesdeTexto(categoria),
        fecha = formatoFecha().format(Date(fechaTimestamp))
    )

    companion object {
        /** Objeto de la libreria -> fila de la tabla (siempre como gasto, no como ingreso). */
        fun desde(gasto: Gasto): GastoEntity = GastoEntity(
            id = gasto.id.toLong(),
            descripcion = gasto.descripcion,
            monto = gasto.monto,
            categoria = gasto.categoria.name,   // Categoria.ALIMENTACION -> "ALIMENTACION"
            esIngreso = false,
            fechaTimestamp = fechaATimestamp(gasto.fecha)
        )

        /**
         * Texto guardado -> Categoria. Acepta el nombre del enum ("ALIMENTACION") y tambien el
         * texto visible ("Alimentación"). Si no coincide con ninguna, usa OTROS en vez de cerrar la app.
         */
        fun categoriaDesdeTexto(texto: String): Categoria =
            Categoria.entries.firstOrNull { it.name == texto || it.nombreVisible == texto }
                ?: Categoria.OTROS

        /** Texto para mostrar en pantalla: "ALIMENTACION" -> "Alimentación". Si no se conoce, el texto tal cual. */
        fun nombreVisible(texto: String): String =
            Categoria.entries.firstOrNull { it.name == texto || it.nombreVisible == texto }
                ?.nombreVisible ?: texto

        /** "2026-10-24" -> milisegundos. Si el texto no es una fecha valida, usa el momento actual. */
        private fun fechaATimestamp(fecha: String): Long =
            runCatching {
                formatoFecha().apply { isLenient = false }.parse(fecha)?.time
            }.getOrNull() ?: System.currentTimeMillis()

        // SimpleDateFormat no es seguro entre hilos: se crea uno nuevo en cada uso.
        private fun formatoFecha() = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }
}

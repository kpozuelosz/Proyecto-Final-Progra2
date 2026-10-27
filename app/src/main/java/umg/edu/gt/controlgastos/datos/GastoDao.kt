package umg.edu.gt.controlgastos.datos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Resultado de agrupar los gastos por categoria (lo llena Room con el SELECT de abajo). */
data class CategoriaSum(
    val categoria: String,
    val total: Double
)

/**
 * DAO = Data Access Object: el MISMO patron que usamos con JDBC y MySQL (Clase 5).
 * La diferencia: aqui solo escribimos la INTERFACE. Room genera la clase que la implementa
 * (GastoDao_Impl) al compilar.
 *
 * Hay dos estilos de funciones:
 *  - suspend: se ejecutan una vez y devuelven el resultado (las usa el repositorio).
 *  - Flow:    se quedan "escuchando" la tabla y emiten un valor nuevo cada vez que cambia
 *             (las usa el Escritorio, para que los totales se actualicen solos).
 */
@Dao
interface GastoDao {

    // ---------- Lo que usa el repositorio (ControlGastos) ----------

    // Devuelve el id que SQLite genero para la fila nueva (Long).
    @Insert
    suspend fun insertar(gasto: GastoEntity): Long

    // Solo gastos: los ingresos no deben aparecer en la lista ni sumar al total gastado.
    // Ordenamos por id (orden de llegada); el "mas reciente primero" lo decide ControlGastos.listar().
    @Query("SELECT * FROM gastos WHERE esIngreso = 0 ORDER BY id")
    suspend fun listar(): List<GastoEntity>

    // :id se reemplaza por el parametro id. Es el "?" del PreparedStatement de JDBC.
    @Query("SELECT * FROM gastos WHERE id = :id")
    suspend fun buscarPorId(id: Int): GastoEntity?

    // Busca la fila por la llave primaria y la reemplaza. Devuelve cuantas filas cambio.
    @Update
    suspend fun actualizar(gasto: GastoEntity): Int

    // Devuelve cuantas filas borro (0 si el id no existia).
    @Query("DELETE FROM gastos WHERE id = :id")
    suspend fun eliminar(id: Int): Int

    // ---------- Lo nuevo: Escritorio (gastos e ingresos) ----------

    @Insert
    suspend fun insertarGasto(gasto: GastoEntity)

    @Query("SELECT * FROM gastos ORDER BY fechaTimestamp DESC")
    fun obtenerTodosLosGastos(): Flow<List<GastoEntity>>

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos WHERE esIngreso = 0")
    fun obtenerTotalGastos(): Flow<Double>

    @Query("SELECT COALESCE(SUM(monto), 0.0) FROM gastos WHERE esIngreso = 1")
    fun obtenerTotalIngresos(): Flow<Double>

    @Query("SELECT categoria, SUM(monto) as total FROM gastos WHERE esIngreso = 0 GROUP BY categoria")
    fun obtenerGastosPorCategoria(): Flow<List<CategoriaSum>>
}

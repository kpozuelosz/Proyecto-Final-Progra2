package umg.edu.gt.controlgastos.datos

import edu.umg.programacion2.gastos.datos.GastoRepository
import edu.umg.programacion2.gastos.modelo.Gasto

/**
 * SEGUNDA implementacion del contrato GastoRepository (la primera es GastoRepositoryEnMemoria).
 *
 * IMPORTANTE: ControlGastos y las pantallas solo conocen la interface GastoRepository.
 * Por eso podemos cambiar la lista en memoria por SQLite sin tocarlos (la "O" de SOLID:
 * abierto para extender, cerrado para modificar).
 *
 * Esta clase es un "traductor": recibe y devuelve Gasto (libreria), y le habla al DAO
 * con GastoEntity (tabla).
 */
class GastoRepositoryRoom(private val dao: GastoDao) : GastoRepository {

    override suspend fun agregar(gasto: Gasto): Gasto {
        // 1. Forzamos id = 0 para que SQLite genere el id (autoGenerate).
        val idNuevo = dao.insertar(GastoEntity.desde(gasto.copy(id = 0)))
        // 2. Devolvemos el gasto con su id real, igual que la version en memoria.
        //    Cuidado: el DAO devuelve Long y Gasto usa Int -> toInt().
        return gasto.copy(id = idNuevo.toInt())
    }

    override suspend fun listar(): List<Gasto> {
        // Convertimos cada fila (GastoEntity) en un Gasto de la libreria.
        val gastos = mutableListOf<Gasto>()
        for (fila in dao.listar()) {
            gastos.add(fila.aGasto())
        }
        return gastos
    }

    override suspend fun buscarPorId(id: Int): Gasto? {
        // ?. = "si no es null, llama a aGasto(); si es null, devuelve null" (Clase 11).
        return dao.buscarPorId(id)?.aGasto()
    }

    // El contrato pide Boolean: "true si existia". El DAO devuelve cuantas filas cambio.
    override suspend fun actualizar(gasto: Gasto): Boolean =
        dao.actualizar(GastoEntity.desde(gasto)) > 0

    override suspend fun eliminar(id: Int): Boolean = dao.eliminar(id) > 0
}

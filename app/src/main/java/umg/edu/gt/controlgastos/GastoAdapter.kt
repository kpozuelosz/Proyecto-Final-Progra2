package umg.edu.gt.controlgastos

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ArrayRes
import androidx.recyclerview.widget.RecyclerView
import edu.umg.programacion2.gastos.modelo.Gasto
import edu.umg.programacion2.gastos.servicio.formatearQuetzales
import umg.edu.gt.controlgastos.databinding.ItemGastoBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * El "puente" entre la lista de datos (List<Gasto>) y las filas que se ven en pantalla.
 *
 * alTocarGasto es una FUNCION que recibe la Activity. El Adapter no sabe que pasa al tocar
 * un gasto (abrir el formulario, borrar, ...): solo avisa. La Activity decide.
 */
class GastoAdapter(
    private val alTocarGasto: (Gasto) -> Unit
) : RecyclerView.Adapter<GastoAdapter.GastoViewHolder>() {

    // Los datos que se muestran. Empieza vacia; la Activity la llena con mostrar(...).
    private var gastos: List<Gasto> = emptyList()

    // Colores de los avatares (ver res/values/arrays.xml). Se leen una sola vez.
    private var fondos: IntArray = IntArray(0)
    private var letras: IntArray = IntArray(0)

    // La fecha viene guardada como "2026-10-24"; en pantalla se muestra "24 oct 2026".
    private val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val formatoSalida = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("es-GT"))

    /** Guarda las vistas de UNA fila para poder reutilizarla (reciclarla). */
    class GastoViewHolder(val binding: ItemGastoBinding) : RecyclerView.ViewHolder(binding.root)

    // 1. ¿Cuantas filas hay?
    override fun getItemCount(): Int = gastos.size

    // 2. Crear una fila NUEVA. RecyclerView lo llama solo unas pocas veces.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GastoViewHolder {
        if (fondos.isEmpty()) {
            fondos = leerColores(parent.context, R.array.avatar_fondos)
            letras = leerColores(parent.context, R.array.avatar_letras)
        }
        // Cuidado: el ultimo parametro va en false; RecyclerView agrega la fila por su cuenta.
        val binding = ItemGastoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GastoViewHolder(binding)
    }

    // 3. Poner los datos del gasto "position" en una fila (nueva o reciclada).
    override fun onBindViewHolder(holder: GastoViewHolder, position: Int) {
        val gasto = gastos[position]
        val b = holder.binding

        b.tvDescripcion.text = gasto.descripcion
        b.tvDetalle.text = "${gasto.categoria} · ${fechaLegible(gasto.fecha)}"
        b.tvMonto.text = formatearQuetzales(gasto.monto)

        // Cada categoria siempre tiene el mismo color: su posicion en el enum decide cual.
        val color = gasto.categoria.ordinal % fondos.size
        b.tvAvatar.text = gasto.categoria.toString().take(1).uppercase()
        b.tvAvatar.setTextColor(letras[color])
        b.tvAvatar.backgroundTintList = ColorStateList.valueOf(fondos[color])

        b.root.setOnClickListener { alTocarGasto(gasto) }
    }

    /** Reemplaza los datos y le pide a RecyclerView que redibuje la lista. */
    @SuppressLint("NotifyDataSetChanged") // forma simple; la mejorada es ListAdapter + DiffUtil
    fun mostrar(nuevos: List<Gasto>) {
        gastos = nuevos
        notifyDataSetChanged()
    }

    /** "2026-10-24" -> "24 oct 2026". Si la fecha no tiene ese formato, se muestra tal cual. */
    private fun fechaLegible(fecha: String): String {
        val date = runCatching { formatoEntrada.parse(fecha) }.getOrNull() ?: return fecha
        return formatoSalida.format(date)
    }

    private fun leerColores(context: Context, @ArrayRes id: Int): IntArray {
        val tipos = context.resources.obtainTypedArray(id)
        val colores = IntArray(tipos.length()) { tipos.getColor(it, 0) }
        tipos.recycle()
        return colores
    }
}

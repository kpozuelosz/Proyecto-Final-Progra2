package umg.edu.gt.controlgastos

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.umg.programacion2.gastos.modelo.Categoria
import edu.umg.programacion2.gastos.modelo.Gasto
import edu.umg.programacion2.gastos.servicio.formatearQuetzales
import kotlinx.coroutines.launch
import umg.edu.gt.controlgastos.databinding.ActivityFormularioGastoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de formulario. Tiene DOS modos:
 *  - CREAR:  se abre sin extra (boton "+" de la lista).
 *  - EDITAR: se abre con el extra EXTRA_ID_GASTO (al tocar un gasto de la lista).
 */
class FormularioGastoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormularioGastoBinding

    // IMPORTANTE: el MISMO control que usa la lista (ver Dependencias.kt).
    private val control = Dependencias.control

    // null = modo CREAR. Si tiene un Gasto = modo EDITAR (es el gasto original).
    private var gastoEditando: Gasto? = null

    // Convierte "16.dp" en pixeles segun la densidad de la pantalla.
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Inflar el layout con View Binding (igual que en la Clase 12).
        binding = ActivityFormularioGastoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        respetarBarrasDelSistema()

        // 2. El enum alimenta los chips de categoria (uno por valor, el primero queda marcado).
        crearChipsDeCategoria()

        // El error rojo de un campo desaparece apenas la persona empieza a corregirlo.
        binding.etMonto.doOnTextChanged { _, _, _, _ -> binding.tilMonto.error = null }
        binding.etDescripcion.doOnTextChanged { _, _, _, _ -> binding.tilDescripcion.error = null }
        binding.btnVolver.setOnClickListener { finish() }

        // 3. ¿Nos enviaron un id? 0 = no vino nada -> modo CREAR.
        val id = intent.getIntExtra(EXTRA_ID_GASTO, 0)
        if (id != 0) {
            lifecycleScope.launch { cargarGasto(id) }
        }

        binding.btnGuardar.setOnClickListener { guardar() }
        binding.btnEliminar.setOnClickListener { confirmarEliminacion() }
    }

    /** Modo EDITAR: busca el gasto y llena los campos con sus datos. */
    private suspend fun cargarGasto(id: Int) {
        val gasto = control.buscarPorId(id)
        if (gasto == null) {
            Toast.makeText(this, "No existe el gasto #$id", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        gastoEditando = gasto

        binding.tvTitulo.text = "Editar gasto"
        binding.btnGuardar.text = "Guardar cambios"
        binding.btnEliminar.visibility = View.VISIBLE
        // Cuidado: un EditText se llena con setText(...), no con "text = ...".
        binding.etDescripcion.setText(gasto.descripcion)
        binding.etMonto.setText(gasto.monto.toString())
        // Marca el chip cuya categoria coincide con la del gasto.
        seleccionarCategoria(gasto.categoria)
    }

    /** Un chip por cada valor del enum Categoria. El texto es el mismo que mostraba el Spinner. */
    private fun crearChipsDeCategoria() {
        Categoria.entries.forEach { categoria ->
            val chip = layoutInflater.inflate(R.layout.item_chip_categoria, binding.chipsCategoria, false) as Chip
            chip.id = View.generateViewId()
            chip.text = categoria.toString()
            chip.tag = categoria   // guardamos el enum en el chip para leerlo al guardar
            binding.chipsCategoria.addView(chip)
        }
        seleccionarCategoria(Categoria.entries.first())
    }

    private fun seleccionarCategoria(categoria: Categoria) {
        for (i in 0 until binding.chipsCategoria.childCount) {
            val chip = binding.chipsCategoria.getChildAt(i) as Chip
            if (chip.tag == categoria) chip.isChecked = true
        }
    }

    /** La categoria del chip marcado. selectionRequired garantiza que siempre hay uno. */
    private fun categoriaSeleccionada(): Categoria? {
        val chip = binding.chipsCategoria.findViewById<Chip>(binding.chipsCategoria.checkedChipId)
        return chip?.tag as? Categoria
    }

    private fun guardar() {
        // 1. Leer y revisar el monto (igual que en el Incremento 2).
        val monto = binding.etMonto.text.toString().toDoubleOrNull()
        if (monto == null) {
            binding.tilMonto.error = "Ingresa un monto válido"
            return
        }
        val descripcion = binding.etDescripcion.text.toString().trim()
        val categoria = categoriaSeleccionada() ?: return

        // 2. Copiamos la propiedad a una variable local: dentro del if Kotlin ya sabe que no es null.
        val original = gastoEditando

        lifecycleScope.launch {
            // 3. if devuelve un valor: el mensaje de error (o null) de registrar o de actualizar.
            val error = if (original == null) {
                control.registrar(
                    Gasto(descripcion = descripcion, monto = monto, categoria = categoria, fecha = fechaDeHoy())
                )
            } else {
                // copy() conserva el id y la fecha originales; solo cambia lo editado.
                control.actualizar(
                    original.copy(descripcion = descripcion, monto = monto, categoria = categoria)
                )
            }

            if (error != null) {
                Toast.makeText(this@FormularioGastoActivity, error, Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 4. Todo bien: cerramos esta pantalla y Android muestra otra vez la lista.
            // IMPORTANTE: NO se abre MainActivity con startActivity (apilaria una segunda lista).
            Toast.makeText(this@FormularioGastoActivity, "Gasto guardado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /** Pregunta antes de borrar: eliminar no se puede deshacer. */
    private fun confirmarEliminacion() {
        val gasto = gastoEditando ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar gasto")
            .setMessage("¿Eliminar \"${gasto.descripcion}\" (${formatearQuetzales(gasto.monto)})?\nEsta acción no se puede deshacer.")
            // El bloque se ejecuta SOLO si el usuario toca "Eliminar".
            .setPositiveButton("Eliminar") { _, _ -> eliminar(gasto.id) }
            // null = solo cerrar el dialogo, sin hacer nada.
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminar(id: Int) {
        lifecycleScope.launch {
            if (control.eliminar(id)) {
                Toast.makeText(this@FormularioGastoActivity, "Gasto eliminado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@FormularioGastoActivity, "No existe el gasto #$id", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Cuidado: java.time.LocalDate necesita API 26; con minSdk 24 usamos SimpleDateFormat.
    private fun fechaDeHoy(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    /**
     * Receta: con targetSdk 35+ la app se dibuja DEBAJO de las barras del sistema.
     * El encabezado baja lo que mide la barra de estado; la barra de botones sube lo que
     * miden la barra de navegacion o el teclado (ime), lo que sea mas alto.
     */
    private fun respetarBarrasDelSistema() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            binding.header.updatePadding(top = barras.top + 12.dp)
            binding.barraAcciones.updatePadding(bottom = barras.bottom + 16.dp)
            insets
        }
    }

    // companion object = "static" de Java. La lista usa esta constante para enviar el id.
    companion object {
        const val EXTRA_ID_GASTO = "id_gasto"
    }
}

package umg.edu.gt.controlgastos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.umg.programacion2.gastos.servicio.formatearQuetzales
import kotlinx.coroutines.launch
import umg.edu.gt.controlgastos.databinding.ActivityMainBinding

/** Pantalla principal: el total gastado en el encabezado, la lista de gastos y el botón "Nuevo gasto". */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: GastoAdapter

    // IMPORTANTE: ya NO se crea aqui; se usa el compartido (ver Dependencias.kt).
    private val control = Dependencias.control

    // Convierte "16.dp" en pixeles segun la densidad de la pantalla.
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Inflar el layout (igual que en la Clase 12).
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        respetarBarrasDelSistema()

        // 2. Crear el Adapter. El bloque { } se ejecuta cuando el usuario toca un gasto.
        adapter = GastoAdapter { gasto -> abrirFormulario(gasto.id) }

        // 3. Conectar la lista. Cuidado: sin LayoutManager la lista queda en blanco y sin error.
        binding.rvGastos.layoutManager = LinearLayoutManager(this)
        binding.rvGastos.adapter = adapter

        // 4. El boton "Nuevo gasto" abre el formulario vacio (sin id).
        binding.btnNuevo.setOnClickListener { abrirFormulario(null) }

        // 5. El boton "Escritorio" abre el resumen (balance, ingresos y categorias).
        binding.btnEscritorio.setOnClickListener {
            startActivity(Intent(this, EscritorioActivity::class.java))
        }
    }

    // IMPORTANTE: onResume se ejecuta al abrir la pantalla Y cada vez que regresamos del
    // formulario. onCreate NO se vuelve a ejecutar al regresar.
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { cargarGastos() }
    }

    private suspend fun cargarGastos() {
        val gastos = control.listar()
        adapter.mostrar(gastos)

        // El encabezado muestra el total grande y, debajo, cuantos gastos hay.
        binding.tvTotal.text = formatearQuetzales(gastos.sumOf { it.monto })
        binding.tvCantidad.text = when (gastos.size) {
            0 -> "Sin gastos registrados"
            1 -> "1 gasto registrado"
            else -> "${gastos.size} gastos registrados"
        }

        // Sin gastos: se oculta la lista y se muestra la invitacion a crear el primero.
        val hayGastos = gastos.isNotEmpty()
        binding.rvGastos.visibility = if (hayGastos) View.VISIBLE else View.GONE
        binding.vacio.visibility = if (hayGastos) View.GONE else View.VISIBLE
    }

    /** id == null -> crear un gasto nuevo. id con valor -> editar ese gasto. */
    private fun abrirFormulario(id: Int?) {
        val intent = Intent(this, FormularioGastoActivity::class.java)
        if (id != null) {
            intent.putExtra(FormularioGastoActivity.EXTRA_ID_GASTO, id)
        }
        startActivity(intent)
    }

    /**
     * Receta: con targetSdk 35+ la app se dibuja DEBAJO de las barras del sistema.
     * El encabezado verde llega hasta arriba del todo, asi que solo su contenido baja
     * lo que mide la barra de estado; la lista y el boton suben lo que mide la de navegacion.
     */
    private fun respetarBarrasDelSistema() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.header.updatePadding(top = barras.top + 16.dp)
            binding.rvGastos.updatePadding(bottom = barras.bottom + 96.dp)
            binding.btnNuevo.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = barras.bottom + 20.dp
            }
            insets
        }
    }
}

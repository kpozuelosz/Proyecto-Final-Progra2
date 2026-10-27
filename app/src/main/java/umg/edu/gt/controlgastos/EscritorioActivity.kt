package umg.edu.gt.controlgastos

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import umg.edu.gt.controlgastos.datos.GastoEntity
import umg.edu.gt.controlgastos.ui.EscritorioDashboard
import umg.edu.gt.controlgastos.ui.theme.ControlGastosTheme

/**
 * Pantalla "Escritorio": balance, gastos, ingresos y gastos por categoria.
 *
 * Esta pantalla esta hecha con Compose (el resto de la app usa vistas XML). Conviven sin problema:
 * cada Activity elige como dibujarse.
 *
 * IMPORTANTE: los datos llegan como Flow desde el DAO. collectAsState() convierte el Flow en un
 * estado de Compose: cuando la tabla cambia, los totales se actualizan solos, sin recargar.
 */
class EscritorioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ControlGastosTheme {
                val dao = Dependencias.gastoDao

                // remember: el Flow se crea UNA vez. Sin remember se crearia uno nuevo en cada redibujado.
                val totalIngresos by remember { dao.obtenerTotalIngresos() }.collectAsState(initial = 0.0)
                val totalGastos by remember { dao.obtenerTotalGastos() }.collectAsState(initial = 0.0)
                val porCategoria by remember { dao.obtenerGastosPorCategoria() }.collectAsState(initial = emptyList())

                Column(modifier = Modifier.fillMaxSize()) {
                    EncabezadoEscritorio(onVolver = { finish() })

                    // El inset de abajo evita que la ultima tarjeta quede debajo de la barra de navegacion.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        EscritorioDashboard(
                            totalIngresos = totalIngresos,
                            totalGastos = totalGastos,
                            // En la tabla se guarda "ALIMENTACION"; en pantalla se muestra "Alimentación".
                            gastosPorCategoria = porCategoria.map {
                                GastoEntity.nombreVisible(it.categoria) to it.total
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Mismo encabezado verde de las otras pantallas, con la flecha para volver a la lista. */
@Composable
private fun EncabezadoEscritorio(onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.header_bg),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            // Primero el fondo y DESPUES el inset: el verde llega hasta arriba, detras de la barra de estado.
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 8.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = "Volver a la lista",
                tint = colorResource(R.color.on_header)
            )
        }
        Text(
            text = stringResource(R.string.titulo_escritorio),
            color = colorResource(R.color.on_header),
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

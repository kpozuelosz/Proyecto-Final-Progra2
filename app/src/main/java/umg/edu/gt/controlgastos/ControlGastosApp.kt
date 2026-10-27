package umg.edu.gt.controlgastos

import android.app.Application

/**
 * Application = el objeto que representa a TODA la app. Android lo crea UNA vez, antes que
 * cualquier Activity, y vive mientras la app este abierta.
 *
 * IMPORTANTE: es el lugar seguro para preparar lo que las pantallas comparten. Si lo
 * hicieramos en MainActivity, y Android abriera directo el formulario (por ejemplo, al
 * restaurar la app), Dependencias.control no estaria listo.
 *
 * Cuidado: se registra en AndroidManifest.xml con android:name=".ControlGastosApp".
 */
class ControlGastosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Dependencias.iniciar(this)
    }
}

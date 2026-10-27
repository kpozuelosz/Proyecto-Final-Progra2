package umg.edu.gt.controlgastos.datos

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * La base de datos completa: que tablas tiene y que DAOs ofrece.
 *
 * - entities: las clases @Entity (una tabla por cada una).
 * - version:  numero del "esquema". Si cambias GastoEntity hay que subirlo y explicarle a Room
 *             como pasar los datos viejos a la tabla nueva (una Migration). Si no, la app se
 *             cierra al abrir.
 * - exportSchema = false: no guardamos el historial del esquema en archivos JSON.
 *
 * Es abstract: Room genera AppDatabase_Impl al compilar.
 */
@Database(entities = [GastoEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gastoDao(): GastoDao

    companion object {
        /**
         * Version 1 -> 2. La tabla pasa de (id, descripcion, monto, categoria, fecha) a
         * (id, descripcion, monto, categoria, esIngreso, fechaTimestamp).
         *
         * SQLite no permite cambiar columnas facilmente, asi que: se crea la tabla nueva, se copian
         * los gastos viejos, se borra la vieja y se renombra la nueva. Los gastos que ya estaban
         * guardados se conservan (todos quedan como gastos, esIngreso = 0).
         *
         * La fecha "2026-10-24" se convierte a milisegundos. Se suman 12 horas (43200 s) para
         * quedar a mediodia: asi la fecha no se corre de dia por la zona horaria.
         */
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE gastos_nueva (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        descripcion TEXT NOT NULL,
                        monto REAL NOT NULL,
                        categoria TEXT NOT NULL,
                        esIngreso INTEGER NOT NULL,
                        fechaTimestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO gastos_nueva (id, descripcion, monto, categoria, esIngreso, fechaTimestamp)
                    SELECT id, descripcion, monto, categoria, 0,
                           COALESCE(CAST(strftime('%s', fecha) AS INTEGER) + 43200,
                                    CAST(strftime('%s', 'now') AS INTEGER)) * 1000
                    FROM gastos
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE gastos")
                db.execSQL("ALTER TABLE gastos_nueva RENAME TO gastos")
            }
        }
    }
}

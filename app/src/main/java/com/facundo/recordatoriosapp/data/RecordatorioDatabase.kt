
package com.facundo.recordatoriosapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RecordatorioEntity::class],
    version = 4,
    exportSchema = false
)
abstract class RecordatorioDatabase : RoomDatabase() {

    abstract fun recordatorioDao(): RecordatorioDao

    companion object {

        @Volatile
        private var INSTANCE: RecordatorioDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE recordatorios ADD COLUMN fecha TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE recordatorios ADD COLUMN pendienteSincronizacion INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE recordatorios ADD COLUMN remoteId INTEGER DEFAULT NULL"
                )
                db.execSQL(
                    "ALTER TABLE recordatorios ADD COLUMN pendienteEliminar INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun obtenerInstancia(context: Context): RecordatorioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    RecordatorioDatabase::class.java,
                    "recordatorios_db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build()

                INSTANCE = instancia
                instancia
            }
        }
    }
}

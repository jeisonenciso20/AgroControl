package com.jeison.agrocontrol.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Animal::class,
        SyncOutbox::class,
        SyncMetadata::class,
        PotreroLocal::class,
        PendingTaskLocal::class,
        ProductionLocal::class,
        ReproductionEventLocal::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun syncDao(): SyncDao
    abstract fun localCacheDao(): LocalCacheDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE animales ADD COLUMN serverId TEXT")
                db.execSQL("ALTER TABLE animales ADD COLUMN syncState TEXT NOT NULL DEFAULT 'SYNCED'")
                db.execSQL("ALTER TABLE animales ADD COLUMN updatedAtEpoch INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_outbox (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entity TEXT NOT NULL,
                        operation TEXT NOT NULL,
                        localId TEXT NOT NULL,
                        serverId TEXT,
                        payloadJson TEXT NOT NULL,
                        createdAtEpoch INTEGER NOT NULL,
                        sent INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_metadata (
                        key TEXT NOT NULL PRIMARY KEY,
                        value TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS potreros (
                        serverId TEXT NOT NULL PRIMARY KEY,
                        nombre TEXT NOT NULL,
                        areaHectareas REAL,
                        estado TEXT NOT NULL,
                        especiesJson TEXT NOT NULL,
                        updatedAtEpoch INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pending_tasks (
                        serverId TEXT NOT NULL PRIMARY KEY,
                        titulo TEXT NOT NULL,
                        tipo TEXT NOT NULL,
                        status TEXT NOT NULL,
                        vencimiento TEXT NOT NULL,
                        referencia TEXT NOT NULL,
                        updatedAtEpoch INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS production_records (
                        serverId TEXT NOT NULL PRIMARY KEY,
                        productType TEXT NOT NULL,
                        cantidad REAL NOT NULL,
                        unidad TEXT NOT NULL,
                        fecha TEXT NOT NULL,
                        notas TEXT NOT NULL,
                        updatedAtEpoch INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE potreros ADD COLUMN tipoPasto TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE potreros ADD COLUMN fechaUltimaSiembra TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE potreros ADD COLUMN fechaProximoUso TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE potreros ADD COLUMN observacion TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE potreros ADD COLUMN syncState TEXT NOT NULL DEFAULT 'SYNCED'")
                db.execSQL("ALTER TABLE production_records ADD COLUMN jornada TEXT NOT NULL DEFAULT 'MANANA'")
                db.execSQL("ALTER TABLE production_records ADD COLUMN animalServerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE production_records ADD COLUMN animalCodigo TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE production_records ADD COLUMN syncState TEXT NOT NULL DEFAULT 'SYNCED'")
                db.execSQL("ALTER TABLE pending_tasks ADD COLUMN workType TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE pending_tasks ADD COLUMN observacion TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE pending_tasks ADD COLUMN syncState TEXT NOT NULL DEFAULT 'SYNCED'")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS reproduction_events (
                        serverId TEXT NOT NULL PRIMARY KEY,
                        animalServerId TEXT NOT NULL,
                        animalCodigo TEXT NOT NULL,
                        tipoEvento TEXT NOT NULL,
                        programada TEXT NOT NULL,
                        notas TEXT NOT NULL,
                        syncState TEXT NOT NULL DEFAULT 'SYNCED',
                        updatedAtEpoch INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agrocontrol.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}


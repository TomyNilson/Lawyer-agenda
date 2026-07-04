package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CaseEntity::class,
        StepEntity::class,
        DocumentEntity::class,
        CourtEntity::class,
        CaseTypeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun caseDao(): CaseDao
    abstract fun stepDao(): StepDao
    abstract fun documentDao(): DocumentDao
    abstract fun courtDao(): CourtDao
    abstract fun caseTypeDao(): CaseTypeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lawyer_diary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

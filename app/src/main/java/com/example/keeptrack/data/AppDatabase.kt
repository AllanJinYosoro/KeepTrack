package com.example.keeptrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(entities = [ExerciseType::class, ExerciseRecord::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "powerfit_database"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = database.exerciseDao()
                                if (dao.getAllExerciseTypes().first().isEmpty()) {
                                    populateDatabase(dao)
                                }
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(dao: ExerciseDao) {
            val defaultTypes = listOf(
                ExerciseType(name = "中等强度有氧", weight = 1.0, description = "快走、椭圆机、轻松骑车、慢速游泳"),
                ExerciseType(name = "较高强度有氧", weight = 1.5, description = "慢跑、跳绳、HIIT低强度版、爬坡跑"),
                ExerciseType(name = "高强度有氧", weight = 2.0, description = "持续跑、强HIIT、冲刺间歇"),
                ExerciseType(name = "全身力量训练", weight = 1.2, description = "胸、背、腿、肩、核心等"),
                ExerciseType(name = "核心训练", weight = 0.8, monthlyCap = 80.0, description = "卷腹、平板支撑、举腿等"),
                ExerciseType(name = "拉伸放松", weight = 0.5, description = "静态拉伸、瑜伽、泡沫轴筋膜放松")
            )
            defaultTypes.forEach { dao.insertExerciseType(it) }
        }
    }
}

package tag.egypt.com.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Primary SQLite Room Database for TAJ EGY local persistence.
 */
@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        ProviderEntity::class,
        PromptEntity::class,
        McpServerEntity::class,
        SkillEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun providerDao(): ProviderDao
    abstract fun promptDao(): PromptDao
    abstract fun mcpDao(): McpDao
    abstract fun skillDao(): SkillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taj_egy_local.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package tag.egypt.com.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Message operations in SQLite in TAJ EGY.
 */
@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesForConversationFlow(convId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesForConversation(convId: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    fun getMessageById(id: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(message: MessageEntity)

    @Update
    fun update(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM messages WHERE conversationId = :convId")
    fun deleteByConversationId(convId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :convId")
    fun getMessageCount(convId: String): Int
}

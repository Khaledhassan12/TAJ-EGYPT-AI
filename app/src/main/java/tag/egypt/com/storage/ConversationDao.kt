package tag.egypt.com.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Conversation operations in SQLite in TAJ EGY.
 */
@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getActiveConversationsFlow(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedConversationsFlow(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun getConversationById(id: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE title LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchConversations(query: String): List<ConversationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(conversation: ConversationEntity)

    @Update
    fun update(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM conversations")
    fun deleteAll()

    @Query("UPDATE conversations SET isPinned = :pinned WHERE id = :id")
    fun setPinned(id: String, pinned: Boolean)

    @Query("UPDATE conversations SET isArchived = :archived WHERE id = :id")
    fun setArchived(id: String, archived: Boolean)

    @Query("UPDATE conversations SET title = :newTitle, updatedAt = :updatedAt WHERE id = :id")
    fun updateTitle(id: String, newTitle: String, updatedAt: Long)
}

package tag.egypt.com.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for reusable Prompt Templates in TAJ EGY.
 */
@Dao
interface PromptDao {

    @Query("SELECT * FROM prompts ORDER BY isFavorite DESC, title ASC")
    fun getAllPromptsFlow(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts ORDER BY isFavorite DESC, title ASC")
    fun getAllPrompts(): List<PromptEntity>

    @Query("SELECT * FROM prompts WHERE id = :id LIMIT 1")
    fun getPromptById(id: String): PromptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(prompt: PromptEntity)

    @Update
    fun update(prompt: PromptEntity)

    @Query("DELETE FROM prompts WHERE id = :id")
    fun deleteById(id: String)

    @Query("UPDATE prompts SET isFavorite = :favorite WHERE id = :id")
    fun setFavorite(id: String, favorite: Boolean)
}

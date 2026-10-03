package tag.egypt.com.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for AI Skills in TAJ EGY.
 */
@Dao
interface SkillDao {

    @Query("SELECT * FROM skills ORDER BY priority DESC, name ASC")
    fun getAllSkillsFlow(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE isEnabled = 1 ORDER BY priority DESC, name ASC")
    fun getEnabledSkills(): List<SkillEntity>

    @Query("SELECT * FROM skills WHERE id = :id LIMIT 1")
    fun getSkillById(id: String): SkillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(skill: SkillEntity)

    @Update
    fun update(skill: SkillEntity)

    @Query("DELETE FROM skills WHERE id = :id")
    fun deleteById(id: String)

    @Query("UPDATE skills SET isEnabled = :enabled WHERE id = :id")
    fun setEnabled(id: String, enabled: Boolean)
}

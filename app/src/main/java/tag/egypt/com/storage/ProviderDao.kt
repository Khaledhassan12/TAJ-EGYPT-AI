package tag.egypt.com.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for AI Provider configurations in SQLite in TAJ EGY.
 */
@Dao
interface ProviderDao {

    @Query("SELECT * FROM providers ORDER BY isDefault DESC, name ASC")
    fun getAllProvidersFlow(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers ORDER BY isDefault DESC, name ASC")
    fun getAllProviders(): List<ProviderEntity>

    @Query("SELECT * FROM providers WHERE id = :id LIMIT 1")
    fun getProviderById(id: String): ProviderEntity?

    @Query("SELECT * FROM providers WHERE isDefault = 1 LIMIT 1")
    fun getDefaultProvider(): ProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(provider: ProviderEntity)

    @Update
    fun update(provider: ProviderEntity)

    @Query("DELETE FROM providers WHERE id = :id")
    fun deleteById(id: String)

    @Query("UPDATE providers SET isDefault = 0")
    fun clearDefaultFlags()

    @Query("UPDATE providers SET isDefault = 1 WHERE id = :id")
    fun setDefaultProvider(id: String)
}

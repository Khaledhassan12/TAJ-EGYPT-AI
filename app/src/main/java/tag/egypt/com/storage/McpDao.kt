package tag.egypt.com.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MCP Server configurations in TAJ EGY.
 */
@Dao
interface McpDao {

    @Query("SELECT * FROM mcp_servers ORDER BY name ASC")
    fun getAllServersFlow(): Flow<List<McpServerEntity>>

    @Query("SELECT * FROM mcp_servers ORDER BY name ASC")
    fun getAllServers(): List<McpServerEntity>

    @Query("SELECT * FROM mcp_servers WHERE id = :id LIMIT 1")
    fun getServerById(id: String): McpServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(server: McpServerEntity)

    @Update
    fun update(server: McpServerEntity)

    @Query("DELETE FROM mcp_servers WHERE id = :id")
    fun deleteById(id: String)

    @Query("UPDATE mcp_servers SET isEnabled = :enabled WHERE id = :id")
    fun setEnabled(id: String, enabled: Boolean)
}

package org.rdtoolkit.model.session

import android.content.Context
import androidx.room.*
import org.rdtoolkit.model.Converters
import java.util.*

@Dao
interface TestSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(session : DbTestSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveResult(session : DbTestSessionResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveConfig(session : DbTestSessionConfiguration)

    @Query("SELECT * FROM DbTestSession WHERE sessionId = :sessionId")
    fun load(sessionId: String): DbTestSession

    @Query("SELECT * FROM DbTestSessionResult WHERE sessionId = :sessionId")
    fun loadResult(sessionId: String): DbTestSessionResult?

    @Query("SELECT * FROM DbTestSessionConfiguration WHERE sessionId = :sessionId")
    fun loadConfig(sessionId: String): DbTestSessionConfiguration

    @Query("SELECT sessionId FROM DbTestSession WHERE state = 'RUNNING' and (timeExpired is null or :now < timeExpired)")
    fun getPendingSessionIds(now : Date) : List<String>

    @Transaction
    fun save(dbSession: DataTestSession) {
        save(dbSession.session)
        saveConfig(dbSession.config)
        dbSession.result?.let{ saveResult(it) }
    }

    fun loadDataSession(sessionId: String): DataTestSession {
        return DataTestSession(load(sessionId),
                loadConfig(sessionId),
                loadResult(sessionId)
        )
    }


    @Query("SELECT * FROM DbTestSession ORDER BY timeStarted DESC")
    fun loadSessions(): List<DataTestSession>
}

@Database(entities = [DbTestSession::class, DbTestSessionConfiguration::class,
    DbTestSessionResult::class], version = 1)
@TypeConverters(Converters::class)
abstract class RdtDatabase : RoomDatabase() {
    abstract fun testSessionDao(): TestSessionDao
}

private lateinit var INSTANCE: RdtDatabase

fun getDatabase(context: Context): RdtDatabase {
    synchronized(RdtDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                    RdtDatabase::class.java,
                    "rdtdatabase").build()
        }
    }
    return INSTANCE
}

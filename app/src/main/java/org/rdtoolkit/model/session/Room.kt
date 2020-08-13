package org.rdtoolkit.model.session

import android.content.Context
import androidx.room.*
import org.rdtoolkit.model.Converters

@Dao
interface TestSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(session : TestSession)

    @Query("SELECT * FROM TestSession WHERE sessionId = :sessionId")
    fun load(sessionId: String): TestSession
}

@Database(entities = [TestSession::class], version = 1)
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

package org.rdtoolkit

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.rdtoolkit.model.session.MIGRATION_1_2
import org.rdtoolkit.model.session.RdtDatabase
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    // Array of all migrations
    private val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2)

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            RdtDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // db has schema version 1. insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            execSQL("INSERT INTO DbTestSession VALUES('9faf1fcf-f6a8-4551-a94d-6a219ed5e5d7','RUNNING','firstresponse_mal_pf',1611329424563,1611330624562,1611331224562);" +
                    "INSERT INTO DbTestSession VALUES('20fcc2f1-a3c2-490d-b2a7-3e280ebf221c','QUEUED','firstresponse_mal_pf',1611329428387,1611329433387,1611330033387);" +
                    "INSERT INTO DbTestSessionConfiguration VALUES('9faf1fcf-f6a8-4551-a94d-6a219ed5e5d7','TWO_PHASE','CRITERIA_SET_AND','PRE_POPULATE','mal_pf','Tedros Adhanom','#4SFS',NULL,NULL,NULL,NULL,'{\"FLAG_TESTING_QA\":\"TRUE\"}');" +
                    "INSERT INTO DbTestSessionConfiguration VALUES('20fcc2f1-a3c2-490d-b2a7-3e280ebf221c','TWO_PHASE','CRITERIA_SET_AND','PRE_POPULATE','mal_pf','Tedros Adhanom','#4SFS',NULL,NULL,NULL,NULL,'{\"FLAG_TESTING_QA\":\"TRUE\"}');" +
                    "INSERT INTO DbTestSessionResult VALUES('20fcc2f1-a3c2-490d-b2a7-3e280ebf221c',1611329457417,'/storage/emulated/0/Android/data/org.rdtoolkit/files/session_media/20fcc2f1-a3c2-490d-b2a7-3e280ebf221c/20210122_103055_cropped.jpg','{\"raw\":\"/storage/emulated/0/Android/data/org.rdtoolkit/files/session_media/20fcc2f1-a3c2-490d-b2a7-3e280ebf221c/20210122_103055.jpg\",\"cropped\":\"/storage/emulated/0/Android/data/org.rdtoolkit/files/session_media/20fcc2f1-a3c2-490d-b2a7-3e280ebf221c/20210122_103055_cropped.jpg\"}','{\"mal_pf\":\"mal_pf_pos\"}','{\"mal_pf\":\"mal_pf_pos\"}');")
            // Prepare for the next version.
            close()
        }

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
    }


    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create earliest version of the database.
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room.databaseBuilder(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                RdtDatabase::class.java,
                        TEST_DB
        ).addMigrations(*ALL_MIGRATIONS).build().apply {
            getOpenHelper().getWritableDatabase()
            close()
        }
    }
}

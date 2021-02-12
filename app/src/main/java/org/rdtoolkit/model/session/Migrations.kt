package org.rdtoolkit.model.session

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `DbTestSessionMetrics` (`sessionId` TEXT NOT NULL, `data` TEXT NOT NULL, PRIMARY KEY(`sessionId`))")
        database.execSQL("CREATE TABLE IF NOT EXISTS `DbTestSessionTraceEvent` " +
                "(`sessionId` TEXT NOT NULL, `timestamp` TEXT NOT NULL, `eventTag` TEXT NOT NULL, " +
                "`eventMessage` TEXT NOT NULL, `eventJson` TEXT, `sandboxObjectId` TEXT, " +
                "PRIMARY KEY(`sessionId`))")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE `DbTestSessionTraceEvent`")
        database.execSQL("CREATE TABLE IF NOT EXISTS `DbTestSessionTraceEvent` " +
                "(`id` INTEGER primary key autoincrement NOT NULL," +
                "`sessionId` TEXT NOT NULL, `timestamp` TEXT NOT NULL, `eventTag` TEXT NOT NULL, " +
                "`eventMessage` TEXT NOT NULL, `eventJson` TEXT, `sandboxObjectId` TEXT)")
        database.execSQL("CREATE INDEX `sessionId_index` ON `DbTestSessionTraceEvent`(`sessionId`)")
    }
}
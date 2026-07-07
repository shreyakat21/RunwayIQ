package com.runwayiq.data.db

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

object DatabaseFactory {
    fun create(): RunwayDatabase {
        val dbDir = File(System.getProperty("user.home"), ".runwayiq")
        dbDir.mkdirs()
        val dbFile = File(dbDir, "runway.db")
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        RunwayDatabase.Schema.create(driver)
        return RunwayDatabase(driver)
    }
}

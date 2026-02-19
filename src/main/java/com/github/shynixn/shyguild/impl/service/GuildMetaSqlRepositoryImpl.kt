package com.github.shynixn.shyguild.impl.service

import com.github.shynixn.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.fasterxml.jackson.databind.DeserializationFeature
import com.github.shynixn.fasterxml.jackson.databind.ObjectMapper
import com.github.shynixn.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.github.shynixn.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.github.shynixn.mcutils.database.api.SqlConnectionService
import com.github.shynixn.mcutils.database.entity.ConnectionType
import com.github.shynixn.shyguild.contract.GuildMetaSqlRepository
import com.github.shynixn.shyguild.entity.GuildMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection

class GuildMetaSqlRepositoryImpl(
    private val tableName: String,
    private val sqlConnectionService: SqlConnectionService,
    private val typeReference: TypeReference<GuildMeta>
) : GuildMetaSqlRepository {
    private val objectMapper: ObjectMapper =
        ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val createTableStatement by lazy {
        if (sqlConnectionService.connectionType == ConnectionType.MYSQL) {
            // id as integer auto-increment primary key, name indexed (normal varchar), data as MEDIUMTEXT for larger payloads
            """
            CREATE TABLE IF NOT EXISTS $tableName (
              id INT AUTO_INCREMENT PRIMARY KEY,
              name VARCHAR(255) NOT NULL,
              data MEDIUMTEXT NOT NULL
            )
            """.trimIndent() + "; CREATE INDEX IF NOT EXISTS ${tableName}_name_index ON $tableName(name)"
        } else {
            // SQLite and others: use INTEGER PRIMARY KEY AUTOINCREMENT for id and TEXT for data
            """
            CREATE TABLE IF NOT EXISTS $tableName (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT NOT NULL,
              data TEXT NOT NULL
            )
            """.trimIndent() + "; CREATE INDEX IF NOT EXISTS ${tableName}_name_index ON $tableName(name)"
        }
    }
    private val deleteStatement by lazy {
        "DELETE FROM $tableName WHERE name=?"
    }
    private val selectStatement by lazy {
        "SELECT data FROM $tableName WHERE name=?"
    }
    private val selectAllStatement by lazy {
        "SELECT data FROM $tableName"
    }
    private val updateStatement by lazy {
        "UPDATE $tableName SET data=? WHERE name=?"
    }
    private val insertStatement by lazy {
        "INSERT INTO $tableName (name, data) VALUES(?,?)"
    }

    /**
     * Creates the database if it does not exist yet.
     */
    override fun createIfNotExist() {
        sqlConnectionService.connect()
        sqlConnectionService.getConnection().use { connection ->
            connection.prepareStatement(createTableStatement).use { statement ->
                statement.execute()
            }
            connection.commit()
        }
    }

    /**
     * Saves the given data.
     */
    override suspend fun save(data: GuildMeta) {
        withContext(Dispatchers.IO) {
            sqlConnectionService.getConnection().use { connection ->
                saveData(connection, data)
                connection.commit()
            }
        }
    }

    /**
     * Deletes the given data from the storage.
     */
    override suspend fun delete(data: GuildMeta) {
        withContext(Dispatchers.IO) {
            sqlConnectionService.getConnection().use { connection ->
                connection.prepareStatement(deleteStatement).use { statement ->
                    statement.setString(1, data.name)
                    statement.execute()
                }
                connection.commit()
            }
        }
    }

    /**
     * Gets the data from the storage by name.
     * Returns null if it does not exist.
     */
    override suspend fun getByName(name: String): GuildMeta? {
        var result: GuildMeta? = null

        withContext(Dispatchers.IO) {
            sqlConnectionService.getConnection().use { connection ->
                connection.prepareStatement(selectStatement).use { statement ->
                    statement.setString(1, name)
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            val data = resultSet.getString("data")
                            result = objectMapper.readValue<GuildMeta>(
                                data, typeReference
                            )
                        }
                    }
                }
            }
        }

        return result
    }

    /**
     * Gets all guilds from the storage.
     */
    override suspend fun getAll(): Sequence<GuildMeta> {
        return withContext(Dispatchers.IO) {
            sequence {
                sqlConnectionService.getConnection().use { connection ->
                    connection.prepareStatement(selectAllStatement).use { statement ->
                        statement.executeQuery().use { resultSet ->
                            while (resultSet.next()) {
                                val data = resultSet.getString("data")
                                val retrievedValue = objectMapper.readValue<GuildMeta>(
                                    data, typeReference
                                )
                                yield(retrievedValue)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveData(connection: Connection, data: GuildMeta) {
        val serializedData = objectMapper.writeValueAsString(data)
        if (data.isPersisted) {
            connection.prepareStatement(updateStatement).use { statement ->
                statement.setString(1, serializedData)
                statement.setString(2, data.name)
                statement.execute()
            }
        } else {
            connection.prepareStatement(insertStatement).use { statement ->
                statement.setString(1, data.name)
                statement.setString(2, serializedData)
                statement.execute()
            }
        }
        data.isPersisted = true
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     */
    override fun close() {
        sqlConnectionService.close()
    }
}
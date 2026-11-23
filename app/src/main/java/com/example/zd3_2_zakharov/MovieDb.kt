package com.example.zd3_2_zakharov

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val imdbID: String,
    val title: String,
    val year: String,
    val type: String,
    val channel: String,
    val plot: String = "",
    val genre: String = "",
    val director: String = "",
    val actors: String = "",
    val rating: String = ""
)

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies WHERE channel = :channel ORDER BY title")
    fun getByChannel(channel: String): List<Movie>

    @Query("SELECT * FROM movies WHERE imdbID = :id")
    fun getById(id: String): Movie?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(movie: Movie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(movies: List<Movie>)

    @Query("DELETE FROM movies WHERE channel = :channel")
    fun clearChannel(channel: String)
}

@Database(entities = [Movie::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun get(context: Context): AppDb = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDb::class.java,
                "movies.db"
            ).allowMainThreadQueries().build().also { instance = it }
        }
    }
}

object Omdb {
    private const val KEY = "bc1af6cd"

    fun search(channel: String, query: String): List<Movie> {
        val url = "https://www.omdbapi.com/?apikey=$KEY&s=${enc(query)}&type=movie"
        return try {
            val array = JSONObject(get(url)).optJSONArray("Search") ?: return emptyList()
            (0 until array.length()).mapNotNull { i ->
                val item = array.optJSONObject(i) ?: return@mapNotNull null
                Movie(
                    imdbID = item.getString("imdbID"),
                    title = item.getString("Title"),
                    year = item.getString("Year"),
                    type = item.getString("Type"),
                    channel = query
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun details(movie: Movie): Movie {
        val url = "https://www.omdbapi.com/?apikey=$KEY&i=${movie.imdbID}&plot=short"
        return try {
            val json = JSONObject(get(url))
            movie.copy(
                plot = json.optString("Plot"),
                genre = json.optString("Genre"),
                director = json.optString("Director"),
                actors = json.optString("Actors"),
                rating = json.optString("imdbRating")
            )
        } catch (e: Exception) {
            movie
        }
    }

    private fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private fun enc(text: String): String = URLEncoder.encode(text, "UTF-8")
}

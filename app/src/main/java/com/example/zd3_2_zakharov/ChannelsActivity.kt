package com.example.zd3_2_zakharov

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChannelsActivity : Activity() {

    private val channels = listOf(
        "Боевики" to "action",
        "Комедии" to "comedy",
        "Ужасы" to "horror",
        "Фантастика" to "star"
    )

    private val dao by lazy { AppDb.get(this).movieDao() }
    private val menuButtons = mutableListOf<Button>()

    private lateinit var adapter: MovieAdapter
    private lateinit var search: EditText
    private var all: List<Movie> = emptyList()
    private var selected = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channels)

        adapter = MovieAdapter { movie ->
            startActivity(
                Intent(this, MovieDetailsActivity::class.java)
                    .putExtra("id", movie.imdbID)
            )
        }

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(this, 3)
        recycler.adapter = adapter

        search = findViewById(R.id.search)
        search.doAfterTextChanged { filter(it.toString()) }

        buildMenu()
        select(0)
        menuButtons.firstOrNull()?.requestFocus()
    }

    private fun buildMenu() {
        val menu = findViewById<LinearLayout>(R.id.channelMenu)
        channels.forEachIndexed { index, channel ->
            val button = Button(this).apply {
                text = "${index + 1}  ${channel.first}"
                textSize = 14f
                isAllCaps = false
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                setPadding(dp(16), 0, 0, 0)
                setTextColor(getColorStateList(R.color.channel_text))
                setBackgroundResource(R.drawable.channel_selector)
                stateListAnimator = null
                nextFocusUpId = R.id.search
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(44)
                ).apply { bottomMargin = dp(4) }
                setOnClickListener { select(index) }
            }
            menu.addView(button)
            menuButtons.add(button)
        }
    }

    private fun select(index: Int) {
        selected = index
        val (name, query) = channels[index]

        findViewById<TextView>(R.id.title).text = name.uppercase()
        menuButtons.forEachIndexed { i, button -> button.isSelected = i == index }

        //сначала показываются сохраненные фильмы из БД, затем идет подгрузка из сети
        all = dao.getByChannel(query)
        filter(search.text.toString())

        Thread {
            val fresh = Omdb.search(query, query)
            if (fresh.isEmpty()) return@Thread

            dao.clearChannel(query)
            dao.saveAll(fresh)
            val saved = dao.getByChannel(query)

            runOnUiThread {
                if (selected == index) {
                    all = saved
                    filter(search.text.toString())
                }
            }
        }.start()
    }

    private fun filter(query: String) {
        val text = query.trim()
        adapter.submit(
            if (text.isEmpty()) all
            else all.filter {
                it.title.contains(text, ignoreCase = true) || it.year.contains(text)
            }
        )
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

class MovieAdapter(
    private val onClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    private val items = mutableListOf<Movie>()

    fun submit(movies: List<Movie>) {
        items.clear()
        items.addAll(movies)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class MovieViewHolder(
        itemView: View,
        private val onClick: (Movie) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val poster: TextView = itemView.findViewById(R.id.poster)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val meta: TextView = itemView.findViewById(R.id.meta)

        fun bind(movie: Movie) {
            poster.text = movie.title.take(1)
            title.text = movie.title
            meta.text = "${movie.year} · ${if (movie.type == "series") "Сериал" else "Фильм"}"
            itemView.setOnClickListener { onClick(movie) }
        }
    }
}

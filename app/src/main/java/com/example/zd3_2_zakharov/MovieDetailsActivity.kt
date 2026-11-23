package com.example.zd3_2_zakharov

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MovieDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)

        if (savedInstanceState == null) {
            val fragment = DetailsFragment()
            fragment.arguments = Bundle().apply {
                putString("id", intent.getStringExtra("id"))
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
    }
}

class DetailsFragment : Fragment(R.layout.fragment_movie_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = requireArguments().getString("id") ?: return
        val dao = AppDb.get(requireContext()).movieDao()
        val movie = dao.getById(id) ?: return

        show(view, movie)

        view.findViewById<Button>(R.id.back).apply {
            setOnClickListener { requireActivity().finish() }
            requestFocus()
        }

        if (movie.plot.isBlank()) {
            Thread {
                val full = Omdb.details(movie)
                dao.save(full)
                activity?.runOnUiThread {
                    if (isAdded) show(requireView(), full)
                }
            }.start()
        }
    }

    private fun show(view: View, movie: Movie) {
        view.findViewById<TextView>(R.id.poster).text = movie.title.take(1)
        view.findViewById<TextView>(R.id.title).text = movie.title

        val meta = listOf(
            movie.year,
            movie.genre,
            if (movie.rating.isNotBlank() && movie.rating != "N/A") "IMDb ${movie.rating}" else ""
        ).filter { it.isNotBlank() && it != "N/A" }
        view.findViewById<TextView>(R.id.meta).text = meta.joinToString(" · ")

        view.findViewById<TextView>(R.id.plot).text =
            if (movie.plot.isBlank() || movie.plot == "N/A") "Описание недоступно"
            else movie.plot

        val people = listOf(
            if (movie.director.isNotBlank() && movie.director != "N/A") "Режиссёр: ${movie.director}" else "",
            if (movie.actors.isNotBlank() && movie.actors != "N/A") "В ролях: ${movie.actors}" else ""
        ).filter { it.isNotBlank() }
        view.findViewById<TextView>(R.id.people).text = people.joinToString("\n")
    }
}

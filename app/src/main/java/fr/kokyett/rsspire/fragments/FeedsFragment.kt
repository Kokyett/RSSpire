package fr.kokyett.rsspire.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.ApplicationContext
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.activities.EditFeedActivity
import fr.kokyett.rsspire.adapters.FeedListAdapter


class FeedsFragment : Fragment() {
    private lateinit var adapter: FeedListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recyclerview, container, false)
        initRecyclerView(view)
        return view
    }

    private fun initRecyclerView(view: View) {
        adapter = FeedListAdapter(activity as AppCompatActivity)
        adapter.onItemClick = {
            val intent = Intent(context, EditFeedActivity::class.java)
            intent.putExtra("FEED", it.id)
            startActivity(intent)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL))

        var idCategory: Long? = null
        if (requireArguments().containsKey("ID_CATEGORY"))
            idCategory = requireArguments().getLong("ID_CATEGORY")

        ApplicationContext.getFeedRepository().getByCategory(idCategory).observe(viewLifecycleOwner) { feeds ->
            feeds?.let { adapter.submitList(it) }
        }
    }
}
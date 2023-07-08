package fr.kokyett.rsspire.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.kokyett.rsspire.R
import fr.kokyett.rsspire.RSSpireApplication
import fr.kokyett.rsspire.activities.EditFeedActivity
import fr.kokyett.rsspire.adapters.FeedListAdapter
import fr.kokyett.rsspire.utils.ExtrasUtils

class FeedsFragment : Fragment() {
    private lateinit var adapter: FeedListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_feeds, container, false)
        initRecyclerView(view)
        return view
    }

    private fun initRecyclerView(view: View) {
        adapter = FeedListAdapter()
        adapter.onItemClick = {
            val intent = Intent(context, EditFeedActivity::class.java)
            intent.putExtra(ExtrasUtils.FEED, it.id)
            startActivity(intent)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context, DividerItemDecoration.VERTICAL
            )
        )

        var idCategory: Long? = null
        if (requireArguments().containsKey(ExtrasUtils.ID_CATEGORY))
            idCategory = requireArguments().getLong(ExtrasUtils.ID_CATEGORY)

        (context?.applicationContext as RSSpireApplication).feedRepository.getByCategory(idCategory).observe(viewLifecycleOwner) { feeds ->
            feeds?.let { adapter.submitList(it) }
        }
    }
}
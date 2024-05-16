package fr.kokyett.rsspire.fragments

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import fr.kokyett.rsspire.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangelogFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_changelog, container, false)
        val textView = view.findViewById<TextView>(R.id.text)
        textView.movementMethod = LinkMovementMethod.getInstance()
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                this@ChangelogFragment.context?.assets?.open("CHANGELOG.html")?.bufferedReader()?.use {
                    val html = fr.kokyett.rsspire.utils.Html.formatFullContent(it.readText())
                    withContext(Dispatchers.Main) {
                        textView.text = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
                    }
                }
            }
        }
        return view
    }
}
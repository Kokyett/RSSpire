package fr.kokyett.rsspire.fragments

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import fr.kokyett.rsspire.BuildConfig
import fr.kokyett.rsspire.R

class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        view.findViewById<TextView>(R.id.version).text =
            String.format("%s / %d", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        val sourceCode = view.findViewById<TextView>(R.id.sourcecode)
        sourceCode.text = Html.fromHtml(getString(R.string.source_code), Html.FROM_HTML_MODE_LEGACY)
        sourceCode.movementMethod = LinkMovementMethod.getInstance()

        val aboutMaterial = view.findViewById<TextView>(R.id.about_material)
        aboutMaterial.text =
            Html.fromHtml(getString(R.string.about_material_link), Html.FROM_HTML_MODE_LEGACY)
        aboutMaterial.movementMethod = LinkMovementMethod.getInstance()

        val aboutLicense = view.findViewById<TextView>(R.id.about_license)
        aboutLicense.text =
            Html.fromHtml(getString(R.string.about_license_link), Html.FROM_HTML_MODE_LEGACY)
        aboutLicense.movementMethod = LinkMovementMethod.getInstance()

        return view
    }
}
package com.glassous.gles.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.glassous.gles.MainActivity
import com.glassous.gles.R

class SettingsFragment : Fragment() {

    private lateinit var etDefaultUrl: EditText
    private lateinit var btnSaveDefaultUrl: Button
    private lateinit var btnGoToGithub: Button
    private var webViewController: MainActivity.WebViewController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        etDefaultUrl = root.findViewById(R.id.et_default_url)
        btnSaveDefaultUrl = root.findViewById(R.id.btn_save_default_url)

        val sharedPreferences = requireContext().getSharedPreferences("WebViewData", android.content.Context.MODE_PRIVATE)

        val defaultUrl = sharedPreferences.getString("default_url", null)
        if (defaultUrl != null) {
            etDefaultUrl.setText(defaultUrl)
        }

        btnSaveDefaultUrl.setOnClickListener {
            var inputUrl = etDefaultUrl.text.toString().trim()
            if (inputUrl.isNotEmpty()) {
                if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
                    inputUrl = "https://$inputUrl"
                }
                sharedPreferences.edit().putString("default_url", inputUrl).apply()
                Toast.makeText(requireContext(), "默认主页已设置为: $inputUrl", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "请输入有效的网址", Toast.LENGTH_SHORT).show()
            }
        }


        return root
    }

    fun setWebViewController(controller: MainActivity.WebViewController) {
        this.webViewController = controller
    }
}
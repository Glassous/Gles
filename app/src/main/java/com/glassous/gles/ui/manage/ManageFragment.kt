package com.glassous.gles.ui.manage

import android.os.Bundle
import android.util.Log
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

class ManageFragment : Fragment() {

    private lateinit var tvCurrentUrl: TextView
    private lateinit var btnBookmark: Button
    private lateinit var btnViewBookmarks: Button
    private lateinit var btnViewHistory: Button
    private lateinit var etUrlInput: EditText
    private lateinit var btnGo: Button
    private lateinit var btnGoToGithub: Button
    private var webViewController: MainActivity.WebViewController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_manage, container, false)

        tvCurrentUrl = root.findViewById(R.id.tv_current_url)
        btnBookmark = root.findViewById(R.id.btn_bookmark)
        btnViewBookmarks = root.findViewById(R.id.btn_view_bookmarks)
        btnViewHistory = root.findViewById(R.id.btn_view_history)
        etUrlInput = root.findViewById(R.id.et_url_input)
        btnGo = root.findViewById(R.id.btn_go)
        btnGoToGithub = root.findViewById(R.id.btn_go_to_github)

        val sharedPreferences = requireContext().getSharedPreferences("WebViewData", android.content.Context.MODE_PRIVATE)

        webViewController?.let {
            tvCurrentUrl.text = it.getCurrentUrl()
        }

        btnBookmark.setOnClickListener {
            val currentUrl = webViewController?.getCurrentUrl() ?: return@setOnClickListener
            val bookmarks = sharedPreferences.getStringSet("bookmarks", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            bookmarks.add(currentUrl)
            sharedPreferences.edit().putStringSet("bookmarks", bookmarks).apply()
        }

        btnViewBookmarks.setOnClickListener {
            val bookmarksFragment = BookmarksFragment().apply {
                setWebViewController(webViewController!!)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_main, bookmarksFragment)
                .addToBackStack(null)
                .commit()
        }

        btnViewHistory.setOnClickListener {
            val historyFragment = HistoryFragment().apply {
                setWebViewController(webViewController!!)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_activity_main, historyFragment)
                .addToBackStack(null)
                .commit()
        }

        btnGo.setOnClickListener {
            var inputUrl = etUrlInput.text.toString().trim()
            if (inputUrl.isNotEmpty()) {
                // Ensure the URL has a scheme (http or https)
                if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
                    inputUrl = "https://$inputUrl"
                }
                sharedPreferences.edit().putString("pending_url", inputUrl).apply()
                Log.d("ManageFragment", "Set pending_url to: $inputUrl")
                Toast.makeText(requireContext(), "已选择 $inputUrl，回到主页后将加载", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
                Log.d("ManageFragment", "Popped back stack to HomeFragment")
                etUrlInput.text.clear()
            }
        }

        btnGoToGithub.setOnClickListener {
            val githubUrl = "https://github.com/Glassous"
            sharedPreferences.edit().putString("pending_url", githubUrl).apply()
            Toast.makeText(requireContext(), "已选择 $githubUrl，回到主页后将加载", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            Log.d("ManageFragment", "Popped back stack to HomeFragment (GitHub)")
        }

        return root
    }

    fun setWebViewController(controller: MainActivity.WebViewController) {
        this.webViewController = controller
    }

    fun onUrlChanged(url: String) {
        tvCurrentUrl.text = url
    }
}
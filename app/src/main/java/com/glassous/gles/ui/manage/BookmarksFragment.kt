package com.glassous.gles.ui.manage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glassous.gles.MainActivity
import com.glassous.gles.R
import com.glassous.gles.ui.home.HomeFragment

class BookmarksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: Button
    private var webViewController: MainActivity.WebViewController? = null
    private lateinit var adapter: UrlAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_bookmarks, container, false)

        recyclerView = root.findViewById(R.id.rv_bookmarks)
        btnBack = root.findViewById(R.id.btn_back)

        val sharedPreferences = requireContext().getSharedPreferences("WebViewData", Context.MODE_PRIVATE)

        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        if (isFirstRun) {
            val defaultBookmarks = setOf(
                "https://www.google.com",
                "https://www.bing.com",
                "https://www.facebook.com",
                "https://www.twitter.com",
                "https://www.instagram.com",
                "https://www.youtube.com",
                "https://www.netflix.com",
                "https://www.bbc.com",
                "https://www.cnn.com",
                "https://www.github.com",
                "https://www.notion.so",
                "https://www.dropbox.com",
                "https://www.amazon.com",
                "https://www.ebay.com",
                "https://www.wikipedia.org"
            )
            sharedPreferences.edit().putStringSet("bookmarks", defaultBookmarks).apply()
            // Mark that the first run has completed
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }

        val bookmarks = sharedPreferences.getStringSet("bookmarks", mutableSetOf())?.toMutableList() ?: mutableListOf()
        adapter = UrlAdapter(bookmarks, { url ->
            sharedPreferences.edit().putString("pending_url", url).apply()
            Toast.makeText(requireContext(), "已选择 $url，回到主页后将加载", Toast.LENGTH_SHORT).show()
        }, { url ->
            bookmarks.remove(url)
            sharedPreferences.edit().putStringSet("bookmarks", bookmarks.toSet()).apply()
            adapter.notifyDataSetChanged()
        })

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val simulateGithubClick = arguments?.getBoolean("simulate_github_click", false) ?: false
        if (simulateGithubClick) {
            val githubUrl = "https://github.com"
            sharedPreferences.edit().putString("pending_url", githubUrl).apply()
            Toast.makeText(requireContext(), "已选择 $githubUrl，回到主页后将加载", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    fun setWebViewController(controller: MainActivity.WebViewController) {
        this.webViewController = controller
    }
}
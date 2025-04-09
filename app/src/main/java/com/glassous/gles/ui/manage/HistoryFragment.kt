package com.glassous.gles.ui.manage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.glassous.gles.MainActivity
import com.glassous.gles.R

class HistoryFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var btnClearHistory: android.widget.Button
    private lateinit var btnToggleHistory: android.widget.Button
    private lateinit var btnBack: android.widget.Button
    private lateinit var historyAdapter: UrlAdapter
    private var webViewController: MainActivity.WebViewController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        rvHistory = root.findViewById(R.id.rv_history)
        btnClearHistory = root.findViewById(R.id.btn_clear_history)
        btnToggleHistory = root.findViewById(R.id.btn_toggle_history)
        btnBack = root.findViewById(R.id.btn_back)

        val sharedPreferences = requireContext().getSharedPreferences("WebViewData", Context.MODE_PRIVATE)

        rvHistory.layoutManager = LinearLayoutManager(context)
        val history = sharedPreferences.getStringSet("history", mutableSetOf())?.toMutableList() ?: mutableListOf()
        history.sortByDescending { it.split("|")[0] }
        historyAdapter = UrlAdapter(
            history,
            onItemClick = { url ->
                if (webViewController == null) {
                    Toast.makeText(requireContext(), "WebViewController 未初始化", Toast.LENGTH_SHORT).show()
                } else {
                    val actualUrl = url.split("|").last()
                    webViewController?.loadUrl(actualUrl)
                    // Navigate to HomeFragment
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_activity_main, com.glassous.gles.ui.home.HomeFragment())
                        .addToBackStack(null)
                        .commit()
                }
            },
            onDeleteClick = { url ->
                history.remove(url)
                sharedPreferences.edit().putStringSet("history", history.toSet()).apply()
                historyAdapter.updateData(history)
            }
        )
        rvHistory.adapter = historyAdapter

        btnClearHistory.setOnClickListener {
            history.clear()
            sharedPreferences.edit().putStringSet("history", history.toSet()).apply()
            historyAdapter.updateData(history)
        }

        val historyEnabled = sharedPreferences.getBoolean("history_enabled", true)
        btnToggleHistory.text = if (historyEnabled) {
            getString(R.string.toggle_history_off)
        } else {
            getString(R.string.toggle_history_on)
        }
        btnToggleHistory.setOnClickListener {
            val newState = !historyEnabled
            sharedPreferences.edit().putBoolean("history_enabled", newState).apply()
            btnToggleHistory.text = if (newState) {
                getString(R.string.toggle_history_off)
            } else {
                getString(R.string.toggle_history_on)
            }
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return root
    }

    fun setWebViewController(controller: MainActivity.WebViewController) {
        this.webViewController = controller
    }
}
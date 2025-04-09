package com.glassous.gles.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.glassous.gles.MainActivity
import com.glassous.gles.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: Button
    private lateinit var btnForward: Button
    private lateinit var btnRefresh: Button
    private val defaultUrl = "https://www.bing.com"
    private var lastRecordedUrl: String? = null
    private var isInitialLoad = true

    private val cookieManager by lazy { CookieManager.getInstance() }
    private val domains = listOf("https://www.bing.com", "https://login.live.com")
    private val cookiePrefsName = "WebViewCookies"
    private val dataPrefsName = "WebViewData"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("HomeFragment", "onCreateView called")
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        // 初始化视图
        webView = root.findViewById(R.id.webView)
        progressBar = root.findViewById(R.id.progressBar)
        btnBack = root.findViewById(R.id.btn_back)
        btnForward = root.findViewById(R.id.btn_forward)
        btnRefresh = root.findViewById(R.id.btn_refresh)

        // 配置 Cookie
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        // 恢复 Cookie
        restoreCookies()

        // 配置 WebView
        setupWebView()

        // 设置按钮监听器
        btnBack.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        btnForward.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        btnRefresh.setOnClickListener { webView.reload() }

        // 初始按钮状态
        updateButtonStates()

        // 初始加载
        if (isInitialLoad) {
            val dataPreferences = requireContext().getSharedPreferences(dataPrefsName, Context.MODE_PRIVATE)
            val initialUrl = dataPreferences.getString("default_url", defaultUrl) ?: defaultUrl
            Log.d("HomeFragment", "Loading initial URL: $initialUrl")
            webView.loadUrl(initialUrl)
            isInitialLoad = false
        }

        return root
    }

    private fun restoreCookies() {
        val cookiePreferences = requireContext().getSharedPreferences(cookiePrefsName, Context.MODE_PRIVATE)
        val savedCookies = cookiePreferences.getStringSet("cookies", null) ?: return
        savedCookies.forEach { cookie ->
            domains.forEach { domain ->
                cookieManager.setCookie(domain, cookie) // 仅为相关域名恢复 Cookie
            }
        }
        cookieManager.flush()
    }

    private fun setupWebView() {
        webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("HomeFragment", "onPageFinished called with URL: $url")
                    saveCookiesAndHistory(url)
                    updateButtonStates()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressBar.progress = newProgress
                    progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
                }
            }

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                setSupportZoom(true)
                cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
                databaseEnabled = true
            }

            // 优化 sessionStorage 和 localStorage 的同步
            val jsCode = """
                (function() {
                    const syncStorage = () => {
                        for (let i = 0; i < sessionStorage.length; i++) {
                            let key = sessionStorage.key(i);
                            if (key) localStorage.setItem(key, sessionStorage.getItem(key) || '');
                        }
                    };
                    window.addEventListener('beforeunload', syncStorage);
                    for (let i = 0; i < localStorage.length; i++) {
                        let key = localStorage.key(i);
                        if (key) sessionStorage.setItem(key, localStorage.getItem(key) || '');
                    }
                })();
            """.trimIndent()
            evaluateJavascript(jsCode, null)
        }
    }

    private fun saveCookiesAndHistory(url: String?) {
        val cookiePreferences = requireContext().getSharedPreferences(cookiePrefsName, Context.MODE_PRIVATE)
        val dataPreferences = requireContext().getSharedPreferences(dataPrefsName, Context.MODE_PRIVATE)

        // 优化 Cookie 存储：仅保存有效且唯一的 Cookie
        val cookieSet = mutableSetOf<String>()
        domains.forEach { domain ->
            cookieManager.getCookie(domain)?.split(";")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let {
                cookieSet.addAll(it)
            }
        }
        if (cookieSet.isNotEmpty()) {
            cookiePreferences.edit().putStringSet("cookies", cookieSet).apply()
        }

        url?.let {
            (activity as? MainActivity)?.onUrlChanged(it)
            val historyEnabled = dataPreferences.getBoolean("history_enabled", true)
            if (historyEnabled && it != lastRecordedUrl) {
                val history = dataPreferences.getStringSet("history", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                // 限制历史记录大小，避免无限增长
                if (history.size >= 100) history.remove(history.first())
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                history.add("$timestamp|$it")
                dataPreferences.edit().putStringSet("history", history).apply()
                lastRecordedUrl = it
            }
        }
    }

    private fun updateButtonStates() {
        btnBack.isEnabled = webView.canGoBack()
        btnForward.isEnabled = webView.canGoForward()
    }

    override fun onResume() {
        super.onResume()
        val dataPreferences = requireContext().getSharedPreferences(dataPrefsName, Context.MODE_PRIVATE)
        dataPreferences.getString("pending_url", null)?.let { pendingUrl ->
            Log.d("HomeFragment", "Loading pending URL: $pendingUrl")
            webView.loadUrl(pendingUrl)
            dataPreferences.edit().remove("pending_url").apply()
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onPause() {
        super.onPause()
        saveCookiesAndHistory(webView.url)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("HomeFragment", "onDestroyView called")
        webView.destroy()
        isInitialLoad = true
    }

    fun getCurrentUrl(): String = webView.url ?: defaultUrl

    fun loadUrl(newUrl: String) {
        Log.d("HomeFragment", "Loading URL: $newUrl")
        webView.loadUrl(newUrl)
        lastRecordedUrl = null
    }
}
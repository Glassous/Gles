package com.glassous.gles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.glassous.gles.databinding.ActivityMainBinding
import com.glassous.gles.ui.home.HomeFragment
import com.glassous.gles.ui.manage.ManageFragment
import com.glassous.gles.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var homeFragment: HomeFragment
    private lateinit var manageFragment: ManageFragment
    private lateinit var settingsFragment: SettingsFragment
    private var currentFragment: Fragment? = null

    interface WebViewController {
        fun getCurrentUrl(): String
        fun loadUrl(url: String)
        fun onUrlChanged(url: String)
    }

    private val webViewController = object : WebViewController {
        override fun getCurrentUrl(): String {
            return homeFragment.getCurrentUrl()
        }

        override fun loadUrl(url: String) {
            homeFragment.loadUrl(url)
        }

        override fun onUrlChanged(url: String) {
            manageFragment.onUrlChanged(url)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        homeFragment = HomeFragment()
        manageFragment = ManageFragment().apply {
            setWebViewController(webViewController)
        }
        settingsFragment = SettingsFragment()

        supportFragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment_activity_main, homeFragment, "home")
            .hide(homeFragment)
            .add(R.id.nav_host_fragment_activity_main, manageFragment, "manage")
            .hide(manageFragment)
            .add(R.id.nav_host_fragment_activity_main, settingsFragment, "settings")
            .hide(settingsFragment)
            .commit()

        showFragment(homeFragment)

        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    showFragment(homeFragment)
                    true
                }
                R.id.navigation_manage -> {
                    showFragment(manageFragment)
                    true
                }
                R.id.navigation_settings -> {
                    showFragment(settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    fun showFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        if (currentFragment != null && currentFragment != fragment) {
            transaction.hide(currentFragment!!)
        }
        transaction.show(fragment).commit()
        currentFragment = fragment
        if (fragment.isResumed) {
            fragment.onResume()
        }
    }

    fun onUrlChanged(url: String) {
        webViewController.onUrlChanged(url)
    }
}
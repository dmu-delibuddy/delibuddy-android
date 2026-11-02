package com.example.delibuddy

import android.os.Bundle
import androidx.core.view.isVisible
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.delibuddy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_chat,
                R.id.navigation_recommendation,
                R.id.navigation_mypage
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateActionBarVisibility(destination.id)
            navView.isVisible = destination.id !in setOf(
                R.id.navigation_auth,
                R.id.navigation_signup,
                R.id.navigation_signin,
            )
        }

        navView.itemIconTintList = null
    }

    private fun updateActionBarVisibility(destinationId: Int) {
        when (destinationId) {
            R.id.navigation_auth,
            R.id.navigation_signin,
            R.id.navigation_signup -> supportActionBar?.hide()
            else -> supportActionBar?.show()
        }
    }
}

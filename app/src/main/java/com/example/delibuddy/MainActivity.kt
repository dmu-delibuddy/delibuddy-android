package com.example.delibuddy

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.core.view.isVisible
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        applySystemBarPadding()

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

        showLogoInActionBar()
    }

    private fun updateActionBarVisibility(destinationId: Int) {
        when (destinationId) {
            R.id.navigation_auth,
            R.id.navigation_signin,
            R.id.navigation_signup -> supportActionBar?.hide()
            else -> supportActionBar?.show()
        }
    }

        // 액션바의 "홈" 같은 제목 대신 DELI-BUDDY 로고를 가운데에 표시
        private fun showLogoInActionBar() {
                supportActionBar?.apply {
                        setDisplayShowTitleEnabled(false)
                        setDisplayShowCustomEnabled(true)
                        setCustomView(
                                layoutInflater.inflate(R.layout.actionbar_logo, null),
                                ActionBar.LayoutParams(
                                        ActionBar.LayoutParams.WRAP_CONTENT,
                                        ActionBar.LayoutParams.WRAP_CONTENT,
                                        Gravity.CENTER
                                )
                        )
                }
        }

        // targetSdk 36 은 화면이 상태바 뒤부터 그려짐(edge-to-edge)
        // -> 위쪽 여백 = 상태바 높이 + 액션바 높이
        private fun applySystemBarPadding() {
                val tv = TypedValue()
                theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
                val actionBarSize = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)

                ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
                        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                        v.setPadding(bars.left, bars.top, bars.right, 0)
                        insets
                }
        }
}


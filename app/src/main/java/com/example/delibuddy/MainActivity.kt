package com.example.delibuddy

import android.os.Bundle
import android.view.Gravity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.ActionBar
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
        applySystemBarInsets()

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
                R.id.navigation_auth_entry,
                R.id.navigation_signup,
                R.id.navigation_email,
                R.id.navigation_password,
                R.id.navigation_signin,
                R.id.navigation_create_room,
            )
        }

        navView.itemIconTintList = null

        showLogoInActionBar()
    }

    private fun applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom,
            )
            // 자식 뷰에서 시스템 바 여백을 중복 적용하지 않도록 처리
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun updateActionBarVisibility(destinationId: Int) {
        when (destinationId) {
            R.id.navigation_auth_entry,
            R.id.navigation_signin,
            R.id.navigation_email,
            R.id.navigation_password,
            R.id.navigation_signup,
            R.id.navigation_create_room -> supportActionBar?.hide()
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

}


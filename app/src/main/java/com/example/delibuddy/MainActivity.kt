package com.example.delibuddy

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.delibuddy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets()

        val bottomNavigation: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavigation.isVisible = destination.id !in setOf(
                R.id.navigation_startup,
                R.id.navigation_auth_entry,
                R.id.navigation_signup,
                R.id.navigation_email,
                R.id.navigation_password,
                R.id.navigation_birth_date,
                R.id.navigation_account_info,
                R.id.navigation_signin,
                R.id.navigation_create_room,
                R.id.navigation_create_room_info,
                R.id.navigation_create_room_food,
                R.id.navigation_create_room_place,
                R.id.navigation_create_room_amount,
                R.id.navigation_room_detail,
                R.id.navigation_chat_room,
            )
        }

        // 이거 xml코드에 넣고 싶어 미치겠는데 xml에 넣으면 적용이 안됨...
        // 진짜 뭐지.......
        bottomNavigation.itemIconTintList = null

    }

    private fun applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = maxOf(bars.bottom, ime.bottom),
            )

            // 자식 뷰에서 시스템 바 여백을 중복 적용하지 않도록 처리
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }

        ViewCompat.requestApplyInsets(binding.root)
    }
}

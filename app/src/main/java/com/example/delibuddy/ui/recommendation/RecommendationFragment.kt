package com.example.delibuddy.ui.recommendation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentRecommendationBinding
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

class RecommendationFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentRecommendationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecommendationViewModel by viewModels()
    private var naverMap: NaverMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecommendationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMap()
    }

    private fun setUpMap() {
        val existingFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? MapFragment
        val mapFragment = existingFragment ?: MapFragment.newInstance().also {
            childFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
        }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.delibuddy.ui.recommendation

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.delibuddy.R
import com.example.delibuddy.data.recommendation.RecommendLocation
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.databinding.FragmentRecommendationBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import java.util.Locale

class RecommendationFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentRecommendationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RecommendationViewModel by viewModels()
    private var naverMap: NaverMap? = null
    private val activeMarkers = mutableListOf<Marker>()

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
        val existingFragment = childFragmentManager.findFragmentById(binding.mapFragment.id) as? MapFragment
        val mapFragment = existingFragment ?: MapFragment.newInstance().also {
            childFragmentManager.beginTransaction().add(binding.mapFragment.id, it).commit()
        }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locale = Locale.KOREAN

        val dongyangUnivLatLng = LatLng(37.500744352377076, 126.86737286093494)
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(dongyangUnivLatLng, 15.5)
        naverMap.moveCamera(cameraUpdate)

        // 2. ViewModel 장소 데이터 관찰 및 마커 표시
        viewModel.locations.observe(viewLifecycleOwner) { locations ->
            displayMarkers(locations)
        }
        viewModel.loadLocations(requireContext())
    }

    private fun displayMarkers(locations: List<RecommendLocation>) {
        val map = naverMap ?: return

        activeMarkers.forEach { it.map = null }
        activeMarkers.clear()

        val markerIcon = OverlayImage.fromResource(R.drawable.location_marker)
        val density = resources.displayMetrics.density
        val markerWidth = (55 * density).toInt()
        val markerHeight = (62 * density).toInt()

        locations.forEach { location ->
            if ((location.lat != 0.0) && (location.lon != 0.0)) {
                val marker = Marker().apply {
                    position = LatLng(location.lat, location.lon)
                    icon = markerIcon
                    width = markerWidth
                    height = markerHeight
                    // 마커 이미지의 하단 중앙(0.5f, 1.0f)을 정확한 GPS 좌표 핀 위치로 앵커 설정
                    anchor = PointF(0.5f, 1.0f)
                    captionText = location.name
                    setOnClickListener {
                        showLocationBottomSheet(location)
                        true
                    }
                    this.map = map
                }
                activeMarkers.add(marker)
            }
        }
    }

    private fun showLocationBottomSheet(location: RecommendLocation) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_location_info, binding.root, false)

        val textRecommendBadge = view.findViewById<TextView>(R.id.text_recommend_badge)
        val textLocationName = view.findViewById<TextView>(R.id.text_location_name)
        val textLocationDescription = view.findViewById<TextView>(R.id.text_location_description)
        val imageLocationPhoto = view.findViewById<ImageView>(R.id.image_location_photo)
        val buttonCreateRoom = view.findViewById<Button>(R.id.button_create_room_here)

        if (textRecommendBadge != null) {
            when (location.mode) {
                RoomMode.PICKUP_ONLY -> {
                    textRecommendBadge.text = getString(R.string.mode_pickup)
                    textRecommendBadge.setBackgroundResource(R.drawable.bg_badge_orange)
                }
                RoomMode.MEAL_BUDDY -> {
                    textRecommendBadge.text = getString(R.string.mode_meal_buddy)
                    textRecommendBadge.setBackgroundResource(R.drawable.bg_badge_yellow)
                }
            }
        }

        textLocationName?.text = location.name
        textLocationDescription?.text = location.description

        dialog.setContentView(view)
        dialog.show()

        if (imageLocationPhoto != null) {
            Glide.with(this)
                .load(location.imageUrl.takeIf { it.isNotBlank() })
                .placeholder(R.drawable.delibuddy_pizza_icon)
                .fallback(R.drawable.delibuddy_pizza_icon)
                .error(R.drawable.delibuddy_pizza_icon)
                .into(imageLocationPhoto)
        }

        buttonCreateRoom?.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activeMarkers.forEach { it.map = null }
        activeMarkers.clear()
        _binding = null
    }
}

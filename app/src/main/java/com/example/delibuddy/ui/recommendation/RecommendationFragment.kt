package com.example.delibuddy.ui.recommendation

import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.delibuddy.R
import com.example.delibuddy.data.recommendation.RecommendLocationModel
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.databinding.BottomSheetLocationInfoBinding
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
        val existingFragment =
            childFragmentManager.findFragmentById(binding.mapFragment.id) as? MapFragment
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

    private fun displayMarkers(locations: List<RecommendLocationModel>) {
        val map = naverMap ?: return

        activeMarkers.forEach { it.map = null }
        activeMarkers.clear()

        val markerIcons = RoomMode.entries.associateWith { mode ->
            OverlayImage.fromResource(mode.markerDrawable())
        }
        val density = resources.displayMetrics.density
        val markerWidth = (24 * density).toInt()
        val markerHeight = (32 * density).toInt()

        locations.forEach { location ->
            if ((location.lat != 0.0) && (location.lon != 0.0)) {
                val marker = Marker().apply {
                    position = LatLng(location.lat, location.lon)
                    icon = markerIcons.getValue(location.mode)
                    width = markerWidth
                    height = markerHeight
                    // 마커 이미지의 하단 중앙(0.5f, 1.0f)을 정확한 GPS 좌표 핀 위치로 앵커 설정
                    anchor = PointF(0.5f, 1.0f)
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

    private fun RoomMode.markerDrawable(): Int = when (this) {
        RoomMode.MEAL_BUDDY -> R.drawable.meal_friend_marker
        RoomMode.PICKUP_ONLY -> R.drawable.pickup_only_marker
    }

    private fun showLocationBottomSheet(location: RecommendLocationModel) {
        val dialog = BottomSheetDialog(requireContext())
        val sheet = BottomSheetLocationInfoBinding.inflate(dialog.layoutInflater)

        sheet.textRecommendBadge.root.displayedChild = if (location.mode == RoomMode.MEAL_BUDDY) 1 else 0

        sheet.textLocationName.text = location.name
        sheet.buttonCreateRoomHere.setOnClickListener { dialog.dismiss() }
        dialog.setContentView(sheet.root)
        dialog.show()

        val imageUrl = location.imageUrl.takeIf { it.isNotBlank() }
        sheet.progressLocationPhoto.isVisible = imageUrl != null
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.recommend_location_image_skeletion)
            .fallback(R.drawable.delibuddy_pizza_icon)
            .error(R.drawable.delibuddy_pizza_icon)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean,
                ): Boolean {
                    sheet.progressLocationPhoto.isVisible = false
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    sheet.progressLocationPhoto.isVisible = false
                    return false
                }
            })
            .into(sheet.imageLocationPhoto)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activeMarkers.forEach { it.map = null }
        activeMarkers.clear()
        _binding = null
    }
}

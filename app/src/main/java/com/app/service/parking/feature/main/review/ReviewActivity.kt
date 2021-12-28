package com.app.service.parking.feature.main.review

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.ViewModelProvider
import com.app.service.parking.R
import com.app.service.parking.custom.dialog.NaviBottomSheetDialog
import com.app.service.parking.databinding.ActivityReviewBinding
import com.app.service.parking.feature.base.BaseActivity
import com.app.service.parking.model.dto.Lot
import com.app.service.parking.model.repository.local.db.AppDB
import com.app.service.parking.model.repository.local.repository.FavoriteRepository
import com.app.service.parking.util.MarkerManager
import com.bumptech.glide.Glide
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import timber.log.Timber


class ReviewActivity : BaseActivity<ActivityReviewBinding, ReviewViewModel>() {

    override val layoutResId: Int = R.layout.activity_review
    override val viewModel: ReviewViewModel by lazy {
        ViewModelProvider(
            this,
            ReviewViewModel.Factory(FavoriteRepository(AppDB.getDatabase(this)))
        )[ReviewViewModel::class.java]
    }
    lateinit var mapView: MapView // 카카오 맵 뷰
    private var naviBottomSheetDialog: NaviBottomSheetDialog? = null
    var mapViewContainer: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActivity()
    }

    override fun initActivity() {
        setParkModel() // Intent로 받은 주차장 데이터로 초기화
        setBindingData() // 데이터 바인딩 설정
        initView() // 뷰 초기화
    }

    override fun finish() {
        mapViewContainer?.removeView(mapView) // 종료할 때 맵뷰 제거 (맵뷰 2개 이상 동시에 불가)
        super.finish()
    }

    private fun initView() {
        initMapView()
        with(binding) {

            // 즐겨찾기 여부에 따른 아이콘 설정
            viewModel?.isFavorite?.observe(this@ReviewActivity) { isFavorite ->
                if (isFavorite) { // 즐겨찾기에 추가되어 있으면
                    Glide.with(this@ReviewActivity).load(R.drawable.ic_review_favorite)
                        .into(favoriteImageView)
                } else {
                    Glide.with(this@ReviewActivity).load(R.drawable.ic_review_unfavorite)
                        .into(favoriteImageView)
                }
            }

            // 즐겨찾기 Entity가 DB에 있으면 '즐겨찾기를 추가한 주차장 데이터'이므로
            // isFavorite를 true로 설정하고, 없으면 false로 설정한다.
            viewModel?.favoriteLot?.observe(this@ReviewActivity) { favoriteEntity ->
                viewModel?.isFavorite?.value = (favoriteEntity != null)
            }

            favoriteButton.setOnClickListener {
                // 즐겨찾기가 추가되어 있는 경우
                if (viewModel?.isFavorite?.value == true) {
                    // 즐겨찾기 여부 변경
                    viewModel?.isFavorite?.value = false
                    // 즐겨찾기 해제
                    viewModel?.favoriteLot?.value?.let { entity -> viewModel?.deleteLot(entity) }
                } else { // 즐겨찾기 해제되어 있는 경우
                    // 즐겨찾기 여부 변경
                    viewModel?.isFavorite?.value = true
                    // 즐겨찾기 추가
                    viewModel?.insertLot()
                }

            }

            // 전화버튼 클릭리스너
            callButton.setOnClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("tel:${model?.phoneNumber}")
                    )
                )
            }
            // 주소 복사 버튼 클릭
            copyAddressButton.setOnClickListener {
                val clipboard: ClipboardManager =
                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

                var text = when {
                    model?.newAddr.isNullOrBlank().not() -> {
                        model?.newAddr
                    }
                    model?.oldAddr.isNullOrBlank().not() -> {
                        model?.oldAddr
                    }
                    else -> {
                        // 주차장 주소가 없음
                        showToast(getString(R.string.clipboard_failed)) // 실패 토스트 출력
                        return@setOnClickListener
                    }
                }
                // 클리보드 데이터 객체 생성
                val clipData = ClipData.newPlainText(
                    getString(R.string.clipboard_label),
                    text
                )
                // 클립보드에 저장
                clipboard.setPrimaryClip(clipData)
                // 성공 토스트 출력
                showToast(getString(R.string.clipboard_success))
            }
            // 내비게이션 버튼 클릭
            navigationButton.setOnClickListener {
                if (naviBottomSheetDialog == null) {
                    naviBottomSheetDialog = NaviBottomSheetDialog(this@ReviewActivity).apply {
                        binding.dialog = this // 다이얼로그 데이터 바인딩
                        setAddress(model?.oldAddr)
                    }
                }
                naviBottomSheetDialog?.show()
            }
            // 길찾기 버튼
            findRoadButton.setOnClickListener {
                val uri = Uri.parse("geo:${model?.latitude},${model?.longitude}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }

            if (model?.newAddr.isNullOrBlank()) {
                binding.parkingLotRoadNameTextView.visibility = View.GONE
            }

            writeReviewButton.setOnClickListener {
                startActivity(Intent(this@ReviewActivity, ReviewWriteActivity::class.java))
            }
        }
    }

    override fun onBackPressed() {
        // 내비게이션 선택 다이얼로그가 열려있으면
        if (naviBottomSheetDialog?.isShown == true) {
            naviBottomSheetDialog?.dismiss() // 다이얼로그를 닫고,
        } else { // 아무것도 열려있지 않으면
            super.onBackPressed() // 뒤로가기
        }
    }

    private fun setParkModel() {
        // Search Activity에서 검색 데이터로 받은 주차장 모델로 초기화
        viewModel.lotModel = intent.getSerializableExtra("model") as Lot
        // 주차장 코드로 즐겨찾기 모델 가져오기
        viewModel.getByParkCode()
    }

    private fun setBindingData() {
        binding.model = viewModel.lotModel // 데이터바인딩 모델 세팅
        binding.viewModel = viewModel // ViewModel 바인딩
    }

    private fun initMapView() {
        // 지도 위치 이동을 위한 좌표
        val latitude = viewModel.lotModel?.latitude?.toDouble() // 위도
        val longitude = viewModel.lotModel?.longitude?.toDouble() // 경도
        val markerManager = MarkerManager()
        // 카카오 맵 뷰 초기화
        if (intent.getBooleanExtra("isShowMap", true)) {
            try {
                mapView = MapView(this).also {
                    // 위치 데이터가 존재한다면
                    if (latitude != null && longitude != null) {
                        mapViewContainer = RelativeLayout(this)
                        mapViewContainer?.layoutParams = RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        binding.kakaoMapContainer.addView(mapViewContainer)
                        mapViewContainer?.addView(it)

                        markerManager.removeAllMarkers(it) // 기존 마커 모두 제거
                        it.setMapCenterPoint(
                            MapPoint.mapPointWithGeoCoord(latitude, longitude),
                            false
                        ) // 해당 좌표로 지도 중심 이동
                        it.currentLocationTrackingMode =
                            MapView.CurrentLocationTrackingMode.TrackingModeOff // 지도 고정을 위해 트래킹 모드 종료
                        val marker = markerManager.createMarker(
                            type = viewModel.lotModel?.feeType, // 요금 타입
                            fee = viewModel.lotModel?.basicFeeWon, // 기본 요금
                            latitude = latitude,
                            longitude = longitude
                        )
                        markerManager.addMarker(it, marker)
                    }
                    it.setOnTouchListener { _, _ -> true } // 지도 터치 방지
                }
            }catch (re: RuntimeException){
                Timber.tag("에러발생").d(re.toString())
                Timber.e(re.toString())
            }

        } else {
            binding.kakaoMapContainer.visibility = View.GONE
        }
    }
}
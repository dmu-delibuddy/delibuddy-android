package com.example.delibuddy.ui.room.create
import android.app.TimePickerDialog
import java.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.annotation.StringRes
import androidx.core.widget.doAfterTextChanged
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentCreateRoomBinding
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.data.room.FoodCostSplitMethod
import com.example.delibuddy.data.room.DeliveryFeeSplitMethod
import com.example.delibuddy.ui.room.create.CreateRoomViewModel.SubmitState



/*
    * 방 만들기.
    * - 상단 < : 방 만들기 종료
    * - 이전 / 시스템 뒤로가기 : 이전 단계 (1단계에서는 종료)
*/

class CreateRoomFragment : Fragment() {

    private var _binding: FragmentCreateRoomBinding? = null
    private val binding get() = _binding!!
    private val viewModel : CreateRoomViewModel by viewModels()

    private lateinit var backCallback : OnBackPressedCallback // 2단계 이상일 때만 켜져서 시스템 뒤로가기를 이전 단계로 바꿈

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 화면 툴
        setUpBackPress()
        setUpTopBar()
        setUpBottomButtons()
        observeStep()

        // 1단계 : 방 정보
        setUpTitleInput()
        setUpParticipantButtons()
        observeParticipants()
        setUpModeSelect()

        // 2단계 : 음식 정보
        setUpCategorySpinner()
        setUpRestaurantInput()

        // 3단계 : 마감 시간
        setUpDeadlineBox()
        observeDeadline()
        setUpNoTimeLimitCheck()
        observeNoTimeLimit()

        // 4단계 : 금액, 정산
        setUpAmountInputs()
        setUpFoodSplitSelect()
        observeFoodSplit()
        setUpDeliverySplitSelect()
        observeFeeNote()

        // 개설
        observeSubmitState()
    }

    private fun setUpBackPress() {
        backCallback = requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, enabled = false) { viewModel.prev() }
    }

    private fun setUpTopBar() {
        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
    }

    // 하단 이전 / 다음(개설) 버튼 클릭 연결
    private fun setUpBottomButtons() {
        binding.buttonPrev.setOnClickListener {
            viewModel.prev()
        }
        binding.buttonNext.setOnClickListener {
            onNextClick()
        }
    }

    // 다음 버튼: 마지막 단계면 개설, 아니면 검사 후 다음 단계
    private fun onNextClick() {
        val step = viewModel.step.value ?: 1
        if (step == CreateRoomViewModel.LAST_STEP) {
            viewModel.submit()
        } else {
            goNextIfValid(step)
        }
    }

    // 현재 단계 입력 검사. 문제 있으면 토스트, 없으면 다음 단계
    private fun goNextIfValid(step: Int) {
        val error = viewModel.validateStep(step)
        if (error != null) showToast(error)
        else viewModel.next()
    }

    // 개설 상태가 바뀔 때마다 버튼 모양 + 결과 처리
    private fun observeSubmitState() {
        viewModel.submitState
            .observe(viewLifecycleOwner) { state ->
                renderSubmitButtons(state)
                handleSubmitResult(state)
            }
    }

    // 저장 중엔 이전 / 다음 버튼 잠그고 "개설 중…" 표시
    private fun renderSubmitButtons(
        state: SubmitState
    ) {
        val loading = state is SubmitState.Loading
        binding.buttonNext.isEnabled = !loading
        binding.buttonPrev.isEnabled = !loading
        binding.buttonNext.setText(
            if (loading) R.string.create_submitting
            else nextButtonText()
        )
    }

    // 현재 단계에 맞는 다음 버튼 글자 (마지막 단계만 "개설하기")
    private fun nextButtonText(): Int {
        val step = viewModel.step.value ?: 1
        val last = CreateRoomViewModel.LAST_STEP
        return if (step == last) {
            R.string.create_room_submit
        } else {
            R.string.create_room_next
        }
    }

    // 성공: 토스트 후 홈으로 / 실패: 안내 토스트 후 상태 초기화
    private fun handleSubmitResult(
        state: SubmitState
    ) {
        when (state) {
            is SubmitState.Success -> {
                showToast(R.string.create_success)
                findNavController().popBackStack()
            }
            is SubmitState.Error -> {
                showToast(state.message)
                viewModel.clearSubmitError()
            }
            else -> Unit
        }
    }

    // 문자열 리소스로 짧은 토스트 띄우기
    private fun showToast(@StringRes message: Int) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun observeStep() {
        viewModel.step.observe(viewLifecycleOwner) { step -> renderStep(step) }
    }

    // 단계에 맞게 상단 1/4, 진행 막대, 보이는 페이지, 하단 버튼을 바꿈
    private fun renderStep(step: Int) {
        val last = CreateRoomViewModel.LAST_STEP
        val progressBars = listOf(binding.progress1, binding.progress2, binding.progress3, binding.progress4)
        val pages = listOf(binding.step1.root, binding.step2.root, binding.step3.root, binding.step4.root)

        binding.textStep.text = "$step/$last"
        progressBars.forEachIndexed { i, bar ->
                bar.setBackgroundResource(if (i < step) R.drawable.bg_progress_on else R.drawable.bg_progress_off)
        }
        pages.forEachIndexed { i, page ->
            page.visibility = if (i == step - 1) View.VISIBLE else View.GONE
        }
        binding.buttonPrev.visibility = if (step > 1) View.VISIBLE else View.GONE
        binding.buttonNext.setText(if (step == last) R.string.create_room_submit else R.string.create_room_next)
        binding.scrollContent.scrollTo(0, 0)
        backCallback.isEnabled = step > 1
    }


    // 1단계 : 방 정보
    private fun setUpTitleInput() {
        binding.step1.editTitle.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            viewModel.setTitle(value)
            binding.step1.textTitleCount.text = "${value.length}/${CreateRoomViewModel.TITLE_MAX}"
        }
    }

    private fun setUpParticipantButtons() {
        binding.step1.buttonMinus.setOnClickListener { viewModel.decreaseParticipants() }
        binding.step1.buttonPlus.setOnClickListener { viewModel.increaseParticipants() }
    }

    // 인원 표시 + 2명 ~ 6명 끝에서 -/+ 버튼 비활성
    private fun observeParticipants() {
        viewModel.maxParticipants.observe(viewLifecycleOwner) { n ->
            val step1 = binding.step1
            step1.textParticipants.text = "${n}명"
            step1.buttonMinus.isEnabled = n > CreateRoomViewModel.MIN_PARTICIPANTS
            step1.buttonPlus.isEnabled = n < CreateRoomViewModel.MAX_PARTICIPANTS
            step1.buttonMinus.alpha = if (step1.buttonMinus.isEnabled) 1f else 0.3f
            step1.buttonPlus.alpha = if (step1.buttonPlus.isEnabled) 1f else 0.3f
        }
    }

    private fun setUpModeSelect() {
        binding.step1.groupCreateMode.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setMode(
                if (checkedId == R.id.radio_create_meal_buddy) RoomMode.MEAL_BUDDY else RoomMode.PICKUP_ONLY
            )
        }
    }


    // 2단계 : 음식 정보
    // 0번 = null , 1번부터 FoodCategory 순서
    private fun setUpCategorySpinner() {
        val categories = FoodCategory.entries
        val labels = listOf(getString(R.string.create_category_hint)) +
                resources.getStringArray(R.array.create_category_labels)

        binding.step2.spinnerCategory.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, labels
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.step2.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setCategory(if (position == 0) null else categories[position - 1])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setUpRestaurantInput() {
        binding.step2.editRestaurant.doAfterTextChanged { text ->
            viewModel.setRestaurant(text?.toString().orEmpty())
        }
    }


    // 3단계 : 마감 시간
    private fun setUpDeadlineBox() {
        binding.step3.boxDeadline.setOnClickListener { showDeadlinePicker() }
    }

    // 이미 고른 시각이 있으면 그 시각, 없으면 지금 + 1시간으로 시계 창 열기
    private fun showDeadlinePicker() {
        val saved = viewModel.deadlineMinutes.value
        val now = Calendar.getInstance()
        val initHour = saved?.div(60) ?: ((now.get(Calendar.HOUR_OF_DAY) + 1) % 24)
        val initMinute = saved?.rem(60) ?: 0
        TimePickerDialog(requireContext(), { _, hour, minute ->
            viewModel.setDeadline(hour, minute)
        }, initHour, initMinute, true).show()
    }

    private fun observeDeadline() {
        viewModel.deadlineMinutes.observe(viewLifecycleOwner) { minutes ->
            binding.step3.textDeadline.text =
                minutes?.let { "%02d:%02d 마감".format(it / 60, it % 60) } ?: ""
        }
    }


    private fun setUpNoTimeLimitCheck() {
        binding.step3.checkNoTimeLimit.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNoTimeLimit(isChecked)
        }
    }

    // 시간 제한 없음이면 마감 시간 칸을 흐리게 + 못 누르게
    private fun observeNoTimeLimit() {
        viewModel.noTimeLimit.observe(viewLifecycleOwner) { noLimit ->
            binding.step3.boxDeadline.isEnabled = !noLimit
            binding.step3.boxDeadline.alpha = if (noLimit) 0.4f else 1f
        }
    }


    // 4단계 : 금액, 정산
    private fun setUpAmountInputs() {
        binding.step4.editMinOrder.doAfterTextChanged {
            viewModel.setMinOrderAmount(it?.toString()?.toIntOrNull())
        }
        binding.step4.editDeliveryFee.doAfterTextChanged {
            viewModel.setDeliveryFee(it?.toString()?.toIntOrNull())
        }
    }

    private fun setUpFoodSplitSelect() {
        val ids = mapOf(
            R.id.radio_food_host to FoodCostSplitMethod.HOST_PAYS,
            R.id.radio_food_equal to FoodCostSplitMethod.EQUAL_SPLIT,
            R.id.radio_food_by_menu to FoodCostSplitMethod.PROPORTIONAL_SPLIT,
            R.id.radio_food_custom to FoodCostSplitMethod.CUSTOM,
        )
        binding.step4.groupFoodSplit.setOnCheckedChangeListener { _, id ->
            ids[id]?.let { viewModel.setFoodCostSplit(it) }
        }
    }

    // 고른 음식값 정산 방법의 설명 한 줄
    private fun observeFoodSplit() {
        viewModel.foodCostSplit.observe(viewLifecycleOwner) { method ->
            binding.step4.textFoodSplitDesc.setText(
                when (method) {
                    FoodCostSplitMethod.HOST_PAYS -> R.string.split_food_desc_host
                    FoodCostSplitMethod.EQUAL_SPLIT -> R.string.split_food_desc_equal
                    FoodCostSplitMethod.PROPORTIONAL_SPLIT -> R.string.split_food_desc_by_menu
                    FoodCostSplitMethod.CUSTOM -> R.string.split_food_desc_custom
                }
            )
        }
    }

    private fun setUpDeliverySplitSelect() {
        val ids = mapOf(
            R.id.radio_delivery_host to DeliveryFeeSplitMethod.HOST_PAYS,
            R.id.radio_delivery_equal to DeliveryFeeSplitMethod.EQUAL_SPLIT,
            R.id.radio_delivery_custom to DeliveryFeeSplitMethod.CUSTOM,
        )
        binding.step4.groupDeliverySplit.setOnCheckedChangeListener { _, id ->
            ids[id]?.let { viewModel.setDeliveryFeeSplit(it) }
        }
    }

    // 배달비, 인원, 배달비 정산 방법 중 하나라도 바뀌면 1인당 배달비 안내를 다시 계산
    private fun observeFeeNote() {
        viewModel.deliveryFee.observe(viewLifecycleOwner) { renderFeeNote() }
        viewModel.maxParticipants.observe(viewLifecycleOwner) { renderFeeNote() }
        viewModel.deliveryFeeSplit.observe(viewLifecycleOwner) { renderFeeNote() }
    }

    private fun renderFeeNote() {
        val fee = viewModel.deliveryFee.value
        val people = viewModel.maxParticipants.value ?: CreateRoomViewModel.DEFAULT_PARTICIPANTS
        binding.step4.textFeeNote.text = when (viewModel.deliveryFeeSplit.value) {
            DeliveryFeeSplitMethod.HOST_PAYS -> getString(R.string.create_fee_note_host)
            DeliveryFeeSplitMethod.CUSTOM -> getString(R.string.create_fee_note_custom)
            else -> if (fee == null) "" else
                getString(R.string.create_fee_note_equal, people, "%,d".format(fee / people))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
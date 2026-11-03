package com.example.delibuddy.ui.room.create

import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.core.widget.doAfterTextChanged
import com.example.delibuddy.data.room.RoomMode
import com.example.delibuddy.R
import com.example.delibuddy.databinding.FragmentCreateRoomBinding
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.delibuddy.data.room.FoodCategory
import com.example.delibuddy.data.room.FoodCostSplitMethod
import com.example.delibuddy.data.room.DeliveryFeeSplitMethod



/*
    * 방 만들기.
    * - 상단 < : 방 만들기 종료
    * - 이전 / 시스템 뒤로가기 : 이전 단계 (1단계에서는 종료)
*/

class CreateRoomFragment : Fragment() {

    private var _binding: FragmentCreateRoomBinding? = null
    private val binding get() = _binding!!

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

        val viewModel = ViewModelProvider(this)[CreateRoomViewModel::class.java]

        val progress =
            listOf(binding.progress1, binding.progress2, binding.progress3, binding.progress4)
        val steps =
            listOf(binding.step1.root, binding.step2.root, binding.step3.root, binding.step4.root)

        // 시스템 뒤로가기 : 2단계 이상이면 이전 단계로 (1단계에서는 꺼져 있어서 화면 종료)
        val backCallback = requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, enabled = false) { viewModel.prev() }

        // 단계가 바뀔 때마다 화면 갱신
        viewModel.step.observe(viewLifecycleOwner) { step ->
            val last = CreateRoomViewModel.LAST_STEP
            binding.textStep.text = "$step/$last"
            progress.forEachIndexed { i, bar ->
                bar.setBackgroundResource(if (i < step) R.drawable.bg_progress_on else R.drawable.bg_progress_off)
            }
            steps.forEachIndexed { i, page ->
                page.visibility = if (i == step - 1) View.VISIBLE else View.GONE
            }
            binding.buttonPrev.visibility = if (step > 1) View.VISIBLE else View.GONE
            binding.buttonNext.setText(if (step == last) R.string.create_room_submit else R.string.create_room_next)
            binding.scrollContent.scrollTo(0, 0)
            backCallback.isEnabled = step > 1
        }

        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
        binding.buttonPrev.setOnClickListener { viewModel.prev() }
        binding.buttonNext.setOnClickListener {
            if (viewModel.step.value == CreateRoomViewModel.LAST_STEP) {
                // 입력 검사 + createRoom 연결 예정
                Toast.makeText(requireContext(), "방 개설은 다음 작업에서 연결", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.next()
            }
        }

        // 1단계 : 방 정보
        val step1 = binding.step1

        // 방 제목 : 입력할 때마다 저장 + 글자 수 표시
        step1.editTitle.doAfterTextChanged { text ->
            val value = text?.toString().orEmpty()
            viewModel.setTitle(value)
            step1.textTitleCount.text = "${value.length}/${CreateRoomViewModel.TITLE_MAX}"
        }

        // 최대 참가자 수 : - / + (2 ~ 6명)
        step1.buttonMinus.setOnClickListener { viewModel.decreaseParticipants() }
        step1.buttonPlus.setOnClickListener { viewModel.increaseParticipants() }
        viewModel.maxParticipants.observe(viewLifecycleOwner) { n ->
            step1.textParticipants.text = "${n}명"
            step1.buttonMinus.isEnabled = n > CreateRoomViewModel.MIN_PARTICIPANTS
            step1.buttonPlus.isEnabled = n < CreateRoomViewModel.MAX_PARTICIPANTS
            step1.buttonMinus.alpha = if (step1.buttonMinus.isEnabled) 1f else 0.3f
            step1.buttonPlus.alpha = if (step1.buttonPlus.isEnabled) 1f else 0.3f
        }

        // 모드 선택
        step1.groupCreateMode.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setMode(
                if (checkedId == R.id.radio_create_meal_buddy) RoomMode.MEAL_BUDDY else RoomMode.PICKUP_ONLY
            )
        }

        // 2단계 : 음식 정보
        val step2 = binding.step2

        // 카테고리 : 0번 - "선택해주세요"(null), 1번부터 FoodCategory 순서 (한식, 중식, 일식, 양식, 기타)
        val categories = FoodCategory.entries
        val categoryLabels = listOf(getString(R.string.create_category_hint)) +
                resources.getStringArray(R.array.create_category_labels)
        step2.spinnerCategory.adapter = ArrayAdapter (
            requireContext(), android.R.layout.simple_spinner_item, categoryLabels
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        step2.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.setCategory(if (position == 0) null else categories[position - 1])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        // 음식점
        step2.editRestaurant.doAfterTextChanged { text ->
            viewModel.setRestaurant(text?.toString().orEmpty())
        }

        // 3단계 : 장소, 마감 시간
        val step3 = binding.step3

        // 마감 시간 칸 -> 시계 창 (24시간) | 처음 열 때는 지금 시각 + 1시간
        step3.boxDeadline.setOnClickListener {
            val saved = viewModel.deadlineMinutes.value
            val now = Calendar.getInstance()
            val initHour = saved?.div(60) ?: ((now.get(Calendar.HOUR_OF_DAY) + 1) % 24)
            val initMinute = saved?.rem(60) ?: 0
            TimePickerDialog(requireContext(), { _, hour, minute ->
                viewModel.setDeadline(hour, minute)
            }, initHour, initMinute, true).show()
        }

        viewModel.deadlineMinutes.observe(viewLifecycleOwner) { minutes ->
            step3.textDeadline.text = minutes?.let { "%02d:%02d 마감".format(it / 60, it % 60) } ?:""
        }

        // 시간 제한 없음 : 체그하면 마감 시간 칸을 흐리게 + 못 누르게
        step3.checkNoTimeLimit.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNoTimeLimit(isChecked)
        }
        viewModel.noTimeLimit.observe(viewLifecycleOwner) { noLimit ->
            step3.boxDeadline.isEnabled = !noLimit
            step3.boxDeadline.alpha = if (noLimit) 0.4f else 1f
        }

        // 4단계 : 금액, 정산
        val step4 = binding.step4

        // 금액 : 숫자로 바꿔 저장 (비어 있으면 null)
        step4.editMinOrder.doAfterTextChanged { viewModel.setMinOrderAmount(it?.toString()?.toIntOrNull()) }
        step4.editDeliveryFee.doAfterTextChanged { viewModel.setDeliveryFee(it?.toString()?.toIntOrNull()) }

        // 음식값 정산 방법 : 버튼 id -> enum
        val foodSplitIds = mapOf (
            R.id.radio_food_host to FoodCostSplitMethod.HOST_PAYS,
            R.id.radio_food_equal to FoodCostSplitMethod.EQUAL_SPLIT,
            R.id.radio_food_by_menu to FoodCostSplitMethod.PROPORTIONAL_SPLIT,
            R.id.radio_food_custom to FoodCostSplitMethod.CUSTOM,
        )
        step4.groupFoodSplit.setOnCheckedChangeListener { _, id ->
            foodSplitIds[id]?.let { viewModel.setFoodCostSplit(it) }
        }
        viewModel.foodCostSplit.observe(viewLifecycleOwner) { method ->
            step4.textFoodSplitDesc.setText (
                when (method) {
                    FoodCostSplitMethod.HOST_PAYS -> R.string.split_food_desc_host
                    FoodCostSplitMethod.EQUAL_SPLIT -> R.string.split_food_desc_equal
                    FoodCostSplitMethod.PROPORTIONAL_SPLIT -> R.string.split_food_desc_by_menu
                    FoodCostSplitMethod.CUSTOM -> R.string.split_food_desc_custom
                }
            )
        }

        // 배달비 정산 방법 : 버튼 id -> enum
        val deliverySplitIds = mapOf (
            R.id.radio_delivery_host to DeliveryFeeSplitMethod.HOST_PAYS,
            R.id.radio_delivery_equal to DeliveryFeeSplitMethod.EQUAL_SPLIT,
            R.id.radio_delivery_custom to DeliveryFeeSplitMethod.CUSTOM,
        )
        step4.groupDeliverySplit.setOnCheckedChangeListener { _, id ->
            deliverySplitIds[id]?.let { viewModel.setDeliveryFeeSplit(it) }
        }

        // 1인당 예상 배달비 안내 : 배달비, 인원, 배달비 정산 방법 중 하나라도 바뀌면 다시 계산
        fun updateFeeNote() {
            val fee = viewModel.deliveryFee.value
            val people = viewModel.maxParticipants.value ?: CreateRoomViewModel.DEFAULT_PARTICIPANTS
            step4.textFeeNote.text = when (viewModel.deliveryFeeSplit.value) {
                DeliveryFeeSplitMethod.HOST_PAYS -> getString(R.string.create_fee_note_host)
                DeliveryFeeSplitMethod.CUSTOM -> getString(R.string.create_fee_note_custom)
                else -> if (fee == null) "" else
                    getString(R.string.create_fee_note_equal, people, "%,d".format(fee / people))
            }
        }
        viewModel.deliveryFee.observe(viewLifecycleOwner) { updateFeeNote() }
        viewModel.maxParticipants.observe(viewLifecycleOwner) { updateFeeNote() }
        viewModel.deliveryFeeSplit.observe(viewLifecycleOwner) { updateFeeNote() }

        // 하단 탭이 없는 화면이라 버튼이 제스처 바에 가리지 않게 아래 여백 추가
        val baseBottom = binding.layoutBottom.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutBottom) { v, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            v.updatePadding(bottom = baseBottom + navBar)
            insets
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
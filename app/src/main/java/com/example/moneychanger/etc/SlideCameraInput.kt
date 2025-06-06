package com.example.moneychanger.etc

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.moneychanger.databinding.SlideCameraInputBinding
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.product.CreateProductRequestDto
import com.example.moneychanger.network.product.CreateProductResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.example.moneychanger.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SlideCameraInput(
    var onProductAddedListener: ((CreateProductResponseDto) -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var _binding: SlideCameraInputBinding? = null
    private val binding get() = _binding!!

    private var listener: OnProductAddedListener? = null

    companion object {
        const val TAG = "SlideCameraInput"
    }

    override fun onStart() {
        super.onStart()

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED // 슬라이드 최대 크기로 시작

            val layoutParams = it.layoutParams
            layoutParams.height = dpToPx(500f).toInt() // 전체 높이 설정
            it.layoutParams = layoutParams
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnProductAddedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnProductAddedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("SlideCameraInput", "onCreateView 실행됨")
        _binding = SlideCameraInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listId = arguments?.getLong("list_id") ?: -1L
        val fromUnit = arguments?.getString("currency_from_unit") ?: ""
        val toUnit = arguments?.getString("currency_to_unit") ?: ""
        val fromKey = fromUnit.replace(Regex("\\(.*\\)"), "").trim()
        val toKey = toUnit.replace(Regex("\\(.*\\)"), "").trim()
        val fromResId = resources.getIdentifier(fromKey, "string", requireContext().packageName)
        val toResId = resources.getIdentifier(toKey, "string", requireContext().packageName)
        val fromSymbol = if (fromResId != 0) getString(fromResId) else fromKey
        val toSymbol = if (toResId != 0) getString(toResId) else toKey

        // 🟢 통화 기호 바인딩
        binding.currencyText.text = fromUnit
        binding.currencySymbol.text = fromSymbol
        binding.currencyText2.text = toUnit
        binding.currencySymbol2.text = toSymbol

        binding.buttonAdd.setOnClickListener {
            val inputName = binding.inputName.text.toString().trim()
            val inputText = binding.inputPrice.text.toString().replace(",", "")
            val quantity = binding.countText.text.toString().toInt()
            val price = inputText.toDoubleOrNull() ?: 0.0

            if (price > 0 && inputName.isNotEmpty()) {
                if (listId != -1L){
                    addProductToList(listId, inputName, quantity, price)
                } else {
                    context?.let {
                        Toast.makeText(it, "리스트를 생성해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
//            if (listId != -1L) {
//                addProductToList(latestListId, productNameCopy, quantity, productPriceCopy)
//            } else {
//                checkAndRequestLocationPermission {
//                    getLocation { address ->
//                        location = address
//                        addNewList(userId, currencyIdFrom, currencyIdTo, location, productNameCopy, productPriceCopy, quantity)
//                    }
//                }
//            }
        }

        var pieces = 1
        binding.countText.text = pieces.toString()
        binding.buttonMinus.setOnClickListener {
            if (pieces > 1) {
                pieces -= 1
                binding.countText.text = pieces.toString()
            }

            if (pieces > 1) {
                binding.rectMinus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.main)
                )
                binding.minusSign.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.main)
                )
            } else {
                binding.rectMinus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.gray_02)
                )
                binding.minusSign.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.gray_02)
                )
            }
            updateChangedText()
        }

        binding.buttonPlus.setOnClickListener {
            pieces += 1
            binding.countText.text = pieces.toString()

            if (pieces > 1) {
                binding.rectMinus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.main)
                )
                binding.minusSign.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.main)
                )
            }else {
                binding.rectMinus.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.gray_03)
                )
                binding.minusSign.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.gray_03)
                )
            }
            updateChangedText()
        }

        binding.inputPrice.doAfterTextChanged {
            updateChangedText()
        }
    }

    fun updateChangedText() {
        val inputText = binding.inputPrice.text.toString().replace(",", "")
        val price = inputText.toDoubleOrNull() ?: 0.0
        val quantity = binding.countText.text.toString().toIntOrNull() ?: 1

        val currencyIdFrom = arguments?.getLong("currency_id_from") ?: -1L
        val currencyIdTo = arguments?.getLong("currency_id_to") ?: -1L


        Log.d("SlideCameraInput", "✅ 환율 요청: $currencyIdFrom → $currencyIdTo")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val exchangeRate = ExchangeRateUtil.getExchangeRate(currencyIdFrom, currencyIdTo)
                Log.d("SlideCameraInput", "📈 환율 응답: $exchangeRate")

                val changedAmount = price * quantity * exchangeRate
                val formatted = String.format("%.2f", changedAmount)

                binding.changedText.text = formatted
            } catch (e: Exception) {
                Log.e("SlideCameraInput", "❌ 환율 계산 실패: ${e.message}")
            }
        }
    }



    private fun addProductToList(listId: Long, productName: String, quantity:Int, price: Double) {
        val productRequest = CreateProductRequestDto(listId, productName, quantity, price)

        RetrofitClient.apiService.createProduct(productRequest)
            .enqueue(object : Callback<ApiResponse<CreateProductResponseDto>> {
                override fun onResponse(
                    call: Call<ApiResponse<CreateProductResponseDto>>,
                    response: Response<ApiResponse<CreateProductResponseDto>>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        context?.let {
                            Toast.makeText(it, "상품 추가 완료!", Toast.LENGTH_SHORT).show()
                        }

                        val addedProduct = response.body()?.data
                        Log.d("SlideCameraInput", "추가된 상품: $addedProduct")

                        if (addedProduct != null) {
                            Log.d("SlideCameraInput", "추가된 상품 콜백 전달")
                            onProductAddedListener?.invoke(addedProduct)  // ★ 콜백 호출
                            dismiss()
                        }
                    } else {
                        Log.e("SlideCameraInput", "상품 추가 실패: ${response.body()?.message}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CreateProductResponseDto>>, t: Throwable) {
                    Log.e("SlideCameraInput", "서버 요청 실패: ${t.message}")
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}

interface OnProductAddedListener {
    fun onProductAdded(productName: String, price: Double)
}

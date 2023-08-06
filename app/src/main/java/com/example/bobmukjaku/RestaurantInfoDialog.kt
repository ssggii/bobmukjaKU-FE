package com.example.bobmukjaku

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bobmukjaku.Model.RestaurantList
import com.example.bobmukjaku.databinding.FragmentRestaurantInfoDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RestaurantInfoDialog(private val restaurant: RestaurantList) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentRestaurantInfoDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRestaurantInfoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 데이터를 설정하여 UI 업데이트
        binding.restaurantName.text = restaurant.bizesNm
        binding.category.text = restaurant.indsMclsNm
        binding.categorySub.text = restaurant.indsSclsNm
        binding.restaurantAdd.text = restaurant.lnoAdr
    }
}
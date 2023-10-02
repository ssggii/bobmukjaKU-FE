package com.example.bobmukjaku

import android.annotation.SuppressLint
import android.util.Log
import android.widget.NumberPicker
import android.widget.TimePicker

class CustomTimePicker {
    companion object{
        //TimePicker의 interval를 변경하는 메서드
        const val DEFAULT_INTERVAL = 5
        const val MINUTES_MIN = 0
        const val MINUTES_MAX = 60

        @SuppressLint("PrivateApi")
        fun TimePicker.setTimeInterval(
            @androidx.annotation.IntRange(from = 0, to = 30)
            timeInterval: Int = DEFAULT_INTERVAL
        ){
            try{
                val classForId = Class.forName("com.android.internal.R\$id")
                val fieldId = classForId.getField("minute").getInt(null)

                (this.findViewById(fieldId) as NumberPicker).apply {
                    minValue = MINUTES_MIN
                    maxValue = MINUTES_MAX / timeInterval - 1
                    displayedValues = getDisplayedValue(timeInterval)
                }
            }catch (e: java.lang.Exception){

                Log.i("timepick", e.printStackTrace().toString())
            }
        }

        private fun getDisplayedValue(
            @androidx.annotation.IntRange(from = 0, to = 30)
            timeInterval: Int = DEFAULT_INTERVAL
        ): Array<String>{
            val minutesArray = ArrayList<String>()
            for(i in 0 until MINUTES_MAX step timeInterval){
                minutesArray.add(i.toString())
            }
            return minutesArray.toArray(arrayOf(""))
        }

        fun TimePicker.getDisplayedMinute(
            @androidx.annotation.IntRange(from = 0, to = 30)
            timeInterval: Int = DEFAULT_INTERVAL
        ): Int = minute * timeInterval * 2
    }
}
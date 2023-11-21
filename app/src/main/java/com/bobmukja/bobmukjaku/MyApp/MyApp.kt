package com.bobmukja.bobmukjaku.MyApp

import android.content.Context


class MyApp {

    companion object{
        private var context: Context? = null

        fun getAppContext():Context?{
            return this.context
        }

        fun setAppContext(context: Context){
            this.context = context
        }
    }
}
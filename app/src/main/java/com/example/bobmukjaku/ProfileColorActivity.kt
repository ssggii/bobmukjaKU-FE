package com.example.bobmukjaku

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.bobmukjaku.databinding.ActivityProfileColorBinding

class ProfileColorActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileColorBinding
    private var selectedButtonId = -1 // 현재 선택된 배경 버튼

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileColorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun colorChoice() {
        val buttonIds = arrayOf(
            R.id.bg1, R.id.bg2, R.id.bg3, R.id.bg4, R.id.bg5, R.id.bg6, R.id.bg7, R.id.bg8, R.id.bg9,
            R.id.bg10, R.id.bg11, R.id.bg12, R.id.bg13, R.id.bg14, R.id.bg15, R.id.bg16, R.id.bg17, R.id.bg18
        )

        for (buttonId in buttonIds) {
            val button = findViewById<ImageButton>(buttonId)
            button.setOnClickListener {
                if (selectedButtonId != buttonId) {
                    setUnselectedBackgrounds()
                    setSelectedBackground(buttonId) // 선택한 버튼 배경 테두리 나타남
                    selectedButtonId = buttonId
                }
            }
        }
    }

    private fun setUnselectedBackgrounds() {
        val buttonIds = arrayOf(
            R.id.bg1, R.id.bg2, R.id.bg3, R.id.bg4, R.id.bg5, R.id.bg6, R.id.bg7, R.id.bg8, R.id.bg9,
            R.id.bg10, R.id.bg11, R.id.bg12, R.id.bg13, R.id.bg14, R.id.bg15, R.id.bg16, R.id.bg17, R.id.bg18
        )

        for (buttonId in buttonIds) {
            val button = findViewById<ImageButton>(buttonId)
            val resourceName = "bg" + resources.getResourceEntryName(buttonId).removePrefix("bg").toInt()
            val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
            button.setBackgroundResource(resourceId)
        }
    }

    private fun setSelectedBackground(selectedId: Int) {
        val button = findViewById<ImageButton>(selectedId)
        val resourceName = "btn_bg" + resources.getResourceEntryName(selectedId).removePrefix("bg").toInt()
        val resourceId = resources.getIdentifier(resourceName, "drawable", packageName)
        button.setBackgroundResource(resourceId)
    }

    private fun init() {
        colorChoice()

        binding.changeButton.setOnClickListener {
            val intent = Intent()
            intent.putExtra("selectedItemId", R.id.forth)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}
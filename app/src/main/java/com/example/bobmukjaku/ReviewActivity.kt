package com.example.bobmukjaku

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.bobmukjaku.databinding.ActivityReviewBinding

@Suppress("DEPRECATION")
class ReviewActivity : AppCompatActivity() {
    lateinit var binding: ActivityReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initLayout()
    }

    private fun initLayout() {

        binding.apply{
            imageBtn.setOnClickListener {
                //해당 버튼을 누르면 갤러리or사진촬영으로 리뷰이미지를 획득 후 파이어베이스에 업로드?
                cameraAction()
            }
        }
    }

    private fun cameraAction() {
        val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(cameraPermissionGranted()){
            captureActivityLauncher.launch(i)
        }
        else{
            checkCameraPermission()
        }
    }

    val captureActivityLauncher
    = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK && it.data != null){
            Log.i("cameraData", it.data?.extras.toString())
            val bundle = it.data?.extras
            val image = bundle?.get("data") as Bitmap
            binding.reviewImg.setImageBitmap(image)
        }
    }

    val cameraPermissionLauncher
    = registerForActivityResult(ActivityResultContracts.RequestPermission()){
    }

    private fun checkCameraPermission() {
        when{
            (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED)
                    ->{

                    }
            (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA))
                    ->{
                        alertCheckPermissionDlg()
                    }
            else
                ->{
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
        }
    }

    private fun alertCheckPermissionDlg() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("카메라 이슈")
            .setMessage("카메라 권한 필수")
            .setPositiveButton("OK"){
                _,_->
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            .setNegativeButton("취소"){
                dlg,_->
                dlg.dismiss()
            }

        val dlg = builder.create()
        dlg.show()
    }

    private fun cameraPermissionGranted(): Boolean =
        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED

}
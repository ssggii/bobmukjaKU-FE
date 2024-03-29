package com.bobmukja.bobmukjaku

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bobmukja.bobmukjaku.Model.*
import com.bobmukja.bobmukjaku.MyApp.MyApp
import com.bobmukja.bobmukjaku.databinding.ActivityReviewBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

@Suppress("DEPRECATION")
class ReviewActivity : AppCompatActivity() {
    lateinit var binding: ActivityReviewBinding

    var bitmapForUpload: Bitmap? = null//업로드할 사진의 bitmap을 저장
    val myInfo by lazy {
        intent.getSerializableExtra("myInfo") as Member
    }
    val restaurantId by lazy {
        intent.getStringExtra("restaurantId")
    }
    val restaurantName by lazy {
        intent.getStringExtra("restaurantName")
    }
    val roomId by lazy{
        intent.getLongExtra("roomId", -1)
    }
    //val roomId = 1
    val accessToken = "Bearer ".plus(SharedPreferences.getString("accessToken", "") ?: null)

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if(doubleBackToExitPressedOnce){
            //super.onBackPressed()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "'뒤로가기'를 한번 더 누르면 메인화면으로 갑니다.", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyApp.setAppContext(this)
        Log.i("yyy", restaurantName.toString() + "/" + restaurantId.toString())
        Log.i("myInfo", myInfo.toString())//내정보 잘 전달받았는지 확인용
        //getRestaurantInfo()
        initLayout()
    }

    /*@RequiresApi(Build.VERSION_CODES.Q)
    private fun getRestaurantInfo() {
        //파이어베이스에 저장되어있는 공지로부터 음식점정보를 가져온다.
        val rf = Firebase.database.getReference("chatRoom/$roomId/notice")
        rf.get().addOnCompleteListener {
            if(it.isSuccessful){

                restaurantId = it.result.child("restaurantId").value.toString()
                restaurantName = it.result.child("restaurantName").value.toString()
                //val starttime = it.result.child("starttime").value.toString().toLong()
                Log.i("kim", restaurantName)
                Log.i("kim", restaurantId)
                //Log.i("kim", starttime.toString())

                initLayout()
            }
        }
    }*/


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun initLayout() {

        binding.apply{

            placeName.setText(restaurantName)
            imageBtn.setOnClickListener {
                //해당 버튼을 누르면 갤러리or사진촬영으로 bitmap(리뷰이미지)를 선택
                cameraAction()
            }

            register.setOnClickListener {
                //리뷰를 등록
                if(placeName.text != "") {
                    register.isEnabled = false//버튼 잠구기
                    progressBar.visibility = View.VISIBLE

                    uploadImageToFirebaseStorage()
                }else{
                    Toast.makeText(this@ReviewActivity,"리뷰를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadImageToFirebaseStorage(){
        //이미지는 따로 파이어베이스 스토리지에 업로드 -> 스토리지상 경로를 반환
        if(bitmapForUpload != null){
            val timeStamp = System.currentTimeMillis().toString()
            val fileName = "${myInfo.uid}_${myInfo.memberNickName}_${timeStamp}.jpg"
            val fullPath = "/${restaurantId}/${fileName}"
            val ref = Firebase.storage.reference.child(fullPath)

            val baos = ByteArrayOutputStream()
            bitmapForUpload?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val inputStream = ByteArrayInputStream(baos.toByteArray())

            Log.i("upload", fullPath)
            ref.putStream(inputStream).addOnCompleteListener{result->
                if(result.isSuccessful){
                    Log.i("cameraData", "업로드성공")
                    registerReviewIntoServer(fullPath)
                }
                else{
                    Log.i("cameraData", "업로드실패")
                    binding.progressBar.visibility = View.GONE
                    binding.register.isEnabled = true
                }
            }
        }
        else{
            registerReviewIntoServer("nodata")
        }
    }

    private fun registerReviewIntoServer(path: String) {//리뷰를 등록
        val reviewText = binding.reviewText.text.toString()
        if(reviewText != ""){
            val request = RetrofitClient.restaurantService.addReview(accessToken, ReviewInfo(myInfo.uid!!, restaurantId!!,path,reviewText,restaurantName!!))
            request.enqueue(object: Callback<Void>{
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.i("review", "리뷰 등록 성공")

                    val intent = Intent(this@ReviewActivity, MainActivity::class.java)
                    //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    //finishAffinity()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.i("review", t.message.toString())
                    binding.progressBar.visibility = View.GONE
                    binding.register.isEnabled = true
                    Toast.makeText(this@ReviewActivity, "업로드 실패", Toast.LENGTH_SHORT).show()
                }
            })
        }else{
            Toast.makeText(this, "리뷰를 작성하지 않았습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun cameraAction() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val chooserIntent = Intent.createChooser(intent, "사진을 가져올 방법을 선택")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        if(cameraPermissionGranted()){
            captureActivityLauncher.launch(chooserIntent)
        }
        else{
            checkCameraPermission()
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private val captureActivityLauncher
    = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.i("camera", "test")
        if(it.resultCode == RESULT_OK && it.data != null){
            when{
                (it.data?.data != null)->{
                    //갤러리에서 사진을 가져올 때

                    //uri를 가져와서
                    val contentProvierUri = it.data?.data!!

                    bitmapForUpload = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, contentProvierUri))
                    binding.reviewImg.setImageBitmap(bitmapForUpload)
                }
                (it.data?.extras?.get("data") != null)->{
                    //카메라로 사진을 찍었을 때

                    //카메라로 찍은 사진을 bitmap으로 가져오고
                    bitmapForUpload = it.data?.extras?.get("data") as Bitmap
                    binding.reviewImg.setImageBitmap(bitmapForUpload)
                }
                else->{
                    Log.i("cameraData", "사진을 가져오는데 실패")
                }
            }
        }
    }

    val cameraPermissionLauncher
    = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
    }

    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE)

    private fun checkCameraPermission() {
        when{
            ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED))
                    ->{

                    }
            ((ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA))
                    || (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)))
                    ->{
                        alertCheckPermissionDlg()
                    }
            else
                ->{
                    cameraPermissionLauncher.launch(permissions)
                }
        }
    }

    private fun alertCheckPermissionDlg() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("카메라 이슈")
            .setMessage("카메라 권한 필수")
            .setPositiveButton("OK"){
                _,_->
                cameraPermissionLauncher.launch(permissions)
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
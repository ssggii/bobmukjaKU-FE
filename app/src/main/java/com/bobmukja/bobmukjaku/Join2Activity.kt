package com.bobmukja.bobmukjaku

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bobmukja.bobmukjaku.Hash.Sha256
import com.bobmukja.bobmukjaku.Model.HashedAuthCode
import com.bobmukja.bobmukjaku.Model.RetrofitClient
import com.bobmukja.bobmukjaku.Model.SharedPreferences
import com.bobmukja.bobmukjaku.MyApp.MyApp
import com.bobmukja.bobmukjaku.databinding.ActivityJoin2Binding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.time.LocalDate

class Join2Activity : AppCompatActivity() {
    lateinit var binding: ActivityJoin2Binding
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    lateinit var email: String
    var hashedAuthCodeFromServer:String = ""
    val sha256 = Sha256()

    private var toast1: Toast? = null
    private var toast2: Toast? = null
    private var toast3: Toast? = null
    private var toast4: Toast? = null


    private val BASE_URL = "http://192.168.219.107:8080/"
    lateinit var retrofit: Retrofit

    val TAG = "kim"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        MyApp.setAppContext(this)
        /*auth = Firebase.auth
        initLayout()
        setupAuthStateListener()*/


        initLayout()

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initLayout() {
        binding.sendBtn.setOnClickListener {
            /*서버에게 이메일인증을 요청*/


            //건국대학교 메일주소만 입력받도록 필터링
            email = binding.inputEmail.text.toString()
            val regex = """^[a-zA-Z0-9]+@konkuk\.ac\.kr$""".toRegex()
            if(!regex.matches(email)) {
                //Log.i("kim", "학교이메일아님")
                toast2?.cancel()
                toast2 = Toast.makeText(this@Join2Activity, "건국대학교 이메일이 아닙니다.", Toast.LENGTH_SHORT)
                toast2?.show()
                return@setOnClickListener
            }

            //1. 서버에 이메일인증 요청
            //2. 응답값에서 인증코드 해시값을 꺼내와서 hashedAuthCodeFromServer변수에 저장
            val request = RetrofitClient.memberService.RequestMailAuth(email)
            CoroutineScope(Dispatchers.IO).launch {
                request.enqueue(object : Callback<HashedAuthCode> {
                    override fun onResponse(
                        call: Call<HashedAuthCode>,
                        response: Response<HashedAuthCode>
                    ) {

                        if(response.isSuccessful) {

                            hashedAuthCodeFromServer = response.body()!!.hashedAuthCode

                            binding.layoutInputAuthcode.visibility = View.VISIBLE
                            binding.join2Button.visibility = View.VISIBLE
                            binding.sendBtn.text = "이메일 재전송"
                            binding.emailSend.text =
                                "6자리 인증코드를 입력해주세요.\n혹시 이메일이 오지 않았다면 이메일 재전송 버튼을 눌러주세요."
                        }
                    }

                    override fun onFailure(call: Call<HashedAuthCode>, t: Throwable) {
                        toast3?.cancel()
                        toast3 = Toast
                            .makeText(this@Join2Activity
                                , "이메일 인증 요청을 실패하였습니다.\n 다시 재학생 인증 버튼을 눌러주세요."
                                , Toast.LENGTH_SHORT)
                        toast3?.show()
                    }
                })
            }
        }

        binding.join2Button.setOnClickListener {
            //서버에서 인증코드 해시값을 받았다는 전제
            //입력한 인증코드의 해시값과 서버로부터의 해시값을 비교 -> 일치하면 mainActivity(메인화면)으로

            //먼저 인증코드 입력폼의 숫자들을 모아서 하나의 문자열로 만든다.
            var authCodeFromUser = ""
            authCodeFromUser= authCodeFromUser
                .plus(binding.num1.text.toString())
                .plus(binding.num2.text.toString())
                .plus(binding.num3.text.toString())
                .plus(binding.num4.text.toString())
                .plus(binding.num5.text.toString())
                .plus(binding.num6.text.toString())

            Log.i("kim", authCodeFromUser)

            if(authCodeFromUser.length != 6){
                toast1?.cancel()
                toast1 = Toast.makeText(this, "6자리의 인증코드를 제대로 입력하세요", Toast.LENGTH_SHORT)
                toast1?.show()
                return@setOnClickListener
            }


            val hashedAuthCodeFromUser = sha256.encrypt(authCodeFromUser)
            if(hashedAuthCodeFromServer != ""){
                when{
                    (hashedAuthCodeFromUser == hashedAuthCodeFromServer)->{
                        //Log.i("kim", "일치합니다.")

                        if(intent.getStringExtra("mode") == "changePassword"){
                            Log.i("changePassword", "yes")
                            val intent = Intent(this@Join2Activity, ChangePasswordActivity2::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                        }
                        else if(intent.getBooleanExtra("alreadyJoin", false)) {//로그인버튼을 눌러 재학생인증 화면으로 왔다면 메인 화면으로,
                            //인증날짜 갱신
                            val authorization = "Bearer ${SharedPreferences.getString("accessToken", "")}"
                            val currentDate = LocalDate.now().toString()

                            val requestBody = mapOf("certificatedAt" to  currentDate)
                            RetrofitClient.memberService.updateMember(authorization,requestBody)
                                .enqueue(object:Callback<Void>{
                                    override fun onResponse(
                                        call: Call<Void>,
                                        response: Response<Void>
                                    ) {
                                        //서버에 인증날짜 갱신했으므로 메인화면으로
                                        Toast.makeText(this@Join2Activity, "재학생 재인증 성공", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@Join2Activity, MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        Toast.makeText(this@Join2Activity, "인증날짜 갱신 실패", Toast.LENGTH_SHORT).show()
                                    }
                                })

                        }else{//회원가입 버튼을 눌러 재학생 인증 화면으로 왔다면 회원가입 화면으로
                            val intent = Intent(this@Join2Activity, JoinActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                        }
                    }
                    else->{
                        //Log.i("kim", "불일치합니다.")
                        toast4?.cancel()
                        toast4 = Toast.makeText(this@Join2Activity, "인증코드가 일치하지 않습니다.", Toast.LENGTH_SHORT)
                        toast4?.show()
                    }
                }
            }
        }

        //인증코드 입력할 때 자동으로 다음 EditText로 넘어가도록
        binding.num1.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num1.setText("")
            }
        }
        binding.num1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.num1.length() == 1){
                    binding.num2.requestFocus()
                }
            }
        })
        binding.num2.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.num2.length() == 1){
                    binding.num3.requestFocus()
                }
            }
        })
        binding.num2.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num2.setText("")
            }
        }
        binding.num3.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.num3.length() == 1){
                    binding.num4.requestFocus()
                }
            }
        })
        binding.num3.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num3.setText("")
            }
        }
        binding.num4.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.num4.length() == 1){
                    binding.num5.requestFocus()
                }
            }
        })
        binding.num4.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num4.setText("")
            }
        }
        binding.num5.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.num5.length() == 1){
                    binding.num6.requestFocus()
                }
            }
        })
        binding.num5.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num5.setText("")
            }
        }
        binding.num6.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                when(binding.num6.text.toString().length){
                    0->binding.join2Button.background= ContextCompat
                        .getDrawable(this@Join2Activity,
                            R.drawable.btn_green_off)

                    1->binding.join2Button.background= ContextCompat
                        .getDrawable(this@Join2Activity,
                            R.drawable.btn_green)
                }
            }

        })
        binding.num6.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.num6.setText("")
            }
        }
    }
}
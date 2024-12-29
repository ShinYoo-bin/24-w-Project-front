package com.example.moneychanger

import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.moneychanger.databinding.ActivityLoginBinding
import com.example.moneychanger.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : TextView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        //아이디 패스워드 찾기
        binding.buttonFindIdPw.paintFlags = binding.buttonFindIdPw.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.buttonFindIdPw.setOnClickListener {
            // 아이디 패스워드 찾기로 연결
        }

        //회원가입
        binding.buttonSignUp.paintFlags = binding.buttonSignUp.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.buttonSignUp.setOnClickListener {
            // 회원가입 페이지로 연결
        }

    }
}
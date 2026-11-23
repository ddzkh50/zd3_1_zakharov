package com.example.zd3_2_zakharov

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer

class MainActivity : Activity() {

    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                if (isFinishing) return
                startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                finish()
            }
        }.start()
    }

    override fun onDestroy() {
        timer?.cancel()
        timer = null
        super.onDestroy()
    }
}

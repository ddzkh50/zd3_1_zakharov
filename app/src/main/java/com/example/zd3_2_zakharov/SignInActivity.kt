package com.example.zd3_2_zakharov

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class SignInActivity : Activity() {

    private lateinit var email: EditText
    private lateinit var password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)

        findViewById<Button>(R.id.login).apply {
            setOnClickListener { login() }
            requestFocus()
        }
    }

    private fun login() {
        val emailText = email.text.toString().trim()
        val passwordText = password.text.toString()

        if (emailText.isNotEmpty() && passwordText.isNotEmpty()) {
            startActivity(Intent(this, ChannelsActivity::class.java))
            finish()
        } else {
            AlertDialog.Builder(this)
                .setTitle(R.string.error_title)
                .setMessage(R.string.error_empty_fields)
                .setPositiveButton("OK", null)
                .create()
                .show()
        }
    }
}

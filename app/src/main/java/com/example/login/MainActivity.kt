package com.example.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.login.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private var phoneNumber: String = ""
    private lateinit var firebaseAuth: FirebaseAuth

    lateinit var verificationID: String
    lateinit var mToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.verifyOTPButton.isEnabled = false
        viewBinding.verifyOTPButton.isClickable = false

        firebaseAuth = FirebaseAuth.getInstance()
        viewBinding.sendOTPButton.setOnClickListener { login() }
        viewBinding.verifyOTPButton.setOnClickListener {
            val otp = viewBinding.OTPEditText.text.trim().toString()
            if(otp.isNotEmpty()){
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationID, otp)
                signInWithCredential(credential)
            }else{
                Toast.makeText(this, "Enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }

        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Toast.makeText(this@MainActivity, "Sign-in is successful", Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationFailed(exc: FirebaseException) {
                Log.e("LoginApp","Can't verify OTP: ", exc)
            }

            override fun onCodeSent(verificationID: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationID, token)
                this@MainActivity.verificationID = verificationID
                mToken = token
                viewBinding.verifyOTPButton.isEnabled = true
                viewBinding.verifyOTPButton.isClickable = true
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
                Toast.makeText(this@MainActivity, "OTP timeout!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(){
        phoneNumber = viewBinding.phoneEditText.text.trim().toString()
        if(phoneNumber.isNotEmpty()){
            phoneNumber = "+91${phoneNumber}"
            sendVerificationCode(phoneNumber)
        }else{
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendVerificationCode(number: String){
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("LoginApp", "Auth started")
    }

    private fun signInWithCredential(credential: PhoneAuthCredential){
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful){
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    intent.putExtra("NAME",viewBinding.nameEditText.text.trim().toString())
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this, "Sign-in failed:(", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
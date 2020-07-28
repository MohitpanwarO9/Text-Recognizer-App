package com.example.phoneauthentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_verify_phone.*
import java.util.concurrent.TimeUnit

class VerifyPhone : AppCompatActivity() {

    private var verificationId:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone)

        sendBt.setOnClickListener {

            progressBar.visibility=View.VISIBLE

            val phone=etPhone.text.toString().trim()
            if(phone.isEmpty()){
                etPhone.error="Enter a valid"
                etPhone.requestFocus()
                return@setOnClickListener

            }

            val phoneNumber= "+91$phone"

            PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(
                    phoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    this,
                    phoneAuthCallbacks
                )

        }

        verify.setOnClickListener {

            val code=Edcode.text.toString().trim()

            if (code.isEmpty()){
                Edcode.error="Code Require"
                Edcode.requestFocus()
                return@setOnClickListener
            }

            verificationId?.let {
                val credential=PhoneAuthProvider.getCredential(it,code)
                addPhoneNumber(credential)
            }

        }


    }

    private val phoneAuthCallbacks= object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            addPhoneNumber(phoneAuthCredential)
            progressBar.visibility=View.GONE
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            progressBar.visibility=View.GONE
            toast()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, token)

            this@VerifyPhone.verificationId=verificationId

        }

    }


    private fun addPhoneNumber(it: PhoneAuthCredential){

        FirebaseAuth.getInstance().signInWithCredential(it)
            .addOnCompleteListener {
                task ->
                if (task.isSuccessful){
                    Toast.makeText(this,"login successfully ",Toast.LENGTH_LONG).show()

                    val intent=Intent(this,MainActivity::class.java)
                    intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                }
                else
                {
                      Toast.makeText(this,"login fail",Toast.LENGTH_LONG).show()
                }
            }

    }

    private fun toast() {
        Toast.makeText(this,"fail login",Toast.LENGTH_LONG).show()
    }

}
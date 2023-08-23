package com.example.meddirect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.meddirect.databinding.ActivitySignInBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val signUpText: TextView = binding.signUpTextView
        val signUpTextMessage: TextView = binding.signUpMessageTextView
        val signInEmail: EditText = binding.signInEmailEditText
        val signInPassword: EditText = binding.signInPasswordEditText
        val signInButton: androidx.appcompat.widget.AppCompatButton = binding.logInButton
        val signInPasswordLayout: TextInputLayout = binding.signInPasswordInstructions

        //if user touches on sign up text, start signup activity
        signUpText.setOnClickListener {
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }
        signUpTextMessage.setOnClickListener {
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }

        //if user presses log in button
        signInButton.setOnClickListener {

            val email = signInEmail.text.toString()
            val password = signInPassword.text.toString()
            signInPasswordLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

            if(validateData(signInEmail,signInPassword,signInPasswordLayout)){
                //data is validated

                //authorize the data
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                    if(it.isSuccessful){
                        //sign in successful
                        Toast.makeText(this,"Signed In Successfully!",Toast.LENGTH_LONG).show()
                        val intent = Intent(this,HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        //wrong details entered by user
                        Toast.makeText(this,"Invalid email/password, try again!",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    private fun validateData(signInEmail: EditText, signInPassword: EditText,signInPasswordLayout: TextInputLayout): Boolean {
        val email = signInEmail.text.toString()
        val password = signInPassword.text.toString()


        if(email.isEmpty()){
            signInEmail.error = "Enter your email"
            Toast.makeText(this,"Email Address Missing!", Toast.LENGTH_SHORT).show()
            return false
        }
        if(!email.matches(emailPattern.toRegex())){
            signInEmail.error = "Enter valid Email Address"
            Toast.makeText(this,"Email address is not valid",Toast.LENGTH_SHORT).show()
            return false
        }
        //email address is now validated


        if(password.isEmpty()){
            signInPasswordLayout.endIconMode = TextInputLayout.END_ICON_NONE
            signInPassword.error = "Enter your password"
            Toast.makeText(this,"Enter your password!",Toast.LENGTH_SHORT).show()
            return false
        }
        if(password.length < 6){
            signInPasswordLayout.endIconMode = TextInputLayout.END_ICON_NONE
            signInPassword.error = "Enter password more than 6 characters"
            Toast.makeText(this,"Password is more than 6 characters!",Toast.LENGTH_SHORT).show()
            return false
        }
        //password pattern is now validated

        return true
    }
}
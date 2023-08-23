package com.example.meddirect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.meddirect.databinding.ActivitySignUpBinding
import com.example.meddirect.model.User
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val signUpName: EditText = binding.signUpNameEditText
        val signUpEmail: EditText = binding.signUpEmailEditText
        val signUpPassword: EditText = binding.signUpPasswordEditText
        val signUpConfirmPassword: EditText = binding.signUpConfirmPasswordEditText
        val signUpPasswordLayout: TextInputLayout = binding.signUpPasswordInstructions
        val signUpConfirmPasswordLayout: TextInputLayout = binding.signUpConfirmPasswordInstructions
        val signUpButton: androidx.appcompat.widget.AppCompatButton = binding.signUpButton
        val signInText: TextView = binding.signUpTextView
        val signInTextMessage: TextView = binding.signUpMessageTextView


        //Go to sign up page if user click on sign in text
        signInText.setOnClickListener {
            val intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
        signInTextMessage.setOnClickListener {
            val intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        //If sign up button is clicked
        signUpButton.setOnClickListener {

            val name = signUpName.text.toString()
            val email = signUpEmail.text.toString()
            val password = signUpPassword.text.toString()

            signUpPasswordLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            signUpConfirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

            //check if the user has filled all the details
            if(validateData(signUpName,signUpEmail,signUpPassword,
                    signUpConfirmPassword,signUpPasswordLayout,
                    signUpConfirmPasswordLayout))
            {
                //data is validated now, start authentication

                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { it1 ->
                    if(it1.isSuccessful){
                        //user details created
                        //add the user in database
                        val databaseRef = database.reference.child("users").child(auth.currentUser!!.uid)
                        val user = User(name,email,auth.currentUser!!.uid,"","","")
                        databaseRef.setValue(user).addOnCompleteListener {
                            if(it.isSuccessful){
                                //Data Entered Successfully
                                //Move to Sign In Activity
                                Toast.makeText(this,"User Registered Successfully!",Toast.LENGTH_LONG).show()
                                val intent = Intent(this,SignInActivity::class.java)
                                startActivity(intent)
                                finish()
                            }else{
                                Toast.makeText(this,"Something went wrong, try again!",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        Toast.makeText(this,"Something went wrong, try again!",Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
    }

    private fun validateData(signUpName: EditText, signUpEmail: EditText,
                             signUpPassword: EditText, signUpConfirmPassword: EditText,
                             signUpPasswordLayout: TextInputLayout,
                             signUpConfirmPasswordLayout: TextInputLayout
    ): Boolean {
        val name = signUpName.text.toString()
        val email = signUpEmail.text.toString()
        val password = signUpPassword.text.toString()
        val confirmPassword = signUpConfirmPassword.text.toString()

        if(name.isEmpty()){
            signUpName.error = "Enter your name"
            Toast.makeText(this,"Name Missing!",Toast.LENGTH_SHORT).show()
            return false
        }
        if(email.isEmpty()){
            signUpEmail.error = "Enter your email"
            Toast.makeText(this,"Email Address Missing!",Toast.LENGTH_SHORT).show()
            return false
        }
        if(!email.matches(emailPattern.toRegex())){
            signUpEmail.error = "Enter valid Email Address"
            Toast.makeText(this,"Email address is not valid",Toast.LENGTH_SHORT).show()
            return false
        }
        //email pattern is now validated


        if(password.isEmpty()){
            signUpPasswordLayout.endIconMode = TextInputLayout.END_ICON_NONE
            signUpPassword.error = "Enter your password"
            Toast.makeText(this,"Set up a Password!",Toast.LENGTH_SHORT).show()
            return false
        }
        if(password.length < 6){
            signUpPasswordLayout.endIconMode = TextInputLayout.END_ICON_NONE
            signUpPassword.error = "Enter password more than 6 characters"
            Toast.makeText(this,"Password is short!",Toast.LENGTH_SHORT).show()
            return false
        }
        //password pattern is now validated


        if(confirmPassword.isEmpty() || password != confirmPassword){
            signUpConfirmPasswordLayout.endIconMode = TextInputLayout.END_ICON_NONE
            signUpConfirmPassword.error = "Re-enter your password"
            Toast.makeText(this,"Password Not Matched!",Toast.LENGTH_SHORT).show()
            return false
        }
        //password is also confirmed, we are good to go
        return true
    }


}
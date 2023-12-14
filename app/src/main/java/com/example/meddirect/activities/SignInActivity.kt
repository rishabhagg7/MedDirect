package com.example.meddirect.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.airbnb.lottie.LottieAnimationView
import com.example.meddirect.R
import com.example.meddirect.databinding.ActivitySignInBinding
import com.example.meddirect.model.User
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)
        callbackManager = CallbackManager.Factory.create()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val signUpText: TextView = binding.signUpTextView
        val signUpTextMessage: TextView = binding.signUpMessageTextView
        val signInEmail: EditText = binding.signInEmailEditText
        val signInPassword: EditText = binding.signInPasswordEditText
        val signInButton: MaterialButton = binding.logInButton
        val signInPasswordLayout: TextInputLayout = binding.signInPasswordInstructions
        val googleSignInButton: AppCompatImageButton = binding.googleLogInButton
        val facebookSignInButton: AppCompatImageButton = binding.facebookLogInButton

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

        //if user presses google-icon
        googleSignInButton.setOnClickListener {
            val signInClient = googleSignInClient.signInIntent
            launcher.launch(signInClient)
        }

        //if user presses facebook-icon
        facebookSignInButton.setOnClickListener {
            configureFbLogin()
        }
    }

    private fun configureFbLogin() {
        binding.loadingAnimation.visibility = LottieAnimationView.VISIBLE
        LoginManager.getInstance().logInWithReadPermissions(this,callbackManager,listOf("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object:FacebookCallback<LoginResult>{
            override fun onCancel() {
                binding.loadingAnimation.visibility = LottieAnimationView.GONE
                Toast.makeText(this@SignInActivity,"Login Cancelled!",Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException) {
                binding.loadingAnimation.visibility = LottieAnimationView.GONE
                Toast.makeText(this@SignInActivity,"Error: $error",Toast.LENGTH_LONG).show()
            }

            override fun onSuccess(result: LoginResult) {
                binding.loadingAnimation.visibility = LottieAnimationView.GONE
                handleFacebookAccessToken(result.accessToken)
            }

        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        binding.loadingAnimation.visibility = LottieAnimationView.VISIBLE
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    val email = user!!.email
                    val name = user.displayName
                    val uid = auth.uid
                    val databaseReference = database.getReference("users")
                    databaseReference.orderByChild("email").equalTo(email)
                        .addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(!snapshot.hasChildren()){
                                    databaseReference.child(uid!!).setValue(User(name,email,uid))
                                }
                                //go ahead
                                Toast.makeText(this@SignInActivity,"Signed In Successfully!",Toast.LENGTH_LONG).show()
                                val intent = Intent(this@SignInActivity,HomeActivity::class.java)
                                startActivity(intent)
                                binding.loadingAnimation.visibility = LottieAnimationView.GONE
                                finish()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication failed",Toast.LENGTH_LONG).show()
                }
            }
    }

    //launcher for google login
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {
        result ->
        run {
            if (result.resultCode == Activity.RESULT_OK) {
                binding.loadingAnimation.visibility = LottieAnimationView.VISIBLE
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    //getting account and credentials
                    val account: GoogleSignInAccount? = task.result
                    val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                    //sign in
                    auth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            //add data of user if it does not exist
                            val email = account!!.email
                            val name = account.displayName
                            val uid = auth.currentUser!!.uid
                            val databaseReference = database.getReference("users")
                            databaseReference.orderByChild("email").equalTo(email)
                                .addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(!snapshot.hasChildren()){
                                        databaseReference.child(uid).setValue(User(name,email,uid))
                                    }

                                    //go ahead
                                    Toast.makeText(this@SignInActivity,"Signed In Successfully!",Toast.LENGTH_LONG).show()
                                    val intent = Intent(this@SignInActivity,HomeActivity::class.java)
                                    binding.loadingAnimation.visibility = LottieAnimationView.GONE
                                    startActivity(intent)
                                    finish()
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                        } else {
                            Toast.makeText(this, "Try Again", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Login Cancelled", Toast.LENGTH_LONG).show()
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

    override fun onStart() {
        super.onStart()
        //checking if user is signed in (not null)
        if(auth.currentUser != null){
            Log.d("check","here")
            val intent = Intent(this@SignInActivity,HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
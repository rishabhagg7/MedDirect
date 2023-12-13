package com.example.meddirect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.meddirect.R
import com.example.meddirect.databinding.ActivityUserProfileBinding
import com.example.meddirect.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var uid: String
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        uid = auth.currentUser!!.uid

        //show details
        displayDetails()

        binding.updateButton.setOnClickListener {
            updateData()
        }

        binding.signOutButton.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut()
            val intent = Intent(this,SignInActivity::class.java)
            Toast.makeText(this,"Signed Out Successfully!",Toast.LENGTH_SHORT).show()
            //finish all activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        //Setting up drop down menu for blood group
        setUpBloodGroupMenu()
    }

    private fun setUpBloodGroupMenu() {
        val autoComplete: AutoCompleteTextView = binding.bloodGroupAutoComplete
        val adapter = ArrayAdapter(this, R.layout.item_drop_down, listOf("O-","O+","A-","A+","B-","B+","AB-","AB+"))
        autoComplete.setAdapter(adapter)
    }

    private fun updateData() {
        val userName = binding.nameEditText
        val userEmail = binding.emailEditText
        val userAge = binding.ageEditText
        val userHeight = binding.heightEditText
        val userWeight = binding.weightEditText
        val userBloodGroup = binding.bloodGroupAutoComplete
        val databaseReference = database.reference
        val uidReference = databaseReference.child("users").child(uid)
        uidReference.get().addOnCompleteListener{
            if(it.isSuccessful){
                val snapshot = it.result
                val id = snapshot.child("uid").getValue(String::class.java)
                val name = userName.text.toString()
                val email = userEmail.text.toString()
                val age = userAge.text.toString()
                val height = userHeight.text.toString()
                val weight = userWeight.text.toString()
                val bloodGroup = userBloodGroup.text.toString()
                val user = User(name,email,id,age,height,weight,bloodGroup)
                uidReference.setValue(user).addOnCompleteListener {
                    Toast.makeText(this,"Profile Updated Successfully!",Toast.LENGTH_SHORT).show()
                    displayDetails()
                }
            }
        }
    }

    private fun displayDetails() {
        val userName = binding.nameEditText
        val userEmail = binding.emailEditText
        val userAge = binding.ageEditText
        val userHeight = binding.heightEditText
        val userWeight = binding.weightEditText
        val userBloodGroup = binding.bloodGroupAutoComplete
        val databaseReference = database.reference
        val uidReference = databaseReference.child("users").child(uid)
        uidReference.get().addOnCompleteListener{
            if(it.isSuccessful){
                val snapshot = it.result
                val name = snapshot.child("name").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                val age = snapshot.child("age").getValue(String::class.java)
                val height = snapshot.child("height").getValue(String::class.java)
                val weight = snapshot.child("weight").getValue(String::class.java)
                val bloodGroup = snapshot.child("bloodGroup").getValue(String::class.java)
                userName.setText(name)
                userEmail.setText(email)
                userAge.setText(age)
                userHeight.setText(height)
                userWeight.setText(weight)
                userBloodGroup.setText(bloodGroup)
            }
        }
    }
}
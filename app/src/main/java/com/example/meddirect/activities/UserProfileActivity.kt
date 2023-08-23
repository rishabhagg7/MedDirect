package com.example.meddirect.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meddirect.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        uid = auth.currentUser!!.uid

        displayNameAndEmail()
    }

    private fun displayNameAndEmail() {
        val userName = binding.nameEditText
        val databaseReference = database.reference
        val uidReference = databaseReference.child("users").child(uid)
        uidReference.get().addOnCompleteListener{
            if(it.isSuccessful){
                val snapshot = it.result
                val name = snapshot.child("name").getValue(String::class.java)
                userName.setText(name)
            }
        }
    }
}
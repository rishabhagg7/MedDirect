package com.example.meddirect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.meddirect.R
import com.example.meddirect.databinding.ActivityDoctorProfileBinding
import com.google.firebase.database.FirebaseDatabase

class DoctorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorProfileBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var bundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        bundle = intent.extras!!
        val doctorId = bundle.getString("dId")
        val makeAppointmentButton: androidx.appcompat.widget.AppCompatButton = binding.buttonMakeAppointment

        //Display Data of doctor
        displayData()

        makeAppointmentButton.setOnClickListener {
            val intent = Intent(this,MakeAppointmentActivity::class.java)
            val sendBundle = Bundle()
            sendBundle.putString("dId",doctorId)
            intent.putExtras(sendBundle)
            startActivity(intent)
        }
    }

    private fun displayData() {
        val doctorId = bundle.getString("dId")
        val doctorPhoto: ImageView = binding.doctorPhoto
        val doctorName: TextView = binding.doctorName
        val doctorCategory: TextView = binding.doctorCategory
        val doctorRating: TextView = binding.doctorRating
        val doctorAbout: TextView = binding.doctorAbout
        val doctorPatients: TextView = binding.doctorPatients
        val doctorExperience: TextView = binding.doctorExperience
        val doctorFees: TextView = binding.doctorFees

        val databaseReference = database.getReference("doctor").child(doctorId!!)
        databaseReference.get().addOnCompleteListener {
            if(it.isSuccessful){
                val snapshot = it.result
                val url = snapshot.child("dPhoto").getValue(String::class.java)
                Glide.with(this).load(url).into(doctorPhoto)
                doctorName.text = snapshot.child("dName").getValue(String::class.java)
                doctorCategory.text = snapshot.child("dCategory").getValue(String::class.java)
                doctorRating.text = String.format(resources.getString(R.string.rating_s_5),snapshot.child("dRating").getValue(String::class.java))
                doctorAbout.text = snapshot.child("dAbout").getValue(String::class.java)
                doctorPatients.text = String.format(resources.getString(R.string.patient_count),snapshot.child("dPatients").getValue(String::class.java))
                doctorExperience.text = String.format(resources.getString(R.string.s_years),snapshot.child("dExperience").getValue(String::class.java))
                doctorFees.text = String.format(resources.getString(R.string.s_hour),snapshot.child("dFees").getValue(String::class.java))
            }
        }
    }
}
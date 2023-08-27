package com.example.meddirect.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.meddirect.R
import com.example.meddirect.databinding.ActivityShowAppointmentDetailsBinding
import com.example.meddirect.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToInt

class ShowAppointmentDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowAppointmentDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var bundle: Bundle
    private lateinit var uIdReference: DatabaseReference
    private lateinit var dIdReference: DatabaseReference
    private lateinit var aIdReference: DatabaseReference
    private lateinit var userId: String
    private lateinit var doctorId: String
    private lateinit var dateAppointment: String
    private lateinit var timeAppointment: String
    private lateinit var description: String
    private lateinit var totalPayment: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowAppointmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        bundle = intent.extras!!

        userId = auth.uid!!
        doctorId = bundle.getString("dId")!!

        uIdReference = database.reference.child("users").child(userId)
        dIdReference = database.reference.child("doctor").child(doctorId)
        aIdReference = database.reference.child("appointments")

        displayData()

        binding.makePaymentButton.setOnClickListener{
            if(!binding.confirmCheckBox.isChecked){
                Toast.makeText(this,"Check the box to continue!",Toast.LENGTH_SHORT).show()
            }else{
                createAppointment()
            }
        }
    }

    private fun createAppointment() {
        //user may have updated description
        updateDescription()
        val aId = aIdReference.push().key!!
        val appointment = Appointment(aId,userId,doctorId,dateAppointment,timeAppointment,description,totalPayment)
        aIdReference.child(aId).setValue(appointment).addOnCompleteListener {
            Toast.makeText(this,"Appointment Scheduled Successfully!",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDescription() {
        description = binding.problemDescriptionEditText.text.toString()
    }

    private fun displayData() {
        val patientName = binding.patientNameEditText
        val patientEmail = binding.patientEmailEditText
        val patientAge = binding.patientAgeEditText
        val patientWeight = binding.patientWeightEditText
        val patientHeight = binding.patientHeightEditText
        val patientBloodGroup = binding.patientBloodGroupEditText
        val doctorName = binding.doctorNameEditText
        val doctorCategory = binding.doctorCategoryEditText
        val doctorFees = binding.tvDoctorFees
        val appointmentDate = binding.appointmentDateEditText
        val appointmentTime = binding.appointmentTimeEditText
        val problemDescription = binding.problemDescriptionEditText
        val appCharges = binding.tvCompanyTax
        val totalCharges = binding.tvTotalPay

        val currency: Currency = Currency.getInstance(Locale("en","IN"))
        val symbol: String = currency.symbol

        uIdReference.get().addOnCompleteListener {
            if(it.isSuccessful){
                val snapshot = it.result
                patientName.setText(snapshot.child("name").getValue(String::class.java))
                patientEmail.setText(snapshot.child("email").getValue(String::class.java))
                patientAge.setText(snapshot.child("age").getValue(String::class.java))
                patientWeight.setText(snapshot.child("weight").getValue(String::class.java))
                patientHeight.setText(snapshot.child("height").getValue(String::class.java))
                patientBloodGroup.setText(snapshot.child("bloodGroup").getValue(String::class.java))
            }
        }
        dIdReference.get().addOnCompleteListener {
            if(it.isSuccessful){
                val snapshot = it.result
                doctorName.setText(snapshot.child("dName").getValue(String::class.java))
                doctorCategory.setText(snapshot.child("dCategory").getValue(String::class.java))
                doctorFees.text = String.format(resources.getString(R.string.fees_doctor),symbol,snapshot.child("dFees").getValue(String::class.java))
                val fees = doctorFees.text.toString().substring(1).toInt()
                val charges = (fees*0.05).roundToInt()
                val totalPay = fees + charges
                appCharges.text = String.format(resources.getString(R.string.charges_app),symbol,charges.toString())
                totalCharges.text = String.format(resources.getString(R.string.pay_total),symbol,totalPay.toString())
                totalPayment = String.format(resources.getString(R.string.pay_total),symbol,totalPay.toString())
            }
        }

        dateAppointment = bundle.getString("aDate")!!
        timeAppointment = bundle.getString("aTime")!!
        description = bundle.getString("problemDescription")!!

        appointmentDate.setText(dateAppointment)
        appointmentTime.setText(timeAppointment)
        problemDescription.setText(description)

    }
}
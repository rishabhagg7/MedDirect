package com.example.meddirect.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meddirect.R
import com.example.meddirect.databinding.ActivityViewAppointmentDetailsBinding
import com.example.meddirect.model.Appointment
import com.example.meddirect.model.Doctor
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToInt

class ViewAppointmentDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAppointmentDetailsBinding
    private lateinit var bundle: Bundle
    private lateinit var firestoreDatabase: FirebaseFirestore
    private lateinit var database: FirebaseDatabase
    private lateinit var appointmentReference: DocumentReference
    private lateinit var appointment: Appointment
    private lateinit var doctorId: String
    private lateinit var doctorReference: DatabaseReference
    private lateinit var doctor: Doctor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewAppointmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        firestoreDatabase = FirebaseFirestore.getInstance()
        bundle = intent.extras!!

        val appointmentId = bundle.getString("appointmentId")
        appointmentReference = firestoreDatabase.collection("appointments").document(appointmentId!!)
        appointmentReference.get().addOnCompleteListener { documentSnapshotTask ->
            if(documentSnapshotTask.isSuccessful){
                appointment = documentSnapshotTask.result.toObject(Appointment::class.java)!!
                doctorId = appointment.doctorId.toString()
                doctorReference = database.reference.child("doctor").child(doctorId)
                doctorReference.get().addOnCompleteListener {
                    if(it.isSuccessful){
                        doctor = it.result.getValue(Doctor::class.java)!!
                        showDetails()
                    }
                }
            }
        }
    }

    private fun showDetails() {
        val currency: Currency = Currency.getInstance(Locale("en","IN"))
        val symbol: String = currency.symbol
        val fees = doctor.dFees!!.toInt()
        val charges = (fees*0.05).roundToInt()
        val totalPay = appointment.totalPay!!.substring(1)

        binding.tvDoctorName.text = doctor.dName
        binding.tvDoctorCategory.text = doctor.dCategory
        binding.tvAppointmentDate.text = String.format(resources.getString(R.string.date_s),appointment.date)
        binding.tvAppointmentTime.text = String.format(resources.getString(R.string.time_s),appointment.time)
        if(appointment.description!!.isEmpty()){
            binding.tvAppointmentDetails.text = String.format(resources.getString(R.string.problem_description_s),"No description provided")
        }else{
            binding.tvAppointmentDetails.text = String.format(resources.getString(R.string.problem_description_s),appointment.description)
        }
        binding.tvDoctorFees.text = String.format(resources.getString(R.string.fees_doctor),symbol,fees.toString())
        binding.tvCompanyTax.text = String.format(resources.getString(R.string.charges_app), symbol,charges.toString())
        binding.tvTotalPay.text = String.format(resources.getString(R.string.pay_total),symbol,totalPay)
        binding.tvAppointmentId.text = String.format(resources.getString(R.string.appointment_id_s),appointment.appointmentId)
        binding.tvPaymentId.text = String.format(resources.getString(R.string.payment_id_s),appointment.paymentId)
    }
}
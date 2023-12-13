package com.example.meddirect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.meddirect.R
import com.example.meddirect.databinding.ActivityShowAppointmentDetailsBinding
import com.example.meddirect.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToInt

class ShowAppointmentDetailsActivity : AppCompatActivity(), PaymentResultListener {
    private lateinit var binding: ActivityShowAppointmentDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestoreDatabase: FirebaseFirestore
    private lateinit var bundle: Bundle
    private lateinit var uIdReference: DatabaseReference
    private lateinit var dIdReference: DatabaseReference
    private lateinit var aIdReference: DatabaseReference
    private lateinit var appointmentCollectionReference: CollectionReference
    private lateinit var userId: String
    private lateinit var doctorId: String
    private lateinit var dateAppointment: String
    private lateinit var timeAppointment: String
    private lateinit var description: String
    private lateinit var totalPayment: String
    private lateinit var paymentId: String
    private var appointmentId: String? = null
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityShowAppointmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firestoreDatabase = FirebaseFirestore.getInstance()
        bundle = intent.extras!!

        userId = auth.uid!!
        doctorId = bundle.getString("dId")!!

        uIdReference = database.reference.child("users").child(userId)
        dIdReference = database.reference.child("doctor").child(doctorId)
        aIdReference = database.reference.child("appointments")
        appointmentCollectionReference = firestoreDatabase.collection("appointments")

        displayData()

        //RazorPay check out
        Checkout.preload(this@ShowAppointmentDetailsActivity)

        binding.makePaymentButton.setOnClickListener{
            if(!binding.confirmCheckBox.isChecked){
                Toast.makeText(this,"Check the box to continue!",Toast.LENGTH_SHORT).show()
            }else{
                makePayment()
            }
        }
    }

    private fun makePayment(){
        val amount = totalPayment.substring(1).toInt()
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_lBA5Sw7LRxriNH")
        try{
            val options = JSONObject()
            options.apply {
                put("name","Medical Direct India Limited")
                put("description","Payment process for Appointment ID: $appointmentId")
                put("theme.color", ContextCompat.getColor(this@ShowAppointmentDetailsActivity,R.color.light_blue_700))
                put("currency","INR")
                put("amount",amount*100)
            }

            val retryObj = JSONObject()
            retryObj.apply {
                put("enabled",true)
                put("max_count",4)
            }
            options.put("retry",retryObj)

            checkout.open(this,options)

        }catch (e:Exception){
            Toast.makeText(this@ShowAppointmentDetailsActivity,
                "Payment Failed: Message- ${e.message}",
                Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun createAppointment() {
        //user may have updated description
        updateDescription()
        appointmentId = appointmentCollectionReference.document().id
        val appointment = Appointment(appointmentId,userId,doctorId,dateAppointment,timeAppointment,description,totalPayment,paymentId)
        appointmentCollectionReference.document(appointmentId!!).set(appointment)
            .addOnCompleteListener {
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

    override fun onPaymentSuccess(p0: String?) {
        val dateKey = bundle.getString("dateKey")
        val timeKey = timeAppointment
        database.reference.child("dateTime")
            .child(doctorId)
            .child(dateKey!!)
            .child("timeSlots")
            .child(timeKey)
            .child("booked")
            .setValue(true)
            .addOnCompleteListener {
                paymentId = p0!!
                Toast.makeText(this,"Payment Successful",Toast.LENGTH_SHORT).show()
                createAppointment()
                val intent = Intent(this,HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Toast.makeText(this,"Payment Error: $p1",Toast.LENGTH_SHORT).show()
    }
}
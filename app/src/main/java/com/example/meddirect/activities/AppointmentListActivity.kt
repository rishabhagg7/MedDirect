package com.example.meddirect.activities

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meddirect.adapters.AppointmentAdapter
import com.example.meddirect.databinding.ActivityAppointmentListBinding
import com.example.meddirect.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppointmentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentListBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreDatabase: FirebaseFirestore
    private val appointmentList =  ArrayList<Appointment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestoreDatabase = FirebaseFirestore.getInstance()

        val rvAppointment:RecyclerView = binding.rvAppointments
        val appointmentAdapter = AppointmentAdapter()
        rvAppointment.layoutManager = LinearLayoutManager(this@AppointmentListActivity)
        rvAppointment.adapter = appointmentAdapter
        binding.layoutNoAppointment.visibility = LinearLayout.GONE
        binding.rvAppointments.visibility = RecyclerView.GONE
        binding.shimmerAppointmentList.visibility = View.VISIBLE
        binding.shimmerAppointmentList.startShimmer()

        val appointmentCollectionReference = firestoreDatabase.collection("appointments")
        appointmentCollectionReference
            .whereEqualTo("userId",auth.uid!!)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    appointmentList.clear()
                    for(document in it.result){
                        val appointment = document.toObject(Appointment::class.java)
                        appointmentList.add(appointment)
                    }
                    if(appointmentList.size == 0){
                        binding.layoutNoAppointment.visibility = LinearLayout.VISIBLE
                        binding.rvAppointments.visibility = RecyclerView.GONE
                    }else{
                        binding.layoutNoAppointment.visibility = LinearLayout.GONE
                        binding.rvAppointments.visibility = RecyclerView.VISIBLE
                    }
                    binding.shimmerAppointmentList.stopShimmer()
                    binding.shimmerAppointmentList.visibility = View.GONE
                    appointmentAdapter.submitList(appointmentList)
                }
            }
    }
}
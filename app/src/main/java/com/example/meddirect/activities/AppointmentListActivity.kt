package com.example.meddirect.activities

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meddirect.adapters.AppointmentAdapter
import com.example.meddirect.databinding.ActivityAppointmentListBinding
import com.example.meddirect.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AppointmentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentListBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val appointmentList =  ArrayList<Appointment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val rvAppointment:RecyclerView = binding.rvAppointments
        val appointmentAdapter = AppointmentAdapter()
        rvAppointment.layoutManager = LinearLayoutManager(this@AppointmentListActivity)
        rvAppointment.adapter = appointmentAdapter
        binding.layoutNoAppointment.visibility = LinearLayout.GONE

        val databaseReference = database.getReference("appointments")
        databaseReference.orderByChild("userId").equalTo(auth.uid!!).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                appointmentList.clear()
                for(dataSnap in snapshot.children){
                    val data = dataSnap.getValue(Appointment::class.java)
                    appointmentList.add(data!!)
                }
                if(appointmentList.size == 0){
                    binding.layoutNoAppointment.visibility = LinearLayout.VISIBLE
                    binding.rvAppointments.visibility = RecyclerView.GONE
                }else{
                    binding.layoutNoAppointment.visibility = LinearLayout.GONE
                    binding.rvAppointments.visibility = RecyclerView.VISIBLE
                }
                appointmentAdapter.submitList(appointmentList)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}
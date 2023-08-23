package com.example.meddirect.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meddirect.adapters.DoctorAdapter
import com.example.meddirect.databinding.ActivityCategoryDoctorListBinding
import com.example.meddirect.model.Doctor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoryDoctorListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryDoctorListBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var doctorList: ArrayList<Doctor>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDoctorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        doctorList = arrayListOf()

        //getting category from bundle
        val bundle = intent.extras
        val category = bundle!!.getString("categoryName","")

        //setting up adapter and recycler view
        val rvDoctor: RecyclerView = binding.rvDoctor
        val doctorAdapter = DoctorAdapter()
        rvDoctor.layoutManager = LinearLayoutManager(this@CategoryDoctorListActivity)
        rvDoctor.adapter = doctorAdapter

        // fetching data
        val databaseReference = database.getReference("doctor")
        databaseReference.orderByChild("dCategory").equalTo(category).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                doctorList.clear()
                for(dataSnap in snapshot.children){
                    val data = dataSnap.getValue(Doctor::class.java)
                    doctorList.add(data!!)
                }
                doctorAdapter.submitList(doctorList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}

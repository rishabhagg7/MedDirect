package com.example.meddirect.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meddirect.adapters.CategoryAdapter
import com.example.meddirect.databinding.ActivityCategoryListBinding
import com.example.meddirect.model.Category
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryListBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var categoryList: ArrayList<Category>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialise data
        database = FirebaseDatabase.getInstance()
        categoryList = arrayListOf()

        //set up adapter and recycler view
        val rvCategory: RecyclerView = binding.rvCategory
        val categoryAdapter = CategoryAdapter()
        rvCategory.layoutManager = LinearLayoutManager(this@CategoryListActivity)
        rvCategory.adapter = categoryAdapter

        //getting data
        val databaseReference = database.getReference("category")
        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()
                //adding the data in list
                for(dataSnap in snapshot.children){
                    val data = dataSnap.getValue(Category::class.java)
                    categoryList.add(data!!)
                }
                //sends the data to the recycler view
                categoryAdapter.submitList(categoryList)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}
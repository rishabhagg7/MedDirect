package com.example.meddirect.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meddirect.R
import com.example.meddirect.adapters.HomeCategoryAdapter
import com.example.meddirect.adapters.HomeDoctorAdapter
import com.example.meddirect.databinding.ActivityHomeBinding
import com.example.meddirect.model.Category
import com.example.meddirect.model.Doctor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityHomeBinding
    private lateinit var homeDoctorList: MutableList<Doctor>
    private lateinit var homeCategoryList: ArrayList<Category>
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        homeDoctorList = mutableListOf()
        homeCategoryList = arrayListOf()
        toolbar = binding.homeToolBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //setting up drawer layout and toggle button
        val drawerLayout = binding.drawerLayout
        val navView = binding.navView

        toggle = ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_nav,R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //when navigation drawer item is clicked
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> {

                }
                R.id.nav_profile -> {
                    val intent = Intent(this@HomeActivity,UserProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_appointment -> {
                    val intent = Intent(this@HomeActivity,AppointmentListActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_share -> {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT,resources.getString(R.string.share_app))
                    startActivity(intent)
                }
                R.id.nav_feedback -> {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SENDTO
                    intent.data = Uri.parse("mailto:medicaldirectindia@gmail.com")
                    intent.putExtra(Intent.EXTRA_SUBJECT,"Feedback for MedDirect App")
                    if(intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    }
                }
                R.id.nav_support -> {
                    Toast.makeText(this,"Coming Soon",Toast.LENGTH_SHORT).show()
                }
                R.id.nav_about_us -> {
                    Toast.makeText(this,"Coming Soon",Toast.LENGTH_SHORT).show()
                }
                R.id.nav_log_out -> {
                    val intent = Intent(this,SignInActivity::class.java)
                    Toast.makeText(this,"Logged Out Successfully!", Toast.LENGTH_SHORT).show()
                    //finish all activities
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        val categoryViewMore = binding.viewMoreTextview
        val doctorViewMore = binding.moreTextview
        val profileIcon = binding.profileIcon

        //display name of the user
        displayName()
        displayCategory()
        displayTopDoctor()

        //when user click on view more of category
        categoryViewMore.setOnClickListener {
            val intent = Intent(this,CategoryListActivity::class.java)
            startActivity(intent)
        }

        //when user click on view more of doctor
        doctorViewMore.setOnClickListener {
            val intent = Intent(this,DoctorListActivity::class.java)
            startActivity(intent)
        }

        //when user click on profile icon
        profileIcon.setOnClickListener {
            val intent = Intent(this,UserProfileActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayTopDoctor(){
        val rvDoctor: RecyclerView = binding.rvHomeDoctor
        val homeDoctorAdapter = HomeDoctorAdapter()
        rvDoctor.layoutManager = LinearLayoutManager(this@HomeActivity)
        rvDoctor.adapter = homeDoctorAdapter
        val databaseReference = database.getReference("doctor")
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                homeDoctorList.clear()
                for(dataSnap in snapshot.children){
                    val data = dataSnap.getValue(Doctor::class.java)
                    homeDoctorList.add(data!!)
                }
                homeDoctorList.sortByDescending {
                    it.dRating
                }
                val totalDoctorToShow = minOf(4,homeDoctorList.size)
                homeDoctorList = homeDoctorList.take(totalDoctorToShow).toMutableList()
                homeDoctorAdapter.submitList(homeDoctorList)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun displayCategory(){
        val rvCategory: RecyclerView = binding.rvHomeCategory
        val homeCategoryAdapter = HomeCategoryAdapter()
        rvCategory.layoutManager = LinearLayoutManager(this@HomeActivity,LinearLayoutManager.HORIZONTAL,false)
        rvCategory.adapter = homeCategoryAdapter
        val databaseReference = database.getReference("category")
        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                homeCategoryList.clear()
                //adding the data in list
                var count = 0
                for(dataSnap in snapshot.children){
                    val data = dataSnap.getValue(Category::class.java)
                    homeCategoryList.add(data!!)
                    count += 1
                    if(count == 5){
                        break
                    }
                }
                //sends the data to the recycler view
                homeCategoryAdapter.submitList(homeCategoryList)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun displayName() {
        //get uid of current user
        val uid = auth.currentUser!!.uid
        //getting textView id
        val userName: TextView = binding.userFirstName
        //fetching data from uid
        val databaseReference = database.reference
        val uidReference = databaseReference.child("users").child(uid)
        uidReference.get().addOnCompleteListener {
            if(it.isSuccessful){
                //taking snapshot of the database
                val snapshot = it.result
                //fetching column data
                val name = snapshot.child("name").getValue(String::class.java)
                //showing the text in textView
                userName.text = name
            }else{
                //don't show name
                userName.visibility = TextView.GONE
            }
        }
    }
}
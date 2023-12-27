package com.example.meddirect.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.meddirect.R
import com.example.meddirect.databinding.ActivityUserProfileBinding
import com.example.meddirect.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var uid: String
    private lateinit var storage: FirebaseStorage
    private lateinit var photoReference: StorageReference
    private lateinit var userReference: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var photoLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        uid = auth.currentUser!!.uid
        photoReference = storage.reference.child("uImage").child(uid)
        userReference = database.reference.child("users").child(uid)

        //show details
        displayDetails()

        //when user click on update button
        binding.updateButton.setOnClickListener {
            updateData()
        }

        //when user click on sign out button
        binding.signOutButton.setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut()
            val intent = Intent(this,SignInActivity::class.java)
            Toast.makeText(this,"Signed Out Successfully!",Toast.LENGTH_SHORT).show()
            //finish all activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        //Setting up drop down menu for blood group
        setUpBloodGroupMenu()

        //defining launcher for permission
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
            if(isGranted){
                openPhotoPicker()
            }else{
                showPermissionMessage()
            }
        }

        //defining launcher for picking photo
        photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode == RESULT_OK){
                uploadPhotoToFirebaseStorage(result.data?.data!!)
            }
        }

        //when user click on profile picture
        binding.profilePic.setOnClickListener {
            checkPermissionAndPickPhoto()
        }
    }

    //upload the photo to cloud storage and saving link in database
    private fun uploadPhotoToFirebaseStorage(photoUri: Uri) {
        binding.progressBar.visibility = View.VISIBLE
        photoReference.putFile(photoUri).addOnCompleteListener {
            photoReference.downloadUrl.addOnCompleteListener {uri->
                userReference.child("photoUrl").setValue(uri.result.toString()).addOnCompleteListener {
                    displayDetails()
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }

    //manage media permission and open media to pick photo
    private fun checkPermissionAndPickPhoto() {
        val readExternalPhoto = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            android.Manifest.permission.READ_MEDIA_IMAGES
        }else{
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if(ContextCompat.checkSelfPermission(this,readExternalPhoto) == PackageManager.PERMISSION_GRANTED){
            //we have permission
            openPhotoPicker()
        }
        else{
            permissionLauncher.launch(readExternalPhoto)
        }
    }

    //show dialog box when permission is not granted
    private fun showPermissionMessage(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setMessage(resources.getString(R.string.media_permission_required_message))
            .setTitle(resources.getString(R.string.permission_required))
            .setPositiveButton(resources.getString(R.string.grant_permission)) { _, _ ->
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
            }
            .setNegativeButton(resources.getString(R.string.no_thanks)){_,_->

            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //to launch media to pick photos
    private fun openPhotoPicker(){
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.type = "image/*"
        photoLauncher.launch(intent)
    }

    //drop down menu for blood groups
    private fun setUpBloodGroupMenu() {
        val autoComplete: AutoCompleteTextView = binding.bloodGroupAutoComplete
        val adapter = ArrayAdapter(this, R.layout.item_drop_down, listOf("O-","O+","A-","A+","B-","B+","AB-","AB+"))
        autoComplete.setAdapter(adapter)
    }

    //update details in database
    private fun updateData() {
        val userName = binding.nameEditText
        val userEmail = binding.emailEditText
        val userAge = binding.ageEditText
        val userHeight = binding.heightEditText
        val userWeight = binding.weightEditText
        val userBloodGroup = binding.bloodGroupAutoComplete
        val databaseReference = database.reference
        val uidReference = databaseReference.child("users").child(uid)
        uidReference.get().addOnCompleteListener{
            if(it.isSuccessful){
                val snapshot = it.result
                val id = snapshot.child("uid").getValue(String::class.java)
                val name = userName.text.toString()
                val email = userEmail.text.toString()
                val age = userAge.text.toString()
                val height = userHeight.text.toString()
                val weight = userWeight.text.toString()
                val bloodGroup = userBloodGroup.text.toString()
                val photoUrl = snapshot.child("photoUrl").getValue(String::class.java)
                val user = User(name = name,email = email,uId = id,age = age,height = height, weight = weight,bloodGroup = bloodGroup, photoUrl = photoUrl)
                uidReference.setValue(user).addOnCompleteListener {
                    Toast.makeText(this,"Profile Updated Successfully!",Toast.LENGTH_SHORT).show()
                    displayDetails()
                }
            }
        }
    }

    //show details on ui
    private fun displayDetails() {
        val userName = binding.nameEditText
        val userEmail = binding.emailEditText
        val userAge = binding.ageEditText
        val userHeight = binding.heightEditText
        val userWeight = binding.weightEditText
        val userBloodGroup = binding.bloodGroupAutoComplete
        userReference.get().addOnCompleteListener{
            if(it.isSuccessful){
                val snapshot = it.result
                val name = snapshot.child("name").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                val age = snapshot.child("age").getValue(String::class.java)
                val height = snapshot.child("height").getValue(String::class.java)
                val weight = snapshot.child("weight").getValue(String::class.java)
                val bloodGroup = snapshot.child("bloodGroup").getValue(String::class.java)
                val photoUrl = snapshot.child("photoUrl").getValue(String::class.java)
                Glide.with(this).load(photoUrl)
                    .apply(RequestOptions().placeholder(R.drawable.baseline_account_circle_24))
                    .circleCrop()
                    .into(binding.profilePic)
                userName.setText(name)
                userEmail.setText(email)
                userAge.setText(age)
                userHeight.setText(height)
                userWeight.setText(weight)
                userBloodGroup.setText(bloodGroup)
            }
        }
    }
}
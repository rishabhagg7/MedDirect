package com.example.meddirect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.meddirect.R
import com.example.meddirect.model.Appointment
import com.google.firebase.database.FirebaseDatabase

class AppointmentAdapter:ListAdapter<Appointment,AppointmentAdapter.AppointmentViewHolder>(DiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        return AppointmentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_appointment_list,parent,false)
        )
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AppointmentViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(appointmentData: Appointment) {
            val appointmentNumber: TextView = itemView.findViewById(R.id.appointment_no)
            val doctorName: TextView = itemView.findViewById(R.id.appointment_doctor_name)
            val doctorCategory: TextView = itemView.findViewById(R.id.appointment_doctor_category)
            val payment: TextView = itemView.findViewById(R.id.appointment_pay)
            val date: TextView = itemView.findViewById(R.id.appointment_date)
            val doctorImage : ImageView = itemView.findViewById(R.id.appointment_doctor_image)

            //Getting doctor details from doctor table using doctor id from firebase
            val doctorReference = FirebaseDatabase.getInstance()
                .reference
                .child("doctor")
                .child(appointmentData.doctorId!!)

            doctorReference.get().addOnCompleteListener {
                if(it.isSuccessful){
                    val snapshot = it.result
                    doctorName.text = snapshot.child("dName").getValue(String::class.java)
                    doctorCategory.text = snapshot.child("dCategory").getValue(String::class.java)
                    val url = snapshot.child("dPhoto").getValue(String::class.java)
                    Glide.with(itemView.context).load(url).into(doctorImage)
                    appointmentNumber.text = String.format(itemView.resources.getString(R.string.appointment_no_s),appointmentData.appointmentId)
                    date.text = String.format(itemView.resources.getString(R.string.date_s),appointmentData.date)
                    payment.text = String.format(itemView.resources.getString(R.string.payment_s),appointmentData.totalPay)
                }
            }
        }

    }
    companion object{
        private val DiffCallBack = object : DiffUtil.ItemCallback<Appointment>(){
            override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
                return oldItem.appointmentId == newItem.appointmentId
            }

            override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
                return areItemsTheSame(oldItem,newItem)
            }

        }
    }
}
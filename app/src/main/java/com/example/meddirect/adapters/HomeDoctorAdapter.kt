package com.example.meddirect.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.meddirect.R
import com.example.meddirect.activities.DoctorProfileActivity
import com.example.meddirect.model.Doctor

class HomeDoctorAdapter : ListAdapter<Doctor, HomeDoctorAdapter.HomeDoctorViewHolder>(DiffCallBack){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeDoctorViewHolder {
        return HomeDoctorViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_home_doctor_list,parent,false)
        )
    }

    override fun onBindViewHolder(holder: HomeDoctorViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DoctorProfileActivity::class.java)
            val bundle = Bundle()
            bundle.putString("dId",getItem(position).dId)
            intent.putExtras(bundle)
            ContextCompat.startActivity(holder.itemView.context, intent, bundle)
        }
    }

    class HomeDoctorViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val doctorName: TextView = view.findViewById(R.id.doctor_name)
        private val doctorCategory: TextView = view.findViewById(R.id.doctor_category)
        private val doctorImage: ImageView = view.findViewById(R.id.doctor_image)
        private val doctorRating: TextView = view.findViewById(R.id.doctor_rating)

        fun bind(doctorData: Doctor){
            doctorName.text = doctorData.dName
            doctorCategory.text = doctorData.dCategory
            Glide.with(itemView.context).load(doctorData.dPhoto).into(doctorImage)
            doctorRating.text = String.format(itemView.resources.getString(R.string.rating_s_5),doctorData.dRating)
        }
    }

    companion object{
        private val DiffCallBack = object: DiffUtil.ItemCallback<Doctor>() {
            override fun areItemsTheSame(oldItem: Doctor, newItem: Doctor): Boolean {
                return oldItem.dId == newItem.dId
            }

            override fun areContentsTheSame(oldItem: Doctor, newItem: Doctor): Boolean {
                return areItemsTheSame(oldItem,newItem)
            }
        }
    }
}
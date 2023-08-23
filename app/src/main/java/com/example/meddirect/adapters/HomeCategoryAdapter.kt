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
import com.example.meddirect.activities.CategoryDoctorListActivity
import com.example.meddirect.model.Category


class HomeCategoryAdapter: ListAdapter<Category, HomeCategoryAdapter.HomeCategoryViewHolder>(DiffCallBack) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeCategoryViewHolder {
        return HomeCategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_home_category_list,parent,false )
        )
    }

    override fun onBindViewHolder(holder: HomeCategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, CategoryDoctorListActivity::class.java)
            val bundle = Bundle()
            bundle.putString("categoryName",getItem(position).cName)
            intent.putExtras(bundle)
            ContextCompat.startActivity(holder.itemView.context, intent, bundle)
        }
    }

    class HomeCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val categoryName: TextView = view.findViewById(R.id.category_name)
        private val categoryImage: ImageView = view.findViewById(R.id.category_image)

        fun bind(categoryData: Category){
            categoryName.text = categoryData.cName
            Glide.with(itemView.context).load(categoryData.cPhoto).into(categoryImage)
        }
    }

    companion object{
        private val DiffCallBack = object: DiffUtil.ItemCallback<Category>(){
            override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
                return oldItem.cId == newItem.cId
            }

            override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
                return areItemsTheSame(oldItem,newItem)
            }

        }
    }

}
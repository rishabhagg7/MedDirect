package com.example.meddirect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.meddirect.R
import com.example.meddirect.model.TimeSlot

class TimeAdapter(private val listener: (timeDataModel: TimeSlot, position:Int) -> Unit)
    : RecyclerView.Adapter<TimeAdapter.MyViewHolder>() {

    private val list = ArrayList<TimeSlot>()
    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(timeDataModel: TimeSlot) {
            val timeSlot = itemView.findViewById<TextView>(R.id.tv_time)
            val cardView = itemView.findViewById<CardView>(R.id.card_time)

            if(timeDataModel.isSelected){
                timeSlot.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.white
                    )
                )
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.red
                    )
                )
            }else{
                timeSlot.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.black
                    )
                )
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.white
                    )
                )
            }

            timeSlot.text = String.format(
                itemView.resources.getString(R.string.time_slot),
                timeDataModel.hour,
                timeDataModel.minutes)

            cardView.setOnClickListener {
                listener.invoke(timeDataModel,adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_list,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setTimeData(timeSlotList: ArrayList<TimeSlot>){
        list.clear()
        list.addAll(timeSlotList)
        notifyDataSetChanged()
    }
}
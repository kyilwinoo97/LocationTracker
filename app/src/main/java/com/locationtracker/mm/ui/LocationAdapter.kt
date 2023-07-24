package com.locationtracker.mm.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.locationtracker.mm.data.db.LocationEntity
import com.locationtracker.mm.databinding.RecyclerItemBinding
import java.text.DateFormat


class LocationAdapter(val item : List<LocationEntity>, val context: Context) :RecyclerView.Adapter<LocationAdapter.ViewHolder>() {


    private lateinit var binding: RecyclerItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater =LayoutInflater.from(parent.context)
        binding= RecyclerItemBinding.inflate(inflater,parent,false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(item[position])
    }
    class ViewHolder(var binding: RecyclerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item : LocationEntity){
            binding.apply {
                txtAddress.text=item.address
                txtLat.text = item.latitude.toString()
                txtLong.text = item.longitude.toString()
                txtDate.text = DateFormat.getDateTimeInstance().format(item.date)
                val appState = if (item.foreground) {
                    "In App"
                } else {
                    "In Background"
                }

                //only for debugging
                txtbackground.text = appState

            }
        }

    }

}
package com.example.weatherforecastapplication.fav.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherforecastapplication.R
import com.example.weatherforecastapplication.model.FavoriteCity


class ProductsDiffUtil : DiffUtil.ItemCallback<FavoriteCity>() {
    override fun areItemsTheSame(oldItem: FavoriteCity, newItem: FavoriteCity): Boolean {
        return oldItem.cityName == newItem.cityName
    }

    override fun areContentsTheSame(oldItem: FavoriteCity, newItem: FavoriteCity): Boolean {
        return oldItem == newItem
    }

}
class FavAdapter(private val listener: OnCityClickListener) :
    ListAdapter<FavoriteCity, FavAdapter.ViewHolder>(
        ProductsDiffUtil()
    ) {




    class ViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
        val cityName: TextView = item.findViewById(R.id.text_city_name)
        val imageView: ImageView = item.findViewById(R.id.icon_favorite)
        val layout: View = itemView.findViewById(R.id.favlayout) // Assuming favlayout is the ID


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.item_favourite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentCity = getItem(position)
        holder.cityName.text = currentCity.cityName
        Log.d("FavAdapter", "Binding city: ${currentCity.cityName}")

        holder.imageView.setOnClickListener {
            Log.d("FavAdapter", "City clicked: ${currentCity.cityName}")

            listener.onDeleteCityClick(currentCity)
        }
        holder.layout.setOnClickListener{
            listener.onCityClick(currentCity)
        }


    }

}
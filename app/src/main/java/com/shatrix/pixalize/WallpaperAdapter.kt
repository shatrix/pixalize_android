package com.shatrix.pixalize

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WallpaperAdapter(
    private var wallpapers: List<Wallpaper>,
    private val onItemClicked: (Wallpaper) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder>() {

    inner class WallpaperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image_view_thumbnail)
        private val nameView: TextView = itemView.findViewById(R.id.text_view_name)

        fun bind(wallpaper: Wallpaper) {
            nameView.text = wallpaper.name
            
            // Optimized Glide loading with placeholder and thumbnail
            Glide.with(itemView.context)
                .load(wallpaper.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .thumbnail(0.25f)  // Load 25% quality first for faster preview
                .centerCrop()
                .into(imageView)

            itemView.setOnClickListener {
                onItemClicked(wallpaper)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallpaper, parent, false)
        return WallpaperViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        holder.bind(wallpapers[position])
    }

    override fun getItemCount() = wallpapers.size

    // Method to update the wallpapers with DiffUtil
    fun updateWallpapers(newWallpapers: List<Wallpaper>) {
        val diffCallback = WallpaperDiffCallback(this.wallpapers, newWallpapers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.wallpapers = newWallpapers
        diffResult.dispatchUpdatesTo(this)
    }
}

class WallpaperDiffCallback(
    private val oldList: List<Wallpaper>,
    private val newList: List<Wallpaper>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].name == newList[newItemPosition].name
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

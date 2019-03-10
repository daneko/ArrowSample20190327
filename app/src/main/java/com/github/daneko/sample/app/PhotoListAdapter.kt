package com.github.daneko.sample.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.daneko.sample.app.databinding.CellBinding
import com.github.daneko.sample.domain.Photo

class PhotoListAdapter : ListAdapter<PhotoCell, PhotoCellViewHolder>(PhotoCellDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoCellViewHolder {
        return CellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .run(::PhotoCellViewHolder)
    }

    override fun onBindViewHolder(holder: PhotoCellViewHolder, position: Int) {
        getItem(position)?.run(holder::setup)
    }
}

object PhotoCellDiff : DiffUtil.ItemCallback<PhotoCell>() {
    override fun areItemsTheSame(oldItem: PhotoCell, newItem: PhotoCell): Boolean {
        return oldItem.data.id == newItem.data.id
    }

    override fun areContentsTheSame(oldItem: PhotoCell, newItem: PhotoCell): Boolean {
        return oldItem.data == newItem.data
    }
}

data class PhotoCell(
    val data: Photo
)

class PhotoCellViewHolder(private val binding: CellBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun setup(cell: PhotoCell) {
        binding.title.text = cell.data.title
        binding.url.text = cell.data.url.toASCIIString()
    }

}

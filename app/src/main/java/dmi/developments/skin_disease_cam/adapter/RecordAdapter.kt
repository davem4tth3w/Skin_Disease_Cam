package dmi.developments.skin_disease_cam.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dmi.developments.skin_disease_cam.R
import dmi.developments.skin_disease_cam.data.entity.Result
import dmi.developments.skin_disease_cam.utils.Converters

class RecordAdapter(
    private val onItemClick: (Result) -> Unit,
    private val onMenuClick: (View, Result) -> Unit,
    private val selectionChangeListener: () -> Unit
) : ListAdapter<Result, RecordAdapter.RecordViewHolder>(DiffCallback()) {

    private val selectedItems = mutableSetOf<Int>() // positions of selected items
    var multiSelect = false

    inner class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val skinDisease: TextView = itemView.findViewById(R.id.tvSkinDisease)
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val menuButton: ImageView = itemView.findViewById(R.id.menuButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = getItem(position)
        holder.skinDisease.text = record.skindisease ?: "Unknown"
        holder.date.text = Converters.formatTimestamp(record.timestamp)

        // Highlight if selected
        holder.itemView.setBackgroundColor(
            if (selectedItems.contains(position)) holder.itemView.context.getColor(R.color.light_blue)
            else holder.itemView.context.getColor(android.R.color.white)
        )

        // Click
        holder.itemView.setOnClickListener {
            if (multiSelect) {
                toggleSelection(position)
            } else {
                onItemClick(record)
            }
        }

        // Long press starts multi-select mode
        holder.itemView.setOnLongClickListener {
            if (!multiSelect) {
                multiSelect = true
                toggleSelection(position)
            }
            true
        }

        // Menu button
        holder.menuButton.setOnClickListener { view ->
            onMenuClick(view, record)
        }
    }

    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) selectedItems.remove(position)
        else selectedItems.add(position)

        selectionChangeListener()
        notifyItemChanged(position)
    }

    fun clearSelection() {
        val list = selectedItems.toList()
        selectedItems.clear()
        list.forEach { notifyItemChanged(it) }
        multiSelect = false
    }

    fun selectAll() {
        selectedItems.clear()
        for (i in 0 until itemCount) selectedItems.add(i)
        notifyDataSetChanged()
    }

    fun getSelectedResults(): List<Result> {
        return selectedItems.map { getItem(it) }
    }

    fun getSelectedCount(): Int = selectedItems.size

    class DiffCallback : DiffUtil.ItemCallback<Result>() {
        override fun areItemsTheSame(oldItem: Result, newItem: Result) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Result, newItem: Result) = oldItem == newItem
    }
}

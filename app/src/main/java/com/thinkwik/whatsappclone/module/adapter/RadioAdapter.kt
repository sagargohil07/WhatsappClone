package com.thinkwik.whatsappclone.module.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.module.model.RadioItem

class RadioAdapter(
    private val radioItems: List<RadioItem>,
    private var onItemSelect: (RadioItem, position: Int) -> Unit
) :
    RecyclerView.Adapter<RadioAdapter.RadioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_radio_selection, parent, false)
        return RadioViewHolder(view)
    }

    override fun onBindViewHolder(holder: RadioViewHolder, position: Int) {
        val radioItem = radioItems[position]
        holder.radioButton.text = radioItem.text
        holder.radioButton.isChecked = radioItem.isSelected

        holder.radioButton.setOnClickListener {
            // Update the selected item and notify the adapter to refresh the UI
            radioItems.forEach { it.isSelected = false }
            radioItem.isSelected = true
            onItemSelect(radioItem, position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = radioItems.size

    inner class RadioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)
    }
}

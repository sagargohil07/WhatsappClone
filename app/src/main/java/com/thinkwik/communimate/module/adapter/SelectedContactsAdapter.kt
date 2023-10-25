package com.thinkwik.communimate.module.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thinkwik.communimate.R
import com.thinkwik.communimate.databinding.ListItemSelectedContactsBinding
import com.thinkwik.communimate.module.model.CombinedContactsModel

class SelectedContactsAdapter(
    val context: Context,
    private val isFromCreateGroup: Boolean = false,
    private var contactsList: ArrayList<CombinedContactsModel>,
    private var onItemRemove: (CombinedContactsModel) -> Unit,
) : RecyclerView.Adapter<SelectedContactsAdapter.SelectedContactsViewHolder>() {

    inner class SelectedContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemSelectedContactsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedContactsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_selected_contacts, parent, false)
        return SelectedContactsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return contactsList.size
    }

    override fun onBindViewHolder(holder: SelectedContactsViewHolder, position: Int) {
        val model = contactsList[position]
        Log.d(
            "rv",
            "onBindViewHolder : SelectedContactsAdapter  $position | model : ${model.toString()}"
        )
        Glide.with(context).load(model.imageUrl).circleCrop().into(holder.binding.ivProfile)
        holder.binding.tvName.text = model.name
        holder.binding.ivSelected.isVisible = !isFromCreateGroup
        holder.itemView.setOnClickListener {
            /*if (model.isContactSelected){

            }*/
            onItemRemove(model)
        }
    }

    fun addContact(item: CombinedContactsModel) {
        contactsList.add(item)
        notifyItemInserted(contactsList.size - 1)
    }

    fun removeContact(position: Int) {
        contactsList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateList(contactsList: ArrayList<CombinedContactsModel>) {
        this.contactsList = contactsList
        notifyDataSetChanged()
    }
}
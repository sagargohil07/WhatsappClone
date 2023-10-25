package com.thinkwik.communimate.module.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thinkwik.communimate.R
import com.thinkwik.communimate.databinding.ListItemContactsBinding
import com.thinkwik.communimate.module.model.CombinedContactsModel

class ContactsAdapter(
    private val context: Context,
    private var contactsList: ArrayList<CombinedContactsModel>,
    private var onContactSelect: (CombinedContactsModel, position: Int) -> Unit,
    private var onInvite: (CombinedContactsModel) -> Unit,
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemContactsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_contacts, parent, false)
        return ContactViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val model = contactsList[position]
        Log.d("rv", "onBindViewHolder: $position | model : ${model.toString()}")
        if (model.isInDB) {
            holder.binding.tvInvite.isVisible = false
            holder.binding.tvAlphabet.isVisible = false
            holder.binding.tvName.text = model.name
            holder.binding.tvNumber.isVisible = !model.number.isNullOrBlank()
            holder.binding.tvNumber.text = model.number
            Glide.with(context).load(model.imageUrl).circleCrop().into(holder.binding.ivProfile)
            holder.binding.ivSelected.isVisible = model.isContactSelected
        } else {
            holder.binding.tvAlphabet.isVisible = true
            holder.binding.tvInvite.isVisible = true
            holder.binding.tvAlphabet.text =
                model.name!!.split(" ").filter { it.isNotBlank() }.take(2).map { it[0] }
                    .joinToString("").uppercase()
            holder.binding.tvName.text = model.name
            holder.binding.tvNumber.text = model.number
            Glide.with(context).load(model.imageUrl).circleCrop().placeholder(R.color.green_light)
                .into(holder.binding.ivProfile)
        }
        holder.binding.tvInvite.setOnClickListener {
            onInvite(model)
        }

        holder.itemView.setOnClickListener {
            if (model.isInDB)
                onContactSelect(model, position)
        }
    }

    override fun getItemCount(): Int {
        return contactsList.size
    }

    fun updateList(contactsList: ArrayList<CombinedContactsModel>) {
        this.contactsList = contactsList
        notifyDataSetChanged()
    }

    fun updateItem(model: CombinedContactsModel) {
        val pos = this.contactsList.indexOf(model)
        if (pos >= 0) {
            this.contactsList[pos].isContactSelected = false
            notifyItemChanged(pos)
        }
    }

}
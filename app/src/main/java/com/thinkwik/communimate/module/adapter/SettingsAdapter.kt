package com.thinkwik.communimate.module.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.thinkwik.communimate.R
import com.thinkwik.communimate.databinding.ListItemSettingsBinding
import com.thinkwik.communimate.module.model.SettingsModel

class SettingsAdapter(
    private val context: Context,
    private var settingsList: ArrayList<SettingsModel>,
    private val onItemClick: (model: SettingsModel) -> Unit,
) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    inner class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemSettingsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_settings, parent, false)
        return SettingsViewHolder(view)
    }


    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        val model = settingsList[position]
        holder.binding.tvTitle.text = model.title
        holder.binding.tvInfo.text = model.info
        holder.binding.tvInfo.isVisible = model.info.isNotEmpty()
        holder.binding.ivIcon.setImageDrawable(model.icon)

        holder.itemView.setOnClickListener {
            onItemClick(model)
        }
    }

    override fun getItemCount(): Int {
        return settingsList.size
    }

    fun updateList(settingsList: ArrayList<SettingsModel>) {
        this.settingsList = settingsList
        notifyDataSetChanged()
    }
}
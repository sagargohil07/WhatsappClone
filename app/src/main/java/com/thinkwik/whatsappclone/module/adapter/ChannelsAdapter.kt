package com.thinkwik.whatsappclone.module.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.databinding.ListItemChannelsBinding
import com.thinkwik.whatsappclone.module.model.ChannelsModel

class ChannelsAdapter(
    private val context: Context,
    private var channelList: ArrayList<ChannelsModel>,
    private val onItemClick: (model: ChannelsModel, isFollowButtonClicked: Boolean) -> Unit,
) : RecyclerView.Adapter<ChannelsAdapter.ChannelsViewHolder>() {

    inner class ChannelsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemChannelsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_channels, parent, false)
        return ChannelsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return channelList.size
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ChannelsViewHolder, position: Int) {
        val model = channelList[position]
        holder.binding.tvChannelName.text = model.channelName
        Glide.with(context)
            .load(model.channelImage)
            .placeholder(R.drawable.progress_animation)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .error(R.drawable.profile)
            .circleCrop()
            .into(holder.binding.ivChannel)

        holder.binding.btnFollow.setOnClickListener {
            onItemClick(model, true)
        }

        holder.itemView.setOnClickListener {
            onItemClick(model, false)
        }
    }

    fun updateList(filterList: ArrayList<ChannelsModel>) {
        channelList = filterList
        notifyDataSetChanged()
    }
}
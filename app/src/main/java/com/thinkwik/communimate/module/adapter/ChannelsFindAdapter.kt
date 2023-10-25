package com.thinkwik.communimate.module.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.thinkwik.communimate.R
import com.thinkwik.communimate.databinding.ListItemFindChannelsBinding
import com.thinkwik.communimate.module.model.ChannelsModel

class ChannelsFindAdapter(
    private val context: Context,
    private var channelList: ArrayList<ChannelsModel>,
    private val onItemClick: (model: ChannelsModel, isFollowButtonClicked: Boolean/*, position: Int*/) -> Unit,
) : RecyclerView.Adapter<ChannelsFindAdapter.ChannelsFindViewHolder>() {
    inner class ChannelsFindViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemFindChannelsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelsFindViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_find_channels, parent, false)
        return ChannelsFindViewHolder(view)
    }

    override fun getItemCount(): Int {
        return channelList.size
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ChannelsFindViewHolder, position: Int) {
        val model = channelList[position]
        holder.binding.tvChannelName.text = model.channelName
        holder.binding.tvChannelInfo.text = model.channelInfo
        Glide.with(context)
            .load(model.channelImage)
            .placeholder(R.drawable.progress_animation)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .into(holder.binding.ivChannelImage)

        val icon = if (model.isFollowing == 0) {
            context.getDrawable(R.drawable.ic_add)
        } else {
            context.getDrawable(R.drawable.ic_done)
        }
        holder.binding.ivChannelFollow.setImageDrawable(icon)

        holder.binding.ivChannelFollow.setOnClickListener {
            channelList[position].isFollowing = if (model.isFollowing == 0) 1 else 0
            notifyItemChanged(position)
            onItemClick(model, true)
        }

        holder.itemView.setOnClickListener {
            onItemClick(model, false)
        }
    }


    fun updateList(channelList: ArrayList<ChannelsModel>) {
        this.channelList = channelList
        notifyDataSetChanged()
    }

}
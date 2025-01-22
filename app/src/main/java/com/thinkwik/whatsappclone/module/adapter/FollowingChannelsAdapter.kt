package com.thinkwik.whatsappclone.module.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.databinding.ListItemFollowingChannelsBinding
import com.thinkwik.whatsappclone.module.model.ChannelsModel

class FollowingChannelsAdapter(
    private val context: Context,
    private var channelList: ArrayList<ChannelsModel>,
    private val onItemClick: (model: ChannelsModel) -> Unit,
) : RecyclerView.Adapter<FollowingChannelsAdapter.FollowingChannelsViewHolder>() {
    inner class FollowingChannelsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemFollowingChannelsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingChannelsViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_following_channels, parent, false)
        return FollowingChannelsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return channelList.size
    }

    override fun onBindViewHolder(holder: FollowingChannelsViewHolder, position: Int) {
        val model = channelList[position]
        holder.binding.tvChannelName.text = model.channelName
        Glide.with(context)
            .load(model.channelImage)
            .placeholder(R.drawable.profile)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .error(R.drawable.profile)
            .into(holder.binding.ivChannelImage)

        holder.itemView.setOnClickListener {
            onItemClick(model)
        }
    }

    fun updateList(filterList: ArrayList<ChannelsModel>) {
        channelList = filterList
        notifyDataSetChanged()
    }
}
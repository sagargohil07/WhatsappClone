package com.thinkwik.communimate.module.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.thinkwik.communimate.R
import com.thinkwik.communimate.databinding.ListItemChannelUpdatesBinding
import com.thinkwik.communimate.module.model.ChannelUpdatesModel

class ChannelsUpdatesAdapter(
    private val context: Context,
    private var channelUpdateList: ArrayList<ChannelUpdatesModel>,
    /*private val onItemClick: (model: ChannelsModel, isFollowButtonClicked: Boolean) -> Unit,*/
) : RecyclerView.Adapter<ChannelsUpdatesAdapter.ChannelsUpdatesViewHolder>() {

    inner class ChannelsUpdatesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemChannelUpdatesBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelsUpdatesViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_channel_updates, parent, false)
        return ChannelsUpdatesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return channelUpdateList.size
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ChannelsUpdatesViewHolder, position: Int) {
        val model = channelUpdateList[position]

        when {
            model.updateType.equals("text") -> {
                holder.binding.tvText.text = model.channelText
                holder.binding.llMedia.isVisible = false
            }

            model.updateType.equals("image") -> {
                holder.binding.tvText.text = model.channelText
                if (model.channelText.toString().isEmpty()) {
                    holder.binding.tvText.isVisible = false
                }
            }

            model.updateType.equals("video") -> {
                holder.binding.btnVideoPlay.isVisible = true
            }
        }

        Glide.with(context)
            .load(model.url)
            .placeholder(R.drawable.progress_animation)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.binding.ivChannelImage)

    }

    fun updateList(filterList: ArrayList<ChannelUpdatesModel>) {
        channelUpdateList = filterList
        notifyDataSetChanged()
    }
}
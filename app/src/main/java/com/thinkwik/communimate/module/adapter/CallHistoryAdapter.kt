package com.thinkwik.communimate.module.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thinkwik.communimate.R
import com.thinkwik.communimate.databinding.ListItemCallHistoryBinding
import com.thinkwik.communimate.module.model.CallHistoryModel

class CallHistoryAdapter(
    private val context: Context,
    var callHistoryList: ArrayList<CallHistoryModel>
) : RecyclerView.Adapter<CallHistoryAdapter.CallHistoryViewHolder>() {

    inner class CallHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemCallHistoryBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_call_history, parent, false)
        return CallHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CallHistoryViewHolder, position: Int) {
        val model = callHistoryList[position]

        holder.binding.tvName.text = model.userName.toString()
        holder.binding.tvTimeAgo.text = model.datetime.toString()

        Glide.with(context)
            .load(model.imageUrl)
            .into(holder.binding.ivProfile)

        val status = when (model.callType) {
            "incoming" -> {
                holder.binding.ivCallStatus.setColorFilter(
                    Color.rgb(25, 136, 122),
                    PorterDuff.Mode.SRC_ATOP
                )
                ContextCompat.getDrawable(context, R.drawable.arrow_down_left)
            }

            "outgoing" -> {
                holder.binding.ivCallStatus.setColorFilter(
                    Color.rgb(25, 136, 122),
                    PorterDuff.Mode.SRC_ATOP
                )
                ContextCompat.getDrawable(context, R.drawable.arrow_up_right)
            }

            else -> {
                println("invalid status")
            }
        }

        Glide.with(context)
            .load(status)
            .into(holder.binding.ivCallStatus)

        val callStatus = when (model.callMediaType) {
            "voice" -> ContextCompat.getDrawable(context, R.drawable.ic_call)
            "video" -> ContextCompat.getDrawable(context, R.drawable.ic_video)
            else -> {
                println("invalid status")
            }
        }

        holder.binding.ivCallStatus.setColorFilter(
            Color.rgb(25, 136, 122),
            PorterDuff.Mode.SRC_ATOP
        )
        Glide.with(context)
            .load(callStatus)
            .into(holder.binding.ivCallType)
    }

    override fun getItemCount(): Int {
        return callHistoryList.size
    }

}
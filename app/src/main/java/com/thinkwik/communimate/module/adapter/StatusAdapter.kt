package com.thinkwik.communimate.module.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.thinkwik.communimate.R
import com.thinkwik.communimate.databinding.ListItemStatusBinding
import com.thinkwik.communimate.module.model.UserModel
import com.thinkwik.communimate.widget.story.StoryModel
import com.thinkwik.communimate.widget.story.StoryPreference

class StatusAdapter(
    private val context: Context,
    private val storyPreference: StoryPreference,
    private var statusList: ArrayList<UserModel>,
    private val onItemClick: (model: UserModel, position: Int) -> Unit,
) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {


    inner class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemStatusBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_status, parent, false)
        return StatusViewHolder(view)
    }

    override fun getItemCount(): Int {
        return statusList.size
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val model = statusList[position]
        val statusList = model.storyList
        holder.binding.tvName.text = model.name

        /*val imagelist = arrayListOf(
            StoryModel(
                "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1694427285333?alt=media&token=911413df-ccd3-4596-8d5d-c35a28f10e13",
                "11:32 am"
            ),
            StoryModel(
                "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1695185363649?alt=media&token=520982fe-8151-4f78-9302-3b1dc91061a5",
                "2:34 pm "
            ),
            StoryModel(
                "https://e0.pxfuel.com/wallpapers/315/1014/desktop-wallpaper-dark-anime-dark-anime-mobile.jpg",
                "2:34 pm "
            ),
            StoryModel(
                "https://images.wondershare.com/drfone/article/2023/03/whatsapp-status-10.jpg",
                "2:34 pm "
            ),
            StoryModel(
                "https://i.pinimg.com/originals/ce/74/05/ce7405d24eed479b37beadc44c93bb32.jpg",
                "2:34 pm "
            ),
            StoryModel(
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT7kRPxQQYsD5PnavNgq7RJeQKSeJw2RSib0A&usqp=CAU",
                "2:34 pm "
            ),
            StoryModel(
                "https://funkylife.in/wp-content/uploads/2021/07/whatsapp-status-15.jpg",
                "2:34 pm "
            ),
            StoryModel(
                "https://talkinnow.com/wp-content/uploads/2018/04/Sad-WhatsApp-Status-in-One-Line.jpg",
                "2:34 pm "
            ),
            StoryModel(
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSDjOOdfhFLN9tiC1_p_Iauj4jrU03uNW-wz2yVj9_Y-xws_HPRkvykR14HfeaBDAMevAY&usqp=CAU",
                "2:34 pm "
            )

        )
        val randomSubList = getRandomSubList(imagelist)*/


        /*   if (statusList!!.isNotEmpty()) {
               holder.binding.storyView.setActivityContext(context as AppCompatActivity)
               *//*holder.binding.storyView.resetStoryVisits()*//*
            holder.binding.storyView.setImageUris(statusList)
            holder.binding.tvTimeAgo.text = model.storyList[0].dateTime
        }*/

        /* Glide.with(context)
             .load(model.imageUrl)
             .diskCacheStrategy(DiskCacheStrategy.ALL)
             .circleCrop()
             .into(holder.binding.ivProfile)*/
        holder.binding.tvTimeAgo.text = statusList!![statusList.size - 1].dateTime
        holder.binding.llAddStatus.background = getIndicatorDrawable(statusList)
        Glide.with(context)
            .load(statusList[statusList.size - 1].imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .into(holder.binding.ivStatus)

        holder.itemView.setOnClickListener {
            onItemClick(model, position)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(statusList: ArrayList<UserModel>) {
        Log.d("status", "updateList: ")
        this.statusList = statusList
        notifyDataSetChanged()
    }

    private fun getIndicatorDrawable(myStory: ArrayList<StoryModel>): Drawable? {
        return if (storyPreference.isStoryVisited(myStory[myStory.size - 1].imageUrl!!)) {
            ContextCompat.getDrawable(context, R.drawable.bg_circle_visited)
        } else {
            ContextCompat.getDrawable(context, R.drawable.bg_circle_pending)
        }
    }

}
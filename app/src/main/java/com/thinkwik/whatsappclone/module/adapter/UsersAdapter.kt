package com.thinkwik.whatsappclone.module.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.databinding.ListItemUsersBinding
import com.thinkwik.whatsappclone.module.model.UserModel

class UsersAdapter(
    private val context: Context,
    private var userlist: ArrayList<UserModel>,
    private val onItemClick: (UserModel, position: Int) -> Unit
) :
    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {

    inner class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemUsersBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_users, parent, false)
        return UsersViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userlist.size
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val itemModel = userlist[position]
        Glide.with(context)
            .load(itemModel.imageUrl)
            .placeholder(R.drawable.profile)
            .error(R.drawable.profile)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop().into(holder.binding.ivProfile)
        holder.binding.tvUserName.text = itemModel.name
        Log.d("userAdapter", "onBindViewHolder: itemModel.isOnline : ${itemModel.online}")
        /*if (itemModel.online == "true") {
            holder.binding.ivStatus.setBackgroundColor(ContextCompat.getColor(context,R.color.green))
        } else {
            holder.binding.ivStatus.setBackgroundColor(ContextCompat.getColor(context,R.color.dark_300))
        }*/

        holder.itemView.setOnClickListener {
            onItemClick(itemModel, position)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(userlist: ArrayList<UserModel>) {
        this.userlist = userlist
        notifyDataSetChanged()
    }
}
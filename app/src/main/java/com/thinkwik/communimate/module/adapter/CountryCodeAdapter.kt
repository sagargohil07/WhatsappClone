package com.thinkwik.communimate.module.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thinkwik.communimate.R
import com.thinkwik.communimate.databinding.ListItemCountryBinding
import com.thinkwik.communimate.module.model.CountryCodeModel

class CountryCodeAdapter(
    private var countryCodeList: ArrayList<CountryCodeModel>,
    private var onCountryClick: (CountryCodeModel) -> Unit
) : RecyclerView.Adapter<CountryCodeAdapter.CountryViewHolder>() {

    interface OnCountryClick {
        fun onClick(countryCode: String)
    }

    inner class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListItemCountryBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_country, parent, false)
        return CountryViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val itemModel = countryCodeList[position]

        if (itemModel.code.contains("+")) {
            itemModel.code.replace("+", "(+")
            holder.binding.tvCountryCode.text = "(${itemModel.code})"
            holder.binding.tvCountryName.text = "${itemModel.name} "
        } else {
            holder.binding.tvCountryCode.text = "(+${itemModel.code})"
            holder.binding.tvCountryName.text = itemModel.name
        }
        holder.itemView.setOnClickListener {
            onCountryClick(itemModel)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterData(countryCodeList: ArrayList<CountryCodeModel>) {
        this.countryCodeList = countryCodeList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return countryCodeList.size
    }

}
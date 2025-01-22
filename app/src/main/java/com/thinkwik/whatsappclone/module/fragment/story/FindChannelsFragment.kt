package com.thinkwik.whatsappclone.module.fragment.story

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentFindChannelsBinding
import com.thinkwik.whatsappclone.module.adapter.ChannelsFindAdapter
import com.thinkwik.whatsappclone.module.adapter.RadioAdapter
import com.thinkwik.whatsappclone.module.model.ChannelsModel
import com.thinkwik.whatsappclone.module.model.CountryCodeModel
import com.thinkwik.whatsappclone.module.model.RadioItem
import com.thinkwik.whatsappclone.requireMainActivity
import com.thinkwik.whatsappclone.utils.DBHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class FindChannelsFragment :
    BaseFragment<FragmentFindChannelsBinding>(R.layout.fragment_find_channels) {

    private lateinit var dialog: BottomSheetDialog
    private var countryCodeArrayList: ArrayList<CountryCodeModel> = ArrayList()
    private var radioCountryList = arrayListOf<RadioItem>()

    private lateinit var channelsList: ArrayList<ChannelsModel>

    private var selectedOption = "All"

    private lateinit var radioAdapter: RadioAdapter
    private lateinit var findChannelsAdapter: ChannelsFindAdapter

    private lateinit var dbHelper: DBHelper

    private val navOptions = NavOptions.Builder()
        .setEnterAnim(R.anim.slide_in_right)
        .setExitAnim(R.anim.slide_out_left)
        .setPopEnterAnim(R.anim.slide_in_left)
        .setPopExitAnim(R.anim.slide_out_right)
        .build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        dbHelper = DBHelper(requireContext())
        setChannelOptions(binding.tvAll)
        fetchCountryCodes()
        initChannelsAdapter()
        initListener()
    }

    private fun initListener() {
        binding.toolbarSearch.setOnClickListener {
            binding.llSearch.isVisible = true
            binding.llFilterTag.isVisible = !binding.llSearch.isVisible
            binding.etSearchContact.requestFocus()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etSearchContact, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.btnBackSearch.setOnClickListener {
            binding.llSearch.isVisible = false
            binding.llFilterTag.isVisible = !binding.llSearch.isVisible
            binding.etSearchContact.setText("")
            requireMainActivity().hideKeyboard(binding.etSearchContact)
        }
        binding.etSearchContact.addTextChangedListener {
            searchList(it.toString())
        }
        binding.tvAll.setOnClickListener {
            selectedOption = "All"
            setCurrentOption()
        }
        binding.tvMostActive.setOnClickListener {
            selectedOption = "MostActive"
            setChannelOptions(binding.tvMostActive)
        }
        binding.tvNew.setOnClickListener {
            selectedOption = "New"
            setChannelOptions(binding.tvNew)
        }
        binding.tvPopular.setOnClickListener {
            selectedOption = "Popular"
            setChannelOptions(binding.tvPopular)
        }
        binding.tvCountry.setOnClickListener {
            setChannelOptions(binding.tvCountry)
            showBottomSheet()
        }
    }

    private fun searchList(text: String) {
        val filterList = ArrayList<ChannelsModel>()
        for (i in 0 until channelsList.size) {
            if (channelsList[i].channelName.contains(text, true)) {
                filterList.add(channelsList[i])
            }
        }
        binding.tvResultNotFound.isVisible = filterList.isEmpty()
        findChannelsAdapter.updateList(filterList)
    }

    private fun setCurrentOption() {
        when (selectedOption) {
            "All" -> {
                setChannelOptions(binding.tvAll)
            }

            "MostActive" -> {
                setChannelOptions(binding.tvMostActive)
            }

            "New" -> {
                setChannelOptions(binding.tvNew)
            }

            "Popular" -> {
                setChannelOptions(binding.tvPopular)
            }
        }
    }

    private fun setChannelOptions(textViewViewSelected: View) {
        binding.tvAll.isSelected = false
        binding.tvMostActive.isSelected = false
        binding.tvNew.isSelected = false
        binding.tvPopular.isSelected = false
        binding.tvCountry.isSelected = false
        textViewViewSelected.isSelected = true
    }

    private fun initChannelsAdapter() {
        channelsList = dbHelper.getAllChannels() as ArrayList<ChannelsModel>
        channelsList = channelsList.filter { it.isMyChannel == 0 } as ArrayList<ChannelsModel>
        findChannelsAdapter =
            ChannelsFindAdapter(requireContext(), channelsList) { model, isFollowButtonClicked ->
                if (isFollowButtonClicked) {
                    if (model.isFollowing == 0) {
                        dbHelper.updateChannelIsFollowing(model.channelName, false)
                    } else {
                        dbHelper.updateChannelIsFollowing(model.channelName, true)
                    }
                } else {
                    val bundle = Bundle()
                    bundle.putSerializable("model", model)
                    findNavController().navigate(
                        R.id.nav_show_channel_update_fragment,
                        bundle,
                        navOptions
                    )
                }
                /*val bundle = Bundle()
                bundle.putSerializable("model", model)
                findNavController().navigate(R.id.nav_show_channel_update_fragment, bundle, navOptions)*/
            }
        binding.rvFindChannels.adapter = findChannelsAdapter
    }

    private fun showBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_radio_country, null)
        dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        dialog.setContentView(dialogView)

        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val rvRadio = dialogView.findViewById<RecyclerView>(R.id.rvAppRadio)
        val etSearch = dialogView.findViewById<AppCompatEditText>(R.id.etSearch)

        etSearch.isVisible = true

        // Set up RecyclerView outside of UI thread
        CoroutineScope(Dispatchers.Default).launch {
            val radioAdapter = RadioAdapter(radioCountryList) { model, _ ->
                dialog.dismiss()
            }
            withContext(Dispatchers.Main) {
                progressBar.isVisible = false
                rvRadio.adapter = radioAdapter
            }
        }

        if (!dialog.isShowing) {
            binding.tvCountry.isEnabled = false
            dialog.show()
        }
        dialog.setOnDismissListener {
            binding.tvCountry.isEnabled = true
            setCurrentOption()
        }
        dialog.setOnCancelListener {
            binding.tvCountry.isEnabled = true
            setCurrentOption()
        }
    }

    private fun getCountryList() {
        radioCountryList.add(RadioItem(0, "India", true))
        for (i in 1 until countryCodeArrayList.size) {
            val model = countryCodeArrayList[i]
            if (model.name != "India") {
                radioCountryList.add(RadioItem(i, model.name, false))
            }
        }
    }

    private fun fetchCountryCodes() {
        GlobalScope.launch(Dispatchers.IO) {
            val jsonArray = JSONArray(loadJSONFromResource(requireContext()))
            for (i in 0 until jsonArray.length()) {
                val temp: JSONObject = jsonArray[i] as JSONObject
                countryCodeArrayList.add(
                    CountryCodeModel(
                        temp.getString("name"),
                        temp.getString("phoneCode")
                    )
                )
            }
            getCountryList()
        }
    }

    private fun loadJSONFromResource(mContext: Context): String {
        var json: String? = null
        try {
            val inputStream = mContext.resources.openRawResource(R.raw.country)
            json = inputStream.bufferedReader().readText()
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ""
        }
        return json
    }

}
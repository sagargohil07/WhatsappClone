package com.thinkwik.communimate.module.fragment.auth

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentLoginBinding
import com.thinkwik.communimate.extensions.setDebounce
import com.thinkwik.communimate.module.adapter.CountryCodeAdapter
import com.thinkwik.communimate.module.model.CountryCodeModel
import com.thinkwik.communimate.requireMainActivity
import com.thinkwik.communimate.utils.runOnUiThread
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {

    private lateinit var auth: FirebaseAuth
    private var countryCodeArrayList: ArrayList<CountryCodeModel> = ArrayList()
    private lateinit var dialog: BottomSheetDialog
    private lateinit var countryCodeAdapter: CountryCodeAdapter
    private lateinit var tvResult: TextView
    private var countryCode: String = "+"
    private var countryName: String = "India"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        initListener()
        fetchCountryCodes()
        auth = FirebaseAuth.getInstance()
        requireMainActivity().showKeyboard(binding.etNumber)

        /*if (auth != null) {
            findNavController().navigate(LoginFragmentDirections.toNavHomeFragment())
        }*/
    }

    private fun initListener() {

        binding.etCountryCode.setOnClickListener {
            showBottomSheet()
        }

        binding.etCountryName.setOnClickListener {
            showBottomSheet()
        }

        binding.etNumber.setDebounce {
            runOnUiThread {
                validateField()
            }
        }

        binding.btnNext.setOnClickListener {
            if (validateField()) {
                countryCode = binding.etCountryCode.text.toString().trim()
                val bundle = bundleOf(
                    "phoneNumber" to binding.etNumber.text.toString().trim(),
                    "countryCode" to countryCode,
                    "countryName" to countryName
                )
                /*val number =
                    binding.etCountryCode.text.toString().trim() + binding.etNumber.text.toString()
                        .trim()*/
                findNavController().navigate(R.id.nav_otp_fragment, bundle)
            }
        }
    }

    private fun validateField(): Boolean {
        if (binding.etNumber.text!!.toString().length < 10) {
            return false
            binding.btnNext.isEnabled = false
            binding.btnNext.alpha = 0.5f
        } else if (binding.etCountryCode.text.toString().isNullOrEmpty()) {
            binding.btnNext.isEnabled = false
            binding.btnNext.alpha = 0.5f
            return false
        }

        binding.btnNext.isEnabled = true
        binding.btnNext.alpha = 1f
        return true
    }

    private fun showBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_country_code_picker, null)
        dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        dialog.setContentView(dialogView)

        tvResult = dialogView.findViewById(R.id.tvResultNotFound)

        val search = dialogView.findViewById<EditText>(R.id.etSearchCountry)
        search.setDebounce {
            runOnUiThread {
                search(search.text.toString())
            }
        }

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvCountry)
        countryCodeAdapter = CountryCodeAdapter(
            countryCodeArrayList
        ) {
            countryCode = "+${it.code}"
            countryName = it.name
            binding.etCountryCode.setText(countryCode)
            binding.etCountryName.setText(countryName)
            dialog.dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = countryCodeAdapter

        if (!dialog.isShowing) {
            dialog.show()
        }

    }

    private fun search(text: String) {
        val filterList = ArrayList<CountryCodeModel>()
        for (i in 0 until countryCodeArrayList.size) {
            if (countryCodeArrayList[i].name.contains(
                    text,
                    true
                ) or countryCodeArrayList[i].code.contains(text, true)
            ) {
                filterList.add(
                    CountryCodeModel(
                        countryCodeArrayList[i].name,
                        countryCodeArrayList[i].code
                    )
                )
            }
        }

        if (filterList.size == 0) {
            tvResult.visibility = View.VISIBLE
        } else if (text.isEmpty() || filterList.size > 0) {
            tvResult.visibility = View.GONE
        }
        countryCodeAdapter.filterData(filterList)
    }

    private fun fetchCountryCodes() {
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
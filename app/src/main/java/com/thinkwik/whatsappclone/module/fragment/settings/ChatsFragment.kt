package com.thinkwik.whatsappclone.module.fragment.settings

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.base.BaseFragment
import com.thinkwik.whatsappclone.databinding.FragmentChatsBinding
import com.thinkwik.whatsappclone.module.adapter.RadioAdapter
import com.thinkwik.whatsappclone.module.model.RadioItem
import com.thinkwik.whatsappclone.prefs.PreferenceStorage
import com.thinkwik.whatsappclone.requireMainActivity
import org.koin.android.ext.android.inject

class ChatsFragment : BaseFragment<FragmentChatsBinding>(R.layout.fragment_chats) {

    private var isSwitchOn1 = false
    private var isSwitchOn2 = true
    private var isSwitchOn3 = false
    private var isSwitchOn4 = true

    private lateinit var selectedTheme: String

    private lateinit var radioAdapter: RadioAdapter
    private var radioThemeList = listOf(
        RadioItem(1, "System Default", false),
        RadioItem(2, "Dark", false),
        RadioItem(3, "Light", false),
    )

    private val prefs: PreferenceStorage by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        Log.d("theme", "init: prefs.appTheme.toString() ${prefs.appTheme.toString()}")
        selectedTheme = prefs.appTheme.toString()
        binding.tvTheme.text = selectedTheme
        initListener()
    }

    private fun initListener() {

        binding.llTheme.setOnClickListener {
            Log.d("theme", "initListener: prefs.appTheme.toString() ${prefs.appTheme.toString()}")
            when (prefs.appTheme.toString().uppercase()) {
                "System default".uppercase() -> {
                    radioThemeList[0].isSelected = true
                }

                "dark".uppercase() -> {
                    radioThemeList[1].isSelected = true
                }

                "light".uppercase() -> {
                    radioThemeList[2].isSelected = true
                }
            }
            dialogRadio("Theme", radioThemeList)
        }
        binding.ivSwitch1.setOnClickListener {
            isSwitchOn1 = !isSwitchOn1
            val result = if (isSwitchOn1) {
                binding.ivSwitch1.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green_100
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_on)
            } else {
                binding.ivSwitch1.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text_gray
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_off)
            }
            binding.ivSwitch1.setImageDrawable(result)
        }
        binding.ivSwitch2.setOnClickListener {
            isSwitchOn2 = !isSwitchOn2
            val result = if (isSwitchOn2) {
                binding.ivSwitch2.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green_100
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_on)
            } else {
                binding.ivSwitch2.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text_gray
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_off)
            }
            binding.ivSwitch2.setImageDrawable(result)
        }
        binding.ivSwitch3.setOnClickListener {
            isSwitchOn3 = !isSwitchOn3
            val result = if (isSwitchOn3) {
                binding.ivSwitch3.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green_100
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_on)
            } else {
                binding.ivSwitch3.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text_gray
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_off)
            }
            binding.ivSwitch3.setImageDrawable(result)
        }
        binding.ivSwitch4.setOnClickListener {
            isSwitchOn4 = !isSwitchOn4
            val result = if (isSwitchOn4) {
                binding.ivSwitch4.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.green_100
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_on)
            } else {
                binding.ivSwitch4.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text_gray
                    )
                )
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_off)
            }
            binding.ivSwitch4.setImageDrawable(result)
        }

    }

    private fun dialogRadio(title: String, list: List<RadioItem>) {
        val dialog = Dialog(requireContext(), R.style.DialogStyle)
        dialog.setContentView(R.layout.dialog_radio)
        /* val d = ColorDrawable(Color.BLACK)
         d.alpha = 150
         dialog.window?.setBackgroundDrawable(d)*/
        /*dialog.window?.setDimAmount(alphaToDimAmount(150))*/
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvTitle = dialog.findViewById<AppCompatTextView>(R.id.tvTitleRadio)
        dialog.findViewById<LinearLayoutCompat>(R.id.llActions).isVisible = true

        val cancel = dialog.findViewById<AppCompatTextView>(R.id.tvCancel)
        val okay = dialog.findViewById<AppCompatTextView>(R.id.tvOkay)

        cancel.setOnClickListener {
            dialog.dismiss()
        }
        okay.setOnClickListener {
            prefs.appTheme = selectedTheme
            binding.tvTheme.text = prefs.appTheme.toString()
            dialog.dismiss()
            requireMainActivity().setAppTheme()
        }

        val rvRadio = dialog.findViewById<RecyclerView>(R.id.rvRadio)
        tvTitle.text = title
        Log.d("theme", "initListener: RadioThemeList : ${list.joinToString()}")
        radioAdapter = RadioAdapter(list) { it, position ->
            selectedTheme = it.text
            updateRadioList(position)
        }
        rvRadio.adapter = radioAdapter

        dialog.show()
    }

    private fun updateRadioList(position: Int) {
        for (i in radioThemeList.indices) {
            radioThemeList[i].isSelected = i == position
        }
    }
}
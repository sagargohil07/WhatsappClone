package com.thinkwik.communimate.module.fragment.settings

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentNotificationsBinding
import com.thinkwik.communimate.module.adapter.RadioAdapter
import com.thinkwik.communimate.module.model.RadioItem

class NotificationsFragment :
    BaseFragment<FragmentNotificationsBinding>(R.layout.fragment_notifications) {

    private val switchStates = mutableListOf(false, true, false, true, false)
    private lateinit var switchViews: List<AppCompatImageView>

    private lateinit var radioAdapter: RadioAdapter
    private val radioVibrate = listOf(
        RadioItem(1, "off", false),
        RadioItem(2, "Default", false),
        RadioItem(3, "Short", false),
        RadioItem(4, "Long", false),
    )
    private val radioLight = listOf(
        RadioItem(1, "Node", false),
        RadioItem(2, "White", false),
        RadioItem(3, "Red", false),
        RadioItem(4, "Yellow", false),
        RadioItem(5, "Green", false),
        RadioItem(6, "Cyan", false),
        RadioItem(7, "Blue", false),
        RadioItem(8, "Purple", false),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        switchViews = listOf(
            binding.ivSwitch1,
            binding.ivSwitch2,
            binding.ivSwitch3,
            binding.ivSwitch4,
            binding.ivSwitch5
        )
        initListener()
    }

    private fun initListener() {
        switchViews.forEachIndexed { index, switchView ->
            switchView.setOnClickListener {
                switchStates[index] = !switchStates[index]
                updateSwitchUI(index)
            }
        }

        binding.tvVibrateMessage.setOnClickListener {
            dialogRadio("Vibrate", radioVibrate)
        }
        binding.tvLightMessage.setOnClickListener {
            dialogRadio("Light", radioLight)
        }
        binding.tvVibrateGroup.setOnClickListener {
            dialogRadio("Vibrate", radioVibrate)
        }
        binding.tvLightGroup.setOnClickListener {
            dialogRadio("Light", radioLight)
        }
        binding.tvVibrateCalls.setOnClickListener {
            dialogRadio("Vibrate", radioVibrate)
        }
    }

    private fun updateSwitchUI(index: Int) {
        val switchState = switchStates[index]
        val switchView = switchViews[index]
        val colorId = if (switchState) R.color.green_100 else R.color.text_gray
        val drawableId = if (switchState) R.drawable.ic_switch_on else R.drawable.ic_switch_off

        switchView.setColorFilter(ContextCompat.getColor(requireContext(), colorId))
        switchView.setImageDrawable(ContextCompat.getDrawable(requireContext(), drawableId))
    }

    private fun dialogRadio(title: String, list: List<RadioItem>) {
        val dialog = Dialog(requireContext(), R.style.DialogStyle)
        dialog.setContentView(R.layout.dialog_radio)
        /* val d = ColorDrawable(Color.BLACK)
         d.alpha = 150
         dialog.window?.setBackgroundDrawable(d)*/
        /*        dialog.window?.setDimAmount(alphaToDimAmount(150))*/
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )


        val tvTitle = dialog.findViewById<AppCompatTextView>(R.id.tvTitleRadio)
        val rvRadio = dialog.findViewById<RecyclerView>(R.id.rvRadio)
        tvTitle.text = title
        radioAdapter = RadioAdapter(list) { _, _ ->
            dialog.dismiss()
        }
        rvRadio.adapter = radioAdapter

        dialog.show()
    }

    fun alphaToDimAmount(alpha: Int): Float {
        // Ensure alpha is in the valid range [0, 255]
        val clampedAlpha = alpha.coerceIn(0, 255)

        // Calculate the dim amount
        return clampedAlpha / 255.0f
    }


}
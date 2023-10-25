package com.thinkwik.communimate.module.fragment.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentSettingBinding
import com.thinkwik.communimate.module.adapter.RadioAdapter
import com.thinkwik.communimate.module.adapter.SettingsAdapter
import com.thinkwik.communimate.module.model.RadioItem
import com.thinkwik.communimate.module.model.SettingsModel
import com.thinkwik.communimate.prefs.PreferenceStorage
import com.thinkwik.communimate.requireMainActivity
import org.koin.android.ext.android.inject

class SettingFragment : BaseFragment<FragmentSettingBinding>(R.layout.fragment_setting) {

    private val prefs: PreferenceStorage by inject()

    private lateinit var selectedAppLanguage: String

    private lateinit var settingsAdapter: SettingsAdapter
    private lateinit var settingsSearchAdapter: SettingsAdapter
    private lateinit var radioAdapter: RadioAdapter

    private var settingsList: ArrayList<SettingsModel> = ArrayList<SettingsModel>()
    private var settingsOptionList: ArrayList<SettingsModel> = ArrayList<SettingsModel>()
    private lateinit var dialog: BottomSheetDialog

    private val radioLanguageList = listOf(
        RadioItem(1, "English", false),
        RadioItem(2, "Hindi", false),
        RadioItem(3, "Gujarati", false),
        RadioItem(9, "Japanese", false),
        RadioItem(4, "Spanish", false),
        RadioItem(5, "German", false),
        RadioItem(7, "Korean", false),
        /*RadioItem(8, "French", false),*/
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    @SuppressLint("CheckResult")
    private fun init() {
        Glide.with(requireActivity())
            .load(prefs.userProfileImage.toString())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .into(binding.ivProfile)
        binding.tvName.text = prefs.userName.toString()
        binding.tvInfo.text = prefs.userName.toString()

        selectedAppLanguage = prefs.appLanguage.toString()

        initSettingAdapter()
        initSearchSettingAdapter()
        initListener()
    }

    private fun initListener() {
        binding.toolbarSearch.setOnClickListener {
            binding.llSearch.isVisible = true
            binding.etSearchContact.requestFocus()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etSearchContact, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.btnBackSearch.setOnClickListener {
            binding.llSearch.isVisible = false
            binding.etSearchContact.setText("")
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etSearchContact.windowToken, 0)
        }
        binding.etSearchContact.addTextChangedListener {
            binding.llMain.isVisible = it?.isEmpty() ?: true
            binding.btnClear.isVisible = !it?.isEmpty()!!
            binding.llSearchView.isVisible = !binding.llMain.isVisible
            searchList(it.toString())
        }
        binding.llProfile.setOnClickListener {
            findNavController().navigate(SettingFragmentDirections.toNavUpdateProfileFragment())
        }
        binding.btnClear.setOnClickListener {
            binding.etSearchContact.setText("")
        }

    }

    private fun initSearchSettingAdapter() {
        settingsSearchAdapter = SettingsAdapter(requireContext(), settingsOptionList) { model ->
            when (model.screen) {
                "account" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavAccountFragment())
                }

                "privacy" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavPrivacyFragment())
                }

                "chats" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavChatsFragment())
                }

                "notification" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavNotificationsFragment())
                }

                "storage_and_data" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavStorageAndDataFragment())
                }

                "app_language" -> {
                    showBottomSheet()
                }

                "help" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavHelpFragment())
                }

                "invite_friend" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavInviteFriendFragment())
                }
            }
        }
        binding.rvSearchSetting.adapter = settingsSearchAdapter
    }

    private fun initSettingAdapter() {
        settingsAdapter = SettingsAdapter(requireContext(), getSettingList()) { model ->
            when (model.screen) {
                "account" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavAccountFragment())
                }

                "privacy" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavPrivacyFragment())
                }

                "chats" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavChatsFragment())
                }

                "notification" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavNotificationsFragment())
                }

                "storage_and_data" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavStorageAndDataFragment())
                }

                "app_language" -> {
                    showBottomSheet()
                }

                "help" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavHelpFragment())
                }

                "invite_friend" -> {
                    findNavController().navigate(SettingFragmentDirections.toNavInviteFriendFragment())
                }
            }
        }
        binding.rvSetting.adapter = settingsAdapter
    }

    private fun showBottomSheet() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_app_language, null)
        dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        dialog.setContentView(dialogView)

        val ivClose = dialogView.findViewById<AppCompatImageView>(R.id.ivClose)
        ivClose.setOnClickListener {
            dialog.dismiss()
        }
        when {
            prefs.appLanguage.equals("English", true) -> {
                radioLanguageList[0].isSelected = true
            }

            prefs.appLanguage.equals("Hindi", true) -> {
                radioLanguageList[1].isSelected = true
            }

            prefs.appLanguage.equals("Gujarati", true) -> {
                radioLanguageList[2].isSelected = true
            }

            prefs.appLanguage.equals("Japanese", true) -> {
                radioLanguageList[3].isSelected = true
            }

            prefs.appLanguage.equals("Spanish", true) -> {
                radioLanguageList[4].isSelected = true
            }

            prefs.appLanguage.equals("German", true) -> {
                radioLanguageList[5].isSelected = true
            }

            prefs.appLanguage.equals("Korean", true) -> {
                radioLanguageList[6].isSelected = true
            }

            /* prefs.appLanguage.equals("French", true) -> {
                 radioLanguageList[7].isSelected = true
             }*/

            else -> {
                radioLanguageList[0].isSelected = true
            }
        }

        val rvRadio = dialogView.findViewById<RecyclerView>(R.id.rvAppRadio)
        radioAdapter = RadioAdapter(radioLanguageList) { model, _ ->
            prefs.appLanguage = model.text
            requireMainActivity().setLanguage()
            dialog.dismiss()
        }
        rvRadio.layoutManager = LinearLayoutManager(context)
        rvRadio.adapter = radioAdapter

        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun getSettingList(): ArrayList<SettingsModel> {
        settingsList.clear()
        settingsList.add(
            SettingsModel(
                screen = "account",
                title = getString(R.string.account),
                info = getString(R.string.security_notification_change_number),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_key),
            )
        )
        settingsList.add(
            SettingsModel(
                screen = "privacy",
                title = getString(R.string.privacy),
                info = getString(R.string.block_contacts_disappearing_messages),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_lock)
            )
        )
        settingsList.add(
            SettingsModel(
                screen = "chats",
                title = getString(R.string.chats),
                info = getString(R.string.theme_wallpapers_chat_history),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_chats)
            )
        )
        settingsList.add(
            SettingsModel(
                screen = "notification",
                title = getString(R.string.notification),
                info = getString(R.string.message_group_and_call_tones),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_notification),
            )
        )
        settingsList.add(
            SettingsModel(
                screen = "storage_and_data",
                title = getString(R.string.storage_and_data),
                info = getString(R.string.network_usage_auto_download),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_chart),
            )
        )
        settingsList.add(
            SettingsModel(
                screen = "app_language",
                title = getString(R.string.app_language),
                info = prefs.appLanguage.toString(),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_web),
            )
        )
        settingsList.add(
            SettingsModel(
                screen = "help",
                title = getString(R.string.help),
                info = getString(R.string.help_center_contact_us_privacy_policy),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_help),
            )
        )
        settingsList.add(
            SettingsModel(
                screen = "invite_friend",
                title = getString(R.string.invite_a_friend),
                info = "",
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_group_2),
            )
        )

        return settingsList
    }

    private fun searchList(text: String) {
        val settingList = settingOptionList()
        val filterList = ArrayList<SettingsModel>()
        for (i in 0 until settingList.size) {
            if (settingList[i].title.contains(text, true)) {
                filterList.add(settingList[i])
            }
        }
        settingsSearchAdapter.updateList(filterList)
        binding.tvSearchResult.isVisible = filterList.isNullOrEmpty()
    }

    private fun settingOptionList(): ArrayList<SettingsModel> {
        settingsOptionList.clear()
        //account
        settingsOptionList.add(
            SettingsModel(
                screen = "account",
                title = "Security notification",
                info = "Account",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "account",
                title = "Two-step verification",
                info = "Account",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "account",
                title = "Change number",
                info = "Account",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "account",
                title = "Request account info",
                info = "Account",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "account",
                title = "Delete account",
                info = "Account",
                icon = null,
            )
        )
        //privacy
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "Last seen and online",
                info = "Privacy",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "Profile photo",
                info = "Privacy",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "Status",
                info = "Privacy",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "About",
                info = "Privacy",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "Status",
                info = "Privacy",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "Read receipt",
                info = "Privacy",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "Disappearing messages",
                info = "Privacy",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "Groups ",
                info = "Privacy",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "Live location ",
                info = "Privacy",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "Calls ",
                info = "Privacy",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "Blocked contacts ",
                info = "Privacy",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "privacy",
                title = "Fingerprint lock ",
                info = "Privacy",
                icon = null,
            )
        )
        //chat
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Display ",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Theme ",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Wallpaper ",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Chat settings ",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Enter is send ",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Media visibility ",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Instant video message ",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Font size ",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Archived ",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Keep chats archived",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Chat backup",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Transfer chats",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "Chats history",
                info = "Chats",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "chats",
                title = "System default",
                info = "Chats",
                icon = null,
            )
        )
        //Notification
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Enter is send",
                info = "Notification",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Notification tone",
                info = "Notification>Message",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Vibrate",
                info = "Notification>Message",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Light",
                info = "Notification>Message",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Use high priority notifications",
                info = "Notification>Message",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Reaction Notification",
                info = "Notification>Message",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Notification tone",
                info = "Notification>Groups",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Vibrate",
                info = "Notification>Groups",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Light",
                info = "Notification>Groups",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Use high priority notifications",
                info = "Notification>Groups",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Reaction Notification",
                info = "Notification>Groups",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Ringtone",
                info = "Notification>Calls",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "notification",
                title = "Vibrate",
                info = "Notification>Calls",
                icon = null,
            )
        )
        //Storage and data
        settingsOptionList.add(
            SettingsModel(
                screen = "storage_and_data",
                title = "Manage storage",
                info = "Storage and data",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "storage_and_data",
                title = "Network usage",
                info = "Storage and data",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "storage_and_data",
                title = "Use less data for calls",
                info = "Storage and data",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "storage_and_data",
                title = "Media auto-download",
                info = "Storage and data",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "storage_and_data",
                title = "When using mobile data",
                info = "Storage and data",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "storage_and_data",
                title = "When connected on Wi-Fi",
                info = "Storage and data",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "storage_and_data",
                title = "When roaming",
                info = "Storage and data",
                icon = null,
            )
        )
        //App language
        settingsOptionList.add(
            SettingsModel(
                screen = "app_language",
                title = "App language",
                info = "App language",
                icon = null,
            )
        )
        //Help
        settingsOptionList.add(
            SettingsModel(
                screen = "help",
                title = "Help Center",
                info = "Help",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "help",
                title = "Contact us",
                info = "Help",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "help",
                title = "Terms and Privacy Policy",
                info = "Help",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "help",
                title = "App info",
                info = "Help",
                icon = null,
            )
        )
        //Invite Friend
        settingsOptionList.add(
            SettingsModel(
                screen = "invite_friend",
                title = "Invite",
                info = "Invite Friend",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "invite_friend",
                title = "Share Link",
                info = "Invite Friend",
                icon = null,
            )
        )
        settingsOptionList.add(
            SettingsModel(
                screen = "invite_friend",
                title = "From Contacts",
                info = "Invite Friend",
                icon = null,
            )
        )

        return settingsOptionList
    }

    private fun updateRadioList(position: Int) {
        for (i in radioLanguageList.indices) {
            radioLanguageList[i].isSelected = i == position
        }
    }
}
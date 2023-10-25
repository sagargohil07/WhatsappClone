package com.thinkwik.communimate.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.thinkwik.communimate.R

@Suppress("MemberVisibilityCanBePrivate")
open class BaseFragment<out Binding : ViewBinding>(@LayoutRes contentLayoutId: Int) :
    Fragment(contentLayoutId) {

    private var _binding: Binding? = null
    val binding: Binding
        get() = _binding ?: throw IllegalStateException(
            "Fragment $this binding cannot be accessed before onCreateView() or " +
                    "after onDestroyView() is called."
        )

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(
            inflater,
            container,
            savedInstanceState
        ) ?: throw IllegalStateException("Fragment $this did not return a View from onCreateView()")
        if (_binding?.root != null)
            return binding.root

        _binding = DataBindingUtil.bind(view)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.toolbar_back)?.let { backButton ->
            backButton.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
    }

    protected fun setStatusBarColor(color: Int) {
        val window = activity?.window ?: return
        window.statusBarColor = color
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            !color.isDarkColor()
    }

    protected fun setNavigationBarColor(color: Int) {
        val window = activity?.window ?: return
        window.navigationBarColor = color
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars =
            !color.isDarkColor()
    }

    fun Int.isDarkColor() = ColorUtils.calculateLuminance(this) < 0.35
}

package com.thinkwik.communimate.module.fragment.auth

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.thinkwik.communimate.R
import com.thinkwik.communimate.base.BaseFragment
import com.thinkwik.communimate.databinding.FragmentOtpBinding
import com.thinkwik.communimate.prefs.PreferenceStorage
import com.thinkwik.communimate.utils.checkLength
import com.thinkwik.communimate.utils.runOnUiThread
import org.koin.android.ext.android.inject
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

class OtpFragment : BaseFragment<FragmentOtpBinding>(R.layout.fragment_otp) {

    private lateinit var auth: FirebaseAuth
    private var database: FirebaseDatabase? = null
    private lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private val args: OtpFragmentArgs by navArgs()
    private val prefs: PreferenceStorage by inject()
    private var verificationId: String = ""
    private var timerCount = 60

    private lateinit var dialog: AlertDialog

    private var phoneNumber: String = ""
    private var countryCode: String = ""
    private var countryName: String = "+"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        init()
    }

    private fun init() {

        phoneNumber = arguments?.getString("phoneNumber").toString()
        countryCode = arguments?.getString("countryCode").toString()
        countryName = arguments?.getString("countryName").toString()

        initListener()
        val builder =
            AlertDialog.Builder(requireContext()).setMessage("Verifying....").setCancelable(false)
        dialog = builder.create()
        database = FirebaseDatabase.getInstance()
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("otp", "onVerificationCompleted: ${credential}")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("otp", "onVerificationFailed: ${e.message}")

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(
                        requireContext(),
                        "verification failed please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(
                        requireContext(),
                        "too many OTP request send",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                    Toast.makeText(requireContext(), "Recaptcha failed", Toast.LENGTH_SHORT).show()
                }

                /*binding.tvResendOtp.isEnabled = true
                binding.tvResendOtp.alpha = 1f
                binding.tvResendOtp.text = "Resend"*/
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Handler(Looper.getMainLooper()).postDelayed({
                    Log.d("otp", "onCodeSent: verificationId $verificationId")
                    binding.btnVerify.text = "Verify"
                    Toast.makeText(
                        requireContext(),
                        "we have send OTP to your number",
                        Toast.LENGTH_LONG
                    )
                    this@OtpFragment.verificationId = verificationId
                }, 1000)
            }
        }
        getOtp()
    }

    private fun initListener() {
        binding.otp1.checkLength(goToNext = {
            binding.otp1.clearFocus()
            binding.otp2.requestFocus()
        }, goToPrevious = {
            binding.otp1.clearFocus()
            binding.otp1.requestFocus()
        }, checkValidation = {
            validateOtp()
        })
        binding.otp2.checkLength(goToNext = {
            binding.otp2.clearFocus()
            binding.otp3.requestFocus()
        }, goToPrevious = {
            binding.otp2.clearFocus()
            binding.otp1.requestFocus()
        }, checkValidation = {
            validateOtp()
        })
        binding.otp3.checkLength(goToNext = {
            binding.otp3.clearFocus()
            binding.otp4.requestFocus()
        }, goToPrevious = {
            binding.otp3.clearFocus()
            binding.otp2.requestFocus()
        }, checkValidation = {
            validateOtp()
        })
        binding.otp4.checkLength(goToNext = {
            binding.otp4.clearFocus()
            binding.otp5.requestFocus()
        }, goToPrevious = {
            binding.otp4.clearFocus()
            binding.otp3.requestFocus()
        }, checkValidation = {
            validateOtp()
        })
        binding.otp5.checkLength(goToNext = {
            binding.otp5.clearFocus()
            binding.otp6.requestFocus()
        }, goToPrevious = {
            binding.otp5.clearFocus()
            binding.otp4.requestFocus()
        }, checkValidation = {
            validateOtp()
        })
        binding.otp6.checkLength(goToNext = {
            binding.otp6.clearFocus()
            binding.otp6.requestFocus()
        }, goToPrevious = {
            binding.otp6.clearFocus()
            binding.otp5.requestFocus()
        }, checkValidation = {
            validateOtp()
        })

        binding.btnVerify.setOnClickListener {

            dialog.show()
            val otp = StringBuilder()
            otp.append(binding.otp1.text.toString())
            otp.append(binding.otp2.text.toString())
            otp.append(binding.otp3.text.toString())
            otp.append(binding.otp4.text.toString())
            otp.append(binding.otp5.text.toString())
            otp.append(binding.otp6.text.toString())

            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, otp.toString())
                auth.signInWithCredential(credential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        dialog.dismiss()
                        /*Toast.makeText(requireContext(), "Number Verified Successful", Toast.LENGTH_SHORT).show()*/
                        Log.d(
                            "otp",
                            "OtpFragment: uid: ${auth.uid.toString()} phoneNumber: ${auth.currentUser?.phoneNumber.toString()}"
                        )
                        prefs.uid = auth.uid.toString()
                        prefs.mobileNumber = auth.currentUser?.phoneNumber.toString()
                        database!!.reference
                            .child("users")
                            .child(auth.uid.toString())
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        prefs.isLogin = true
                                        findNavController().navigate(OtpFragmentDirections.toNavMainFragment())
                                    } else {
                                        val bundle = bundleOf(
                                            "phoneNumber" to phoneNumber,
                                            "countryCode" to countryCode,
                                            "countryName" to countryName
                                        )
                                        findNavController().navigate(
                                            R.id.nav_set_profile_fragment,
                                            bundle
                                        )
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Error :${it.exception}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })

                    } else {
                        dialog.dismiss()
                        if (it.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            Log.d("otp", "Error: ${it.isSuccessful} ${it.exception}")
                            Toast.makeText(requireContext(), "The verification code is invalid", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                dialog.dismiss()
                Log.d("otp", "Exception: ${e.message}")
            }
        }

        binding.tvResendOtp.setOnClickListener {
            getOtp()
        }
    }

    private fun getOtp() {
        startResendTimer()
        binding.tvResendOtp.isVisible = true
        val options = PhoneAuthOptions.newBuilder().setPhoneNumber(countryCode + phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS).setActivity(requireActivity())
            .setCallbacks(callback).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun startResendTimer() {
        binding.tvResendOtp.isEnabled = false
        binding.tvResendOtp.alpha = 0.5f
        val timer = Timer()
        timer.scheduleAtFixedRate(timerTask {
            timerCount--
            runOnUiThread {
                binding.tvResendOtp.text = "Resend OTP in $timerCount seconds"
            }
            if (timerCount <= 0) {
                timerCount = 60
                timer.cancel()
                runOnUiThread {
                    binding.tvResendOtp.isEnabled = true
                    binding.tvResendOtp.alpha = 1f
                    binding.tvResendOtp.text = "Resend"
                }
            }
        }, 0, 1000)
    }

    private fun validateOtp(): Boolean {
        if (binding.otp1.text!!.isEmpty() || binding.otp2.text!!.isEmpty() || binding.otp3.text!!.isEmpty() || binding.otp4.text!!.isEmpty() || binding.otp5.text!!.isEmpty() || binding.otp6.text!!.isEmpty()) {
            binding.btnVerify.isEnabled = false
            binding.btnVerify.alpha = 0.5f
            return false
        }
        binding.btnVerify.isEnabled = true
        binding.btnVerify.alpha = 1f
        return true
    }

    /*private fun check() {
        runOnUiThread {
            if (binding.otp1.text!!.toString().isNotEmpty()) {
                if (binding.otp2.text!!.toString().isNotEmpty()) {
                    if (binding.otp3.text!!.toString().isNotEmpty()) {
                        if (binding.otp4.text!!.toString().isNotEmpty()) {
                            if (binding.otp5.text!!.toString().isNotEmpty()) {
                                if (binding.otp6.text!!.toString().isNotEmpty()) {

                                } else {
                                    binding.otp6.requestFocus()
                                }
                            } else {
                                binding.otp5.requestFocus()
                            }
                        } else {
                            binding.otp4.requestFocus()
                        }
                    } else {
                        binding.otp3.requestFocus()
                    }
                } else {
                    binding.otp2.requestFocus()
                }
            } else {
                binding.otp1.requestFocus()
            }
        }
    }*/

}
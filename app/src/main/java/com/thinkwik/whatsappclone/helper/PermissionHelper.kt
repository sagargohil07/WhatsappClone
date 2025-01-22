package com.thinkwik.whatsappclone.helper

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Semaphore

interface ResponsePermissionCallback {
    fun onResult(permissionResult: List<String>)
}

object KotlinPermissions {
    private const val TAG = "KotlinPermission"
    private val semaphore: Semaphore = Semaphore(1)

    @JvmStatic
    fun with(activity: AppCompatActivity): PermissionCore {
        return PermissionCore(activity)
    }

    class PermissionCore(activity: AppCompatActivity) {
        private val activityReference: WeakReference<AppCompatActivity> = WeakReference(activity)
        private var permissions: List<String> = ArrayList()
        private var acceptedCallback: WeakReference<ResponsePermissionCallback>? = null
        private var deniedCallback: WeakReference<ResponsePermissionCallback>? = null
        private var foreverDeniedCallback: WeakReference<ResponsePermissionCallback>? = null
        private val listener = object : PermissionFragment.PermissionListener {
            override fun onRequestPermissionsResult(
                acceptedPermissions: List<String>,
                refusedPermissions: List<String>,
                askAgainPermissions: List<String>
            ) {
                onReceivedPermissionResult(
                    acceptedPermissions,
                    refusedPermissions,
                    askAgainPermissions
                )
            }
        }

        internal fun onReceivedPermissionResult(
            acceptedPermissions: List<String>?,
            foreverDenied: List<String>?,
            denied: List<String>?
        ) {

            acceptedPermissions.whenNotNullNorEmpty {
                acceptedCallback?.get()?.onResult(it)
            }

            foreverDenied.whenNotNullNorEmpty {
                foreverDeniedCallback?.get()?.onResult(it)
            }

            denied.whenNotNullNorEmpty {
                deniedCallback?.get()?.onResult(it)
            }
        }

        fun permissions(vararg permission: String): PermissionCore {
            permissions = permission.toList()
            return this@PermissionCore
        }

        fun onAccepted(callback: (List<String>) -> Unit): PermissionCore {
            this.acceptedCallback = WeakReference(object : ResponsePermissionCallback {
                override fun onResult(permissionResult: List<String>) {
                    callback(permissionResult)
                }
            })
            return this@PermissionCore
        }

        fun onAccepted(callback: ResponsePermissionCallback): PermissionCore {
            this.acceptedCallback = WeakReference(callback)
            return this@PermissionCore
        }

        fun onDenied(callback: (List<String>) -> Unit): PermissionCore {
            this.deniedCallback = WeakReference(object : ResponsePermissionCallback {
                override fun onResult(permissionResult: List<String>) {
                    callback(permissionResult)
                }
            })
            return this@PermissionCore
        }

        fun onDenied(callback: ResponsePermissionCallback): PermissionCore {
            this.deniedCallback = WeakReference(callback)
            return this@PermissionCore
        }

        fun onForeverDenied(callback: (List<String>) -> Unit): PermissionCore {
            this.foreverDeniedCallback = WeakReference(object : ResponsePermissionCallback {
                override fun onResult(permissionResult: List<String>) {
                    callback(permissionResult)
                }
            })
            return this@PermissionCore
        }

        fun onForeverDenied(callback: ResponsePermissionCallback): PermissionCore {
            this.foreverDeniedCallback = WeakReference(callback)
            return this@PermissionCore
        }

        fun ask() {
            semaphore.acquire()
            val activity = activityReference.get()
            activity?.let { AppCompatActivity ->
                if (AppCompatActivity.isFinishing) {
                    semaphore.release()
                    return
                }

                //ne need < Android Marshmallow
                if (permissions.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    permissionAlreadyAccepted(activity, permissions)
                ) {
                    onAcceptedPermission(permissions)
                    semaphore.release()
                } else {
                    val oldFragment =
                        AppCompatActivity.supportFragmentManager.findFragmentByTag(TAG) as PermissionFragment?

                    oldFragment.ifNotNullOrElse({
                        it.addPermissionForRequest(listener, permissions)
                        semaphore.release()
                    }, {
                        val newFragment = PermissionFragment.newInstance()
                        newFragment.addPermissionForRequest(listener, permissions)
                        Try.withThreadIfFail({
                            AppCompatActivity.runOnUiThread {
                                AppCompatActivity.supportFragmentManager.beginTransaction()
                                    .add(newFragment, TAG)
                                    .commitNowAllowingStateLoss()
                                semaphore.release()
                            }
                        }, 3)

                    })
                }
            }
        }

        private fun onAcceptedPermission(permissions: List<String>) {
            onReceivedPermissionResult(permissions, null, null)
        }

        private fun permissionAlreadyAccepted(
            context: Context,
            permissions: List<String>
        ): Boolean {
            for (permission in permissions) {
                val permissionState = ContextCompat.checkSelfPermission(context, permission)
                if (permissionState == PackageManager.PERMISSION_DENIED) {
                    return false
                }
            }
            return true
        }
    }
}

class PermissionFragment : Fragment() {
    private val permissionQueue = ConcurrentLinkedQueue<PermissionHolder>()
    private var permissionsList: List<String> = ArrayList()
    private var listener: PermissionListener? = null
    private var waitingForReceive = false


    init {
        retainInstance = true
    }

    override fun onResume() {
        super.onResume()
        runQueuePermission()
    }

    private fun runQueuePermission() {
        if (waitingForReceive) return
        val poll = permissionQueue.poll()
        poll.ifNotNullOrElse({
            waitingForReceive = true
            this.listener = it.listener
            permissionsList = ArrayList(it.permissions)
            proceedPermissions()
        }, {
            if (!waitingForReceive) removeFragment()
        })
    }

    private fun proceedPermissions() {
        val perms = permissionsList.toTypedArray()
        requestPermissions(perms, REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE && permissions.isNotEmpty() && grantResults.isNotEmpty()) {

            val acceptedPermissions = ArrayList<String>()
            val askAgainPermissions = ArrayList<String>()
            val refusedPermissions = ArrayList<String>()

            for (i in permissions.indices) {
                val permissionName = permissions[i]
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    acceptedPermissions.add(permissionName)
                } else {
                    if (shouldShowRequestPermissionRationale(permissionName)) {
                        askAgainPermissions.add(permissionName)
                    } else {
                        refusedPermissions.add(permissionName)
                    }
                }
            }

            listener?.onRequestPermissionsResult(
                acceptedPermissions,
                refusedPermissions,
                askAgainPermissions
            )
        }
        waitingForReceive = false
    }

    private fun removeFragment() {
        try {
            fragmentManager?.beginTransaction()?.remove(this@PermissionFragment)
                ?.commitAllowingStateLoss()
        } catch (e: Exception) {
            Log.w(TAG, "Error while removing fragment")
        }
    }

    internal fun addPermissionForRequest(listener: PermissionListener, permission: List<String>) {
        permissionQueue.add(PermissionHolder(permission, listener))
    }

    internal interface PermissionListener {
        fun onRequestPermissionsResult(
            acceptedPermissions: List<String>,
            refusedPermissions: List<String>,
            askAgainPermissions: List<String>
        )
    }

    private data class PermissionHolder(
        val permissions: List<String>,
        val listener: PermissionListener
    )

    companion object {
        private const val REQUEST_CODE = 23000
        fun newInstance(): PermissionFragment {
            return PermissionFragment()
        }

        private const val TAG = "PermissionFragment"
    }
}

inline fun <E : Any, T : Collection<E>> T?.withNotNullNorEmpty(func: T.() -> Unit) {
    if (this != null && this.isNotEmpty()) {
        with(this) { func() }
    }
}

inline fun <E : Any, T : Collection<E>> T?.whenNotNullNorEmpty(func: (T) -> Unit) {
    if (this != null && this.isNotEmpty()) {
        func(this)
    }
}

inline fun <T> T?.notNull(f: (T) -> Unit) {
    if (this != null) {
        f(this)
    }
}

inline fun <T : Any, R> T?.ifNotNullOrElse(ifNotNullPath: (T) -> R, elsePath: () -> R) =
    let { if (it == null) elsePath() else ifNotNullPath(it) }

private object Try {

    private const val DELAY = 2000
    val TAG: String = Try::class.java.simpleName

    fun withUiThread(runnable: Runnable?, tryCount: Int) {
        runnable?.let {
            runTryCycle(it, tryCount)
        }
    }

    fun withThreadIfFail(action: () -> Unit, tryCount: Int) {
        action.let {
            try {
                Runnable(it).run()
            } catch (e: Exception) {
                Log.d(TAG, "Attempt in UI thread fail!")
                Thread { runTryCycle(Runnable(it), tryCount) }.start()
            }
        }
    }

    fun withThread(action: () -> Unit, tryCount: Int) {
        Thread { runTryCycle(Runnable(action), tryCount) }.start()
    }

    private fun runTryCycle(runnable: Runnable?, tryCount: Int) {
        runnable?.let {
            var tryCountLocal = tryCount
            var attempt = 1
            while (tryCountLocal > 0) {
                try {
                    Log.d(TAG, "Attempt $attempt")
                    it.run()
                } catch (e: Exception) {
                    Log.w(TAG, "Attempt $attempt fail!")
                    attempt++
                    tryCountLocal--
                    try {
                        Thread.sleep(DELAY.toLong())
                    } catch (e1: InterruptedException) {
                        e1.printStackTrace()
                    }

                    continue
                }

                tryCountLocal = 0
            }
        }
    }
}
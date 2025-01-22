package com.thinkwik.whatsappclone.module.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.firebase.auth.FirebaseAuth
import com.thinkwik.whatsappclone.R
import com.thinkwik.whatsappclone.databinding.ListItemReceiverAudioBinding
import com.thinkwik.whatsappclone.databinding.ListItemReceiverImageBinding
import com.thinkwik.whatsappclone.databinding.ListItemReceiverTextBinding
import com.thinkwik.whatsappclone.databinding.ListItemReceiverVideoBinding
import com.thinkwik.whatsappclone.databinding.ListItemSenderAudioBinding
import com.thinkwik.whatsappclone.databinding.ListItemSenderImageBinding
import com.thinkwik.whatsappclone.databinding.ListItemSenderTextBinding
import com.thinkwik.whatsappclone.databinding.ListItemSenderVideoBinding
import com.thinkwik.whatsappclone.module.model.MessageModel
import java.io.IOException

class ChatsAdapter(
    private val context: Context,
    private var messageList: ArrayList<MessageModel>,
    private var onVideoPlay: (url: String) -> Unit,
    private var onAudioPlay: (url: String, isPlay: Boolean) -> Unit,
    private var onMessageSelect: (MessageModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mediaPlayer = MediaPlayer()
    private var currentPlayingPosition = -1
    private val handler = Handler()
    private var isPlaying = false

    init {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setLooping(false)
        Log.d("rvlifecycle", "init: ")
    }

    companion object {
        const val SENDER_TEXT = 1
        const val SENDER_IMAGE = 2
        const val SENDER_AUDIO = 3
        const val SENDER_VIDEO = 4

        const val RECEIVER_TEXT = 5
        const val RECEIVER_IMAGE = 6
        const val RECEIVER_AUDIO = 7
        const val RECEIVER_VIDEO = 8
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SENDER_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_sender_text, parent, false)
                SenderTextViewHolder(view.rootView)
            }

            SENDER_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_sender_image, parent, false)
                SenderImageViewHolder(view.rootView)
            }

            SENDER_AUDIO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_sender_audio, parent, false)
                SenderAudioViewHolder(view.rootView)
            }

            SENDER_VIDEO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_sender_video, parent, false)
                SenderVideoViewHolder(view.rootView)
            }

            RECEIVER_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_receiver_text, parent, false)
                ReceiverTextViewHolder(view.rootView)
            }

            RECEIVER_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_receiver_image, parent, false)
                ReceiverImageViewHolder(view.rootView)
            }

            RECEIVER_AUDIO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_receiver_audio, parent, false)
                ReceiverAudioViewHolder(view.rootView)
            }

            RECEIVER_VIDEO -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_receiver_video, parent, false)
                ReceiverVideoViewHolder(view.rootView)
            }

            else -> {
                throw IllegalArgumentException("Unknown View Type")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = messageList[position]
        Log.d("rv", "onBindViewHolder: $position | model : ${model.toString()}")

        val gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    return true
                }

                @RequiresApi(Build.VERSION_CODES.M)
                override fun onLongPress(e: MotionEvent) {
                    onMessageSelect(model)
                    super.onLongPress(e)
                }
            })

        holder.itemView.setOnTouchListener(View.OnTouchListener { view, e ->
            gestureDetector.onTouchEvent(e)
            return@OnTouchListener true
        })

        when (holder) {
            is SenderTextViewHolder -> {
                holder.senderBinding.tvSenderMessage.text = model.message
            }

            is SenderImageViewHolder -> {
                Glide.with(context)
                    .load(model.file?.url)
                    .error(R.drawable.ic_broken_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.senderBinding.ivSenderImage)
            }

            is SenderAudioViewHolder -> {
                holder.senderBinding.tvSenderAudioName.text = model.file?.name

                holder.senderBinding.seekbarSenderAudio.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser && mediaPlayer != null) {
                            mediaPlayer.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        // Pause playback when the user starts seeking
                        mediaPlayer.pause()
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        // Resume playback when the user stops seeking
                        mediaPlayer.start()
                    }
                })

                holder.senderBinding.ivSenderAudio.setOnClickListener {
                    /* onAudioPlay(model.file?.url!!, true)*/
                    if (holder.getAdapterPosition() == currentPlayingPosition) {
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.pause()
                            holder.senderBinding.ivSenderAudio.setImageResource(R.drawable.ic_play)
                        } else {
                            holder.senderBinding.ivSenderAudio.setImageResource(R.drawable.ic_pause)
                            mediaPlayer.start()
                        }
                    } else {
                        currentPlayingPosition = holder.getAdapterPosition()
                        holder.senderBinding.ivSenderAudio.setImageResource(R.drawable.ic_pause)
                        playAudio(model.file?.url!!)
                    }
                    notifyDataSetChanged()
                }

                val updateSeekBar = object : Runnable {
                    override fun run() {
                        val currentPosition = mediaPlayer?.currentPosition ?: 0
                        holder.senderBinding.seekbarSenderAudio.progress = currentPosition
                        handler.postDelayed(this, 100)
                    }
                }

                if (holder.getAdapterPosition() == currentPlayingPosition && isPlaying) {
                    // Handle currently playing item
                    holder.senderBinding.seekbarSenderAudio.max = mediaPlayer.duration ?: 0
                    handler.removeCallbacks(updateSeekBar)
                    handler.postDelayed(updateSeekBar, 100)
                } else {
                    // Handle other items
                    holder.senderBinding.seekbarSenderAudio.max = 0
                }

            }

            is SenderVideoViewHolder -> {
                Glide.with(context)
                    .load(model.file?.url)
                    .error(R.drawable.ic_broken_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable?>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable?>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(holder.senderBinding.ivSenderVideoImage)
                holder.senderBinding.btnSenderVideoPlay.setOnClickListener {
                    onVideoPlay(model.file?.url!!)
                }
            }

            is ReceiverTextViewHolder -> {
                holder.receiverBinding.tvReceiverMessage.text = model.message
            }

            is ReceiverImageViewHolder -> {
                Log.d(
                    "chatAdapter",
                    "onBindViewHolder: ReceiverImageViewHolder : model.file?.url ${model.file?.url}"
                )
                Glide.with(context)
                    .load(model.file?.url)
                    .error(R.drawable.ic_broken_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable?>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d(
                                "chatAdapter",
                                "Glide listener: ReceiverImageViewHolder : onLoadFailed ${e} | ${e?.message} | ${e.toString()}"
                            )
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable?>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d(
                                "chatAdapter",
                                "Glide listener: ReceiverImageViewHolder : onResourceReady ${dataSource} | ${dataSource.toString()} "
                            )
                            return false
                        }
                    })
                    .into(holder.receiverBinding.ivReceiverImage)
            }

            is ReceiverAudioViewHolder -> {
                Log.d(
                    "chatAdapter",
                    "onBindViewHolder: ReceiverAudioViewHolder : model.file?.url ${model.file?.url}"
                )
                holder.receiverBinding.tvReceiverAudioName.text = model.file?.name

                holder.receiverBinding.seekbarReceiverAudio.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser && mediaPlayer != null) {
                            mediaPlayer.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        // Pause playback when the user starts seeking
                        mediaPlayer.pause()
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        // Resume playback when the user stops seeking
                        mediaPlayer.start()
                    }
                })

                holder.receiverBinding.ivReceiverAudio.setOnClickListener {
                    /* onAudioPlay(model.file?.url!!, true)*/
                    if (holder.getAdapterPosition() == currentPlayingPosition) {
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.pause()
                            holder.receiverBinding.ivReceiverAudio.setImageResource(R.drawable.ic_play)
                        } else {
                            holder.receiverBinding.ivReceiverAudio.setImageResource(R.drawable.ic_pause)
                            mediaPlayer.start()
                        }
                    } else {
                        currentPlayingPosition = holder.getAdapterPosition()
                        holder.receiverBinding.ivReceiverAudio.setImageResource(R.drawable.ic_pause)
                        playAudio(model.file?.url!!)
                    }
                    notifyDataSetChanged()
                }

                val updateSeekBar = object : Runnable {
                    override fun run() {
                        val currentPosition = mediaPlayer.currentPosition ?: 0
                        holder.receiverBinding.seekbarReceiverAudio.progress = currentPosition
                        handler.postDelayed(this, 100)
                    }
                }

                if (holder.getAdapterPosition() == currentPlayingPosition && isPlaying) {
                    // Handle currently playing item
                    holder.receiverBinding.seekbarReceiverAudio.max = mediaPlayer.duration ?: 0
                    handler.removeCallbacks(updateSeekBar)
                    handler.postDelayed(updateSeekBar, 100)
                } else {
                    // Handle other items
                    holder.receiverBinding.seekbarReceiverAudio.max = 0
                }
            }

            is ReceiverVideoViewHolder -> {
                Log.d(
                    "chatAdapter",
                    "onBindViewHolder: ReceiverVideoViewHolder : model.file?.url ${model.file?.url}"
                )
                Glide.with(context)
                    .load(model.file?.url)
                    .error(R.drawable.ic_broken_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.receiverBinding.ivReceiverVideoImage)
                holder.receiverBinding.btnReceiverVideoPlay.setOnClickListener {
                    onVideoPlay(model.file?.url!!)
                }
            }
        }

    }

    inner class SenderTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var senderBinding = ListItemSenderTextBinding.bind(view)
    }

    inner class SenderImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var senderBinding = ListItemSenderImageBinding.bind(view)
    }

    inner class SenderAudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var senderBinding = ListItemSenderAudioBinding.bind(view)
    }

    inner class SenderVideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var senderBinding = ListItemSenderVideoBinding.bind(view)
    }

    inner class ReceiverTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var receiverBinding = ListItemReceiverTextBinding.bind(view)
    }

    inner class ReceiverImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var receiverBinding = ListItemReceiverImageBinding.bind(view)
    }

    inner class ReceiverAudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var receiverBinding = ListItemReceiverAudioBinding.bind(view)
    }

    inner class ReceiverVideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var receiverBinding = ListItemReceiverVideoBinding.bind(view)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (FirebaseAuth.getInstance().uid == messageList[position].senderId) {
            when (messageList[position].messageType) {
                "text" -> {
                    SENDER_TEXT
                }

                "image" -> {
                    SENDER_IMAGE
                }

                "audio" -> {
                    SENDER_AUDIO
                }

                "video" -> {
                    SENDER_VIDEO
                }

                else -> {
                    throw IllegalArgumentException("unknown type")
                }
            }
        } else {
            when (messageList[position].messageType) {
                "text" -> {
                    RECEIVER_TEXT
                }

                "image" -> {
                    RECEIVER_IMAGE
                }

                "audio" -> {
                    RECEIVER_AUDIO
                }

                "video" -> {
                    RECEIVER_VIDEO
                }

                else -> {
                    throw IllegalArgumentException("unknown type")
                }
            }
        }
    }

    fun updateList(messageList: ArrayList<MessageModel>) {
        this.messageList = messageList
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPopupMenu(itemView: View, model: MessageModel) {
        val popupMenu = PopupMenu(context, itemView)
        popupMenu.inflate(R.menu.chat_option_menu)
        popupMenu.gravity = Gravity.END
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
            if (it.itemId == R.id.menuDelete) {

            } else {

            }
            true
        })
        popupMenu.show()
    }

    private fun playAudio(audioItem: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            mediaPlayer.reset()
        }

        try {
            mediaPlayer.setDataSource(audioItem)
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlaying = true
            notifyDataSetChanged()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            currentPlayingPosition = -1
            notifyDataSetChanged()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        Log.d("lifecycle", "onDetachedFromRecyclerView: ")
        if (mediaPlayer.isPlaying)
            mediaPlayer.stop()
        mediaPlayer.release()
    }

}


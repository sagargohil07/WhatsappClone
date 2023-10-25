package com.thinkwik.communimate.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.thinkwik.communimate.module.model.ChannelUpdatesModel
import com.thinkwik.communimate.module.model.ChannelsModel
import kotlin.math.log

class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "whatsapp_clone.db"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "UserStory"

        private const val COLUMN_ID = "id"
        private const val COLUMN_UID = "uid"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_USER_PROFILE = "userprofile"
        private const val COLUMN_URL = "url"
        private const val COLUMN_DATETIME = "dateTime"

        // New table columns for channels
        private const val TABLE_CHANNELS = "channels"
        private const val COLUMN_CHANNEL_ID = "id"
        private const val COLUMN_CHANNEL_NAME = "channelName"
        private const val COLUMN_CHANNEL_INFO = "channelInfo"
        private const val COLUMN_CHANNEL_IMAGE = "channelImage"
        private const val COLUMN_CHANNEL_ISFOLLWING = "isFollowing"
        private const val COLUMN_IS_MY_CHANNEL = "isMyChannel"

        // Columns for the channel updates table
        private const val TABLE_CHANNEL_UPDATES = "channelUpdates"
        private const val COLUMN_UPDATE_TYPE = "updateType"
        private const val COLUMN_CHANNEL_TEXT = "channelText"
        private const val COLUMN_DATE_TIME = "dateTime"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("sql", "onCreate: ")
        val createTableQuery = "CREATE TABLE $TABLE_NAME " +
                "($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_UID TEXT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_USER_PROFILE TEXT, " +
                "$COLUMN_URL TEXT, " +
                "$COLUMN_DATETIME TEXT)"
        db?.execSQL(createTableQuery)

        // Create Channels table
        val createChannelsTableQuery = "CREATE TABLE $TABLE_CHANNELS " +
                "($COLUMN_CHANNEL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_CHANNEL_NAME TEXT, " +
                "$COLUMN_CHANNEL_INFO TEXT, " +
                "$COLUMN_CHANNEL_IMAGE TEXT, " +
                "$COLUMN_CHANNEL_ISFOLLWING INTEGER, " +
                "$COLUMN_IS_MY_CHANNEL INTEGER)"
        db?.execSQL(createChannelsTableQuery)

        // Create the channelUpdates table
        val createChannelUpdatesTableQuery = "CREATE TABLE $TABLE_CHANNEL_UPDATES " +
                "($COLUMN_CHANNEL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_CHANNEL_NAME TEXT, " +
                "$COLUMN_UPDATE_TYPE TEXT, " +
                "$COLUMN_CHANNEL_TEXT TEXT, " +
                "$COLUMN_URL TEXT, " +
                "$COLUMN_DATE_TIME TEXT)"
        db?.execSQL(createChannelUpdatesTableQuery)

        // Insert predefined data into Channels table if it's empty
        if (!hasDataInChannelsTable(db)) {
            insertPredefinedChannelData(db)
            insertPredefinedChannelUpdatesData(db)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d("sql", "onUpgrade: ")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CHANNELS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CHANNEL_UPDATES")
        onCreate(db)
    }

    private fun hasDataInChannelsTable(db: SQLiteDatabase?): Boolean {
        val cursor = db?.rawQuery("SELECT * FROM $TABLE_CHANNELS", null)
        val hasData = (cursor?.count ?: 0) > 0
        cursor?.close()
        return hasData
    }

    private fun insertPredefinedChannelData(db: SQLiteDatabase?) {
        val predefinedData = listOf(
            ChannelsModel(
                channelName = "Whatsapp",
                channelInfo = "30M followers",
                channelImage = "https://store-images.s-microsoft.com/image/apps.8985.13655054093851568.1c669dab-3716-40f6-9b59-de7483397c3a.8b1af40f-2a98-4a00-98cd-94e485a04427"
            ),
            ChannelsModel(
                channelName = "kalyani priyadarshan",
                channelInfo = "21.8M followers",
                channelImage = "https://justformoviefreaks.in/wp-content/uploads/2023/02/Best-Movies-of-Kalyani-Priyadarshan.jpg"
            ),
            ChannelsModel(
                channelName = "Vijay Thalapathy",
                channelInfo = "11.3M followers",
                channelImage = "https://c.ndtvimg.com/2023-09/9jc1kvn8_alia-_625x300_18_September_23.jpg"
            ),
            ChannelsModel(
                channelName = "Allu arjun",
                channelInfo = "13.2M followers",
                channelImage = "https://www.newstap.in/h-upload/2023/07/25/1524656-sakshi-fan-of-aa.webp"
            ),
            ChannelsModel(
                channelName = "Nani",
                channelInfo = "10M followers",
                channelImage = "https://www.telugu360.com/wp-content/uploads/2023/09/Nani-1.jpg"
            ),
            ChannelsModel(
                channelName = "Vijay Sethupathi",
                channelInfo = "15M followers",
                channelImage = "https://img.etimg.com/thumb/width-640,height-480,imgsize-56970,resizemode-75,msid-96218309/news/new-updates/vijay-sethupathis-drastic-weight-loss-stuns-fans-see-pics.jpg"
            ),
            ChannelsModel(
                channelName = "Rajinikanth",
                channelInfo = "20M followers",
                channelImage = "https://static.toiimg.com/thumb/msid-93702520,width-1280,resizemode-4/93702520.jpg"
            ),
            ChannelsModel(
                channelName = "Kamal haasan",
                channelInfo = "5M followers",
                channelImage = "https://images.news18.com/ibnlive/uploads/2022/09/kamal-haasan-vikram-1-16623782714x3.jpg"
            ),
            ChannelsModel(
                channelName = "Rashmika mandanna",
                channelInfo = "25M followers",
                channelImage = "https://images.cinemaexpress.com/uploads/user/imagelibrary/2018/9/3/original/Rashmika-Mandanna-.jpg"
            ),

            ChannelsModel(
                channelName = "Brahmanandam",
                channelInfo = "9.99M followers",
                channelImage = "https://w0.peakpx.com/wallpaper/865/15/HD-wallpaper-brahmi-actor-brahmanandam-comedy-telugu-tollywood.jpg"
            ),
            /* ChannelsModel(channelName = "", channelInfo = "4.5M followers", channelImage = ""),
             ChannelsModel(channelName = "", channelInfo = "4.5M followers", channelImage = ""),*/

            ChannelsModel(
                channelName = "mahesh babu",
                channelInfo = "4.5M followers",
                channelImage = "https://www.gethucinema.com/wp-content/uploads/2017/05/mahesh-babu-hd-Gethucinema.jpeg"
            ),
            ChannelsModel(
                channelName = "Prabhas",
                channelInfo = "18.6M followers",
                channelImage = "https://cdn.tollywood.net/wp-content/uploads/2020/01/All-Set-for-Unleashing-Prabhas-Saaho-Mania-in-Japan.jpg"
            ),
            ChannelsModel(
                channelName = "Suriya Sivakumar",
                channelInfo = "15.5M followers",
                channelImage = "https://w0.peakpx.com/wallpaper/1001/145/HD-wallpaper-rolex-rolex-surya.jpg"
            ),
            ChannelsModel(
                channelName = "Dhanush",
                channelInfo = "14.8M followers",
                channelImage = "https://psmind.in/wp-content/uploads/2023/02/Dhanush-Net-Worth-2023-Biography-Age-Wife.jpg"
            ),
            ChannelsModel(
                channelName = "vijay devarakonda",
                channelInfo = "7.7M followers",
                channelImage = "https://www.hdwallpapers.in/download/vijay_devarakonda_is_wearing_sandal_dress_and_cap_hd_vijay_devarakonda-1920x1080.jpg"
            ),
            ChannelsModel(
                channelName = "N. T. Rama Rao Jr.",
                channelInfo = "16.6M followers",
                channelImage = "https://w0.peakpx.com/wallpaper/916/917/HD-wallpaper-jr-ntr-smile-ntr.jpg"
            ),
            ChannelsModel(
                channelName = "Ram charan",
                channelInfo = "17.8M followers",
                channelImage = "https://igimages.gumlet.io/telugu/home/charan25052023_c.jpg?w=376&dpr=2.6"
            ),
            ChannelsModel(
                channelName = "Vikram",
                channelInfo = "11.2M followers",
                channelImage = "https://www.mrdustbin.com/en/wp-content/uploads/2021/03/Vikram.jpg"
            ),
            ChannelsModel(
                channelName = "Ajith Kumar",
                channelInfo = "6.7M followers",
                channelImage = "https://images.hindustantimes.com/rf/image_size_640x362/HT/p2/2016/05/04/Pictures/_7812824e-11dd-11e6-a855-9958039a7c6d.jpg"
            ),
            ChannelsModel(
                channelName = "Nagarjuna Akkineni",
                channelInfo = "14.5M followers",
                channelImage = "https://i.pinimg.com/1200x/16/9a/2f/169a2f75f5b983582857a222c66ddd24.jpg"
            ),
            ChannelsModel(
                channelName = "Naga Chaitanya Akkineni",
                channelInfo = "18.9M followers",
                channelImage = "https://www.themoviedb.org/t/p/w500/pcGTJvzIC3JzcfNVzdA6IHo3e8k.jpg"
            ),
            ChannelsModel(
                channelName = "Keerthy Suresh",
                channelInfo = "16M followers",
                channelImage = "https://www.hiscraves.com/blog/wp-content/uploads/2023/06/Keerthy-Suresh.jpg"
            ),
            ChannelsModel(
                channelName = "Pooja Hegde",
                channelInfo = "17M followers",
                channelImage = "https://www.hiscraves.com/blog/wp-content/uploads/2023/06/Pooja-Hegde.jpg"
            ),
            ChannelsModel(
                channelName = "Nayanthara",
                channelInfo = "18M followers",
                channelImage = "https://images.indianexpress.com/2023/10/nayanthara-11102023.jpg"
            ),
            ChannelsModel(
                channelName = "Sai Pallavi",
                channelInfo = "12M followers",
                channelImage = "https://w0.peakpx.com/wallpaper/440/580/HD-wallpaper-sai-pallavi-cute-face-smiling.jpg"
            ),
            ChannelsModel(
                channelName = "Anupama Parameswaram",
                channelInfo = "13M followers",
                channelImage = "https://4kwallpapers.com/images/wallpapers/anupama-parameswaran-indian-actress-south-actress-beautiful-3440x1440-7642.jpg"
            ),
            ChannelsModel(
                channelName = "Samantha",
                channelInfo = "25M followers",
                channelImage = "https://cdn.tollywood.net/wp-content/uploads/2022/07/Big-trouble-Samantha-Instagram-Hacked.jpg"
            ),
        )
        for (channelInfo in predefinedData) {
            val values = ContentValues()
            values.put(COLUMN_CHANNEL_NAME, channelInfo.channelName)
            values.put(COLUMN_CHANNEL_INFO, channelInfo.channelInfo)
            values.put(COLUMN_CHANNEL_IMAGE, channelInfo.channelImage)
            values.put(COLUMN_CHANNEL_ISFOLLWING, channelInfo.isFollowing)
            values.put(COLUMN_IS_MY_CHANNEL, channelInfo.isMyChannel)
            db?.insert(TABLE_CHANNELS, null, values)
        }
    }

    private fun insertPredefinedChannelUpdatesData(db: SQLiteDatabase?) {
        val predefinedData = listOf(
            ChannelUpdatesModel(
                channelName = "kalyani priyadarshan",
                channelText = "",
                updateType = "image",
                url = "https://m.media-amazon.com/images/M/MV5BOTNmOWFhNDYtNzAzZC00NzE3LTlmYjMtOTcyMzc1N2M3MWU2XkEyXkFqcGdeQXVyMzYxOTQ3MDg@._V1_.jpg",
                dateTime = ""
            ),
            ChannelUpdatesModel(
                channelName = "kalyani priyadarshan",
                channelText = "Hello Taqdeer release on  23 June 2018",
                updateType = "image",
                url = "https://i.ytimg.com/vi/Lxx2CynCe3w/maxresdefault.jpg",
                dateTime = ""
            ),
            ChannelUpdatesModel(
                channelName = "kalyani priyadarshan",
                channelText = "moview is release not go and watch",
                updateType = "text",
                url = "",
                dateTime = ""
            ),
            ChannelUpdatesModel(
                channelName = "kalyani priyadarshan",
                channelText = "Leo",
                updateType = "image",
                url = "https://timesofindia.indiatimes.com/photo/msid-104203851,imgsize-211866.cms",
                dateTime = ""
            ),
            ChannelUpdatesModel(
                channelName = "kalyani priyadarshan",
                channelText = "",
                updateType = "image",
                url = "https://chennaivision.com/wp-content/uploads/2023/07/MV5BMWZlMTY0YjItMDczMC00Mzk1LWEyMDgtM2M5YWJmYTU4Y2I2XkEyXkFqcGdeQXVyMTEzNzg0Mjkx._V1_.jpg",
                dateTime = ""
            ),
        )
        for (model in predefinedData) {
            val values = ContentValues()
            values.put(COLUMN_CHANNEL_NAME, model.channelName)
            values.put(COLUMN_UPDATE_TYPE, model.updateType)
            values.put(COLUMN_CHANNEL_TEXT, model.channelText)
            values.put(COLUMN_URL, model.url)
            values.put(COLUMN_DATE_TIME, model.dateTime)
            db?.insert(TABLE_CHANNEL_UPDATES, null, values)
        }
    }

    fun insertChannel(
        name: String,
        url: String,
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CHANNEL_NAME, name)
        values.put(COLUMN_CHANNEL_INFO, "0 followers")
        values.put(COLUMN_CHANNEL_IMAGE, url)
        values.put(COLUMN_CHANNEL_ISFOLLWING, 1)
        values.put(COLUMN_IS_MY_CHANNEL, 1)
        db?.insert(TABLE_CHANNELS, null, values)
        return true
    }

    fun insertChannelMessage(
        channelName: String,
        updateType: String = "",
        text: String ="",
        url : String ="",
        dateTime: String="",
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CHANNEL_NAME, channelName)
        values.put(COLUMN_UPDATE_TYPE, updateType)
        values.put(COLUMN_CHANNEL_TEXT, text)
        values.put(COLUMN_URL, url)
        values.put(COLUMN_DATE_TIME, dateTime)
        db?.insert(TABLE_CHANNEL_UPDATES, null, values)
        return true
    }


    @SuppressLint("Range")
    fun getAllChannels(): List<ChannelsModel> {
        val channels = mutableListOf<ChannelsModel>()
        val db = readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.query(
                TABLE_CHANNELS,
                arrayOf(
                    COLUMN_CHANNEL_NAME,
                    COLUMN_CHANNEL_INFO,
                    COLUMN_CHANNEL_IMAGE,
                    COLUMN_CHANNEL_ISFOLLWING,
                    COLUMN_IS_MY_CHANNEL,
                ),
                null,
                null,
                null,
                null,
                null
            )

            cursor?.let {
                if (cursor.moveToFirst()) {
                    do {
                        val name = cursor.getString(cursor.getColumnIndex(COLUMN_CHANNEL_NAME))
                        val info = cursor.getString(cursor.getColumnIndex(COLUMN_CHANNEL_INFO))
                        val imageUrl = cursor.getString(cursor.getColumnIndex(COLUMN_CHANNEL_IMAGE))
                        val isFollowing =
                            cursor.getInt(cursor.getColumnIndex(COLUMN_CHANNEL_ISFOLLWING))
                        val isMyChannel = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_MY_CHANNEL))
                        val channelInfo =
                            ChannelsModel(name, info, imageUrl, isFollowing, isMyChannel)
                        channels.add(channelInfo)
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: SQLException) {
            // Handle any exceptions here
            Log.d("DBHelper", "getAllChannels: ${e.message}")
        }

        return channels
    }

    @SuppressLint("Range")
    fun getAllChannelUpdates(): List<ChannelUpdatesModel> {
        val channelUpdates = mutableListOf<ChannelUpdatesModel>()
        val db = readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.query(
                TABLE_CHANNEL_UPDATES,
                arrayOf(
                    COLUMN_CHANNEL_NAME,
                    COLUMN_UPDATE_TYPE,
                    COLUMN_CHANNEL_TEXT,
                    COLUMN_URL,
                    COLUMN_DATE_TIME
                ),
                null,
                null,
                null,
                null,
                null
            )

            cursor?.let {
                if (cursor.moveToFirst()) {
                    do {
                        val channelName =
                            cursor.getString(cursor.getColumnIndex(COLUMN_CHANNEL_NAME))
                        val updateType = cursor.getString(cursor.getColumnIndex(COLUMN_UPDATE_TYPE))
                        val channelText =
                            cursor.getString(cursor.getColumnIndex(COLUMN_CHANNEL_TEXT))
                        val url = cursor.getString(cursor.getColumnIndex(COLUMN_URL))
                        val dateTime = cursor.getString(cursor.getColumnIndex(COLUMN_DATE_TIME))

                        val channelUpdate = ChannelUpdatesModel(channelName, updateType, channelText, url, dateTime)
                        channelUpdates.add(channelUpdate)
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: SQLException) {
            // Handle any exceptions here
        }

        return channelUpdates
    }

    fun updateChannelIsFollowing(channelName: String, isFollowing: Boolean): ChannelsModel? {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CHANNEL_ISFOLLWING, if (isFollowing) 1 else 0)
        // Update the value based on the channelName
        db.update(
            TABLE_CHANNELS,
            values,
            "$COLUMN_CHANNEL_NAME = ?",
            arrayOf(channelName)
        )
        return getUserDetailsByChannelName(channelName)
    }

    fun deleteChannel(channelName: String): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_CHANNELS,
            "$COLUMN_CHANNEL_NAME = ?",
            arrayOf(channelName)
        )
    }

    @SuppressLint("Range")
    fun getUserDetailsByChannelName(channelName: String): ChannelsModel? {
        val db = readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.query(
                TABLE_CHANNELS,
                arrayOf(
                    COLUMN_CHANNEL_ID,
                    COLUMN_CHANNEL_NAME,
                    COLUMN_CHANNEL_INFO,
                    COLUMN_CHANNEL_IMAGE,
                    COLUMN_CHANNEL_ISFOLLWING,
                    COLUMN_IS_MY_CHANNEL
                ),
                "$COLUMN_CHANNEL_NAME = ?",
                arrayOf(channelName),
                null,
                null,
                null
            )

            cursor?.let {
                if (cursor.moveToFirst()) {
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_CHANNEL_ID))
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_CHANNEL_NAME))
                    val info = cursor.getString(cursor.getColumnIndex(COLUMN_CHANNEL_INFO))
                    val image = cursor.getString(cursor.getColumnIndex(COLUMN_CHANNEL_IMAGE))
                    val isFollowing =
                        cursor.getInt(cursor.getColumnIndex(COLUMN_CHANNEL_ISFOLLWING))
                    val isMyChannel = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_MY_CHANNEL))
                    return ChannelsModel(name, info, image, isFollowing, isMyChannel)
                }
                cursor.close()
            }
        } catch (e: SQLException) {
            // Handle any exceptions here
            Log.d("DBHelper", "getUserDetailsByChannelName: ${e.message}")
        }

        return null
    }

    fun insertUserStory(
        uid: String,
        name: String,
        userProfile: String,
        url: String,
        dateTime: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_UID, uid)
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_USER_PROFILE, userProfile)
        values.put(COLUMN_URL, url)
        values.put(COLUMN_DATETIME, dateTime)
        db.insert(TABLE_NAME, null, values)
        return true
    }

    @SuppressLint("Range")
    fun getUserStoriesByUid(uid: String): List<UserStory> {
        val userStories = mutableListOf<UserStory>()
        val db = readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.query(
                TABLE_NAME,
                arrayOf(
                    COLUMN_ID,
                    COLUMN_UID,
                    COLUMN_NAME,
                    COLUMN_USER_PROFILE,
                    COLUMN_URL,
                    COLUMN_DATETIME
                ),
                "$COLUMN_UID = ?",
                arrayOf(uid),
                null,
                null,
                null
            )

            cursor?.let {
                if (cursor.moveToFirst()) {
                    do {
                        val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                        val uid = cursor.getString(cursor.getColumnIndex(COLUMN_UID))
                        val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                        val userProfile =
                            cursor.getString(cursor.getColumnIndex(COLUMN_USER_PROFILE))
                        val url = cursor.getString(cursor.getColumnIndex(COLUMN_URL))
                        val dateTime = cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME))
                        val userStory = UserStory(uid, name, userProfile, url, dateTime)
                        userStories.add(userStory)
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: SQLException) {
            // Handle any exceptions here
            Log.d("DBHelper", "getUserStoriesByUid: ${e.message}")
        }

        return userStories
    }

    @SuppressLint("Range")
    fun getAllUserStories(): List<UserStory> {
        val userStories = mutableListOf<UserStory>()
        val db = readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.query(
                TABLE_NAME,
                arrayOf(
                    COLUMN_ID,
                    COLUMN_UID,
                    COLUMN_NAME,
                    COLUMN_USER_PROFILE,
                    COLUMN_URL,
                    COLUMN_DATETIME
                ),
                null,
                null,
                null,
                null,
                null
            )

            cursor?.let {
                if (cursor.moveToFirst()) {
                    do {
                        val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                        val uid = cursor.getString(cursor.getColumnIndex(COLUMN_UID))
                        val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                        val userProfile =
                            cursor.getString(cursor.getColumnIndex(COLUMN_USER_PROFILE))
                        val url = cursor.getString(cursor.getColumnIndex(COLUMN_URL))
                        val dateTime = cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME))
                        val userStory = UserStory(uid, name, userProfile, url, dateTime)
                        userStories.add(userStory)
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: SQLException) {
            // Handle any exceptions here
            Log.d("db", "getAllUserStories: ${e.message} ${e.toString()}")
        }

        return userStories
    }

    fun clearAllUserStories() {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }

    fun enterOtherUserStory() {
        insertUserStory(
            "ax5RFo5lBRPhRIPzT9NIypYmr0a2",
            "Ashvini\uD83E\uDD29",
            "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1695962375157?alt=media&token=39170113-1458-42be-97b7-da53e3099eee",
            "https://wallpapers-clan.com/wp-content/uploads/2022/09/stitch-drawing-blue-wallpaper-scaled.jpg",
            "yesterday , 11:15 am"
        )
        insertUserStory(
            "ax5RFo5lBRPhRIPzT9NIypYmr0a2",
            "Ashvini\uD83E\uDD29",
            "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1695962375157?alt=media&token=39170113-1458-42be-97b7-da53e3099eee",
            "https://e1.pxfuel.com/desktop-wallpaper/472/325/desktop-wallpaper-nature-for-android-group-3d-nature-for-mobile.jpg",
            "yesterday , 11:15 am"
        )
        insertUserStory(
            "ax5RFo5lBRPhRIPzT9NIypYmr0a2",
            "Ashvini\uD83E\uDD29",
            "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1695962375157?alt=media&token=39170113-1458-42be-97b7-da53e3099eee",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQFQqwl_FMTU5x5mOToB5AB6lfaWPnvv3UaCxR3CYI_GKdujVsEHcaD7HnMxh0Xt1J_Hhc&usqp=CAU",
            "yesterday , 11:15 am"
        )
        insertUserStory(
            "ax5RFo5lBRPhRIPzT9NIypYmr0a2",
            "Ashvini\uD83E\uDD29",
            "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1695962375157?alt=media&token=39170113-1458-42be-97b7-da53e3099eee",
            "https://e0.pxfuel.com/wallpapers/290/822/desktop-wallpaper-stitch.jpg",
            "yesterday , 11:15 am"
        )

        insertUserStory(
            "S869VS45jbPolBemUvX79IIRRfI3",
            "viky",
            "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1694427285333?alt=media&token=911413df-ccd3-4596-8d5d-c35a28f10e13",
            "https://1.bp.blogspot.com/-AM8M5lpueyI/Xtsc6TthL7I/AAAAAAAARZ0/4HLH1DdPZJAffbOCGpvBKa6YZ9i1ZoKOgCK4BGAsYHg/w1600/black-backgroun-wallpaper.jfif",
            "yesterday , 1:22 pm"
        )
        insertUserStory(
            "S869VS45jbPolBemUvX79IIRRfI3",
            "viky",
            "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1694427285333?alt=media&token=911413df-ccd3-4596-8d5d-c35a28f10e13",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT9UJ3ZHVHfUDKMWAgqRO77FvnIKQVFSMBv1XGK6XnPRCcxvmMH2hLpdtLPpQ0a5ZhFUjw&usqp=CAU",
            "yesterday , 1:23 pm"
        )
        insertUserStory(
            "S869VS45jbPolBemUvX79IIRRfI3",
            "viky",
            "https://firebasestorage.googleapis.com/v0/b/whatapp-45b1a.appspot.com/o/Profile%2F1694427285333?alt=media&token=911413df-ccd3-4596-8d5d-c35a28f10e13",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTnP0E3H1UON-bsNNGH-FQaBPsn3krl814wnLy3eI7JbqRDsVaoyk36UlpgDrvvgedWH0Y&usqp=CAU",
            "yesterday , 1:24 pm"
        )
    }

    @SuppressLint("Range")
    fun getDistinctUserIds(): List<String> {
        val userIds = mutableListOf<String>()
        val db = readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.rawQuery("SELECT DISTINCT $COLUMN_UID FROM $TABLE_NAME", null)
            cursor?.let {
                if (cursor.moveToFirst()) {
                    do {
                        val uid = cursor.getString(cursor.getColumnIndex(COLUMN_UID))
                        userIds.add(uid)
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: SQLException) {
            // Handle any exceptions here
            Log.d("DBHelper", "getDistinctUserIds: ${e.message}")
        }

        return userIds
    }

    fun hasRecordsExceptUid(uid: String): Boolean {
        val db = readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.query(
                TABLE_NAME,
                arrayOf(COLUMN_ID),
                "$COLUMN_UID != ?",
                arrayOf(uid),
                null,
                null,
                null
            )

            cursor?.let {
                val hasRecords = cursor.count > 0
                cursor.close()
                return hasRecords
            }
        } catch (e: SQLException) {
            // Handle any exceptions here
            Log.d("DBHelper", "hasRecordsExceptUid: ${e.message}")
        }

        return false
    }

    @SuppressLint("Range")
    fun getDistinctUserDetails(): List<UserStory> {
        val userDetails = mutableListOf<UserStory>()
        val db = readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(
                "SELECT DISTINCT $COLUMN_UID, $COLUMN_NAME, $COLUMN_USER_PROFILE FROM $TABLE_NAME",
                null
            )

            cursor?.let {
                if (cursor.moveToFirst()) {
                    do {
                        val uid = cursor.getString(cursor.getColumnIndex(COLUMN_UID))
                        val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                        val userProfile =
                            cursor.getString(cursor.getColumnIndex(COLUMN_USER_PROFILE))
                        val userDetail = UserStory(uid, name, userProfile, "", "")
                        userDetails.add(userDetail)
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        } catch (e: SQLException) {
            // Handle any exceptions here
            Log.d("DBHelper", "getDistinctUserDetails: ${e.message}")
        }

        return userDetails
    }

}
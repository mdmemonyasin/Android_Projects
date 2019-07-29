package com.example.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.example.echo.CurrentSongHelper
import com.example.echo.R
import com.example.echo.Songs
import com.example.princ.echo.databases.EchoDatabase
import java.util.*
import java.util.concurrent.TimeUnit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class SongPlayingFragment : Fragment() {


    object Statified {
        var myActivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playpauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var seekBar: SeekBar? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var shuffleImageButton: ImageButton? = null
        var currentSongHelper: CurrentSongHelper? = null
        var currentPosition: Int? = 0
        var fetchSongs: ArrayList<Songs>? = null
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null
        var fab: ImageButton? = null
        var MY_PREFS_NAME = "ShakeFeature"

        var mSensorManager: SensorManager? = null
        var mSensorListner: SensorEventListener? = null

        var favoriteContent: EchoDatabase? = null

        var updateSongtime = object : Runnable {
            override fun run() {
                val getCurrent = mediaPlayer?.currentPosition
                startTimeText?.setText(
                    String.format(
                        "%d:%d", TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                        TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long) - TimeUnit.MILLISECONDS.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long)
                        )
                    )
                )
                Handler().postDelayed(this, 1000)
            }

        }
    }

    object Staticated {
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        fun onSongComplete() {
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                playNext("playNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying = true
            } else {
                if (Statified.currentSongHelper?.isLoop as Boolean) {
                    Statified.currentSongHelper?.isPlaying = true
                    val nextSong = Statified.fetchSongs?.get(Statified.currentPosition as Int)
                    Statified.currentSongHelper?.songPath = nextSong?.songData
                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songArtists = nextSong?.artist
                    Statified.currentSongHelper?.songId = nextSong?.songID as Long
                    Statified.currentSongHelper?.currentPosition = Statified.currentPosition
                    updateTextViews(
                        Statified.currentSongHelper?.songTitle as String,
                        Statified.currentSongHelper?.songArtists as String
                    )


                    Statified.mediaPlayer?.reset()
                    try {
                        Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(nextSong?.songData))
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                        processInformation(Statified.mediaPlayer as MediaPlayer)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                } else {
                    playNext("playNextNormal")
                    Statified.currentSongHelper?.isPlaying = true
                }
            }
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity as Context,
                        R.drawable.favorite_on
                    )
                )

            } else {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity as Context,
                        R.drawable.favorite_off
                    )
                )
            }
        }


        fun updateTextViews(songTitle: String, songArtist: String) {
            var songTitleUpdated = songTitle
            if (songTitle.equals("<unknown>", true)) {
                songTitleUpdated = "unknown"
            }
            var songArtistUpdated = songArtist
            if (songArtist.equals("<unknown>", true)) {
                songArtistUpdated = "unknown"
            }
            Statified.songTitleView?.setText(songTitleUpdated)
            Statified.songArtistView?.setText(songArtistUpdated)

        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer?.duration
            val starttime = mediaPlayer?.currentPosition
            Statified.seekBar?.max = finalTime

            Statified.startTimeText?.setText(
                String.format(
                    "%d:%d", TimeUnit.MILLISECONDS.toMinutes(starttime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(starttime?.toLong() as Long) - TimeUnit.MILLISECONDS.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            starttime?.toLong() as Long
                        )
                    )
                )
            )
            Statified.endTimeText?.setText(
                String.format(
                    "%d:%d", TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime?.toLong() as Long) - TimeUnit.MILLISECONDS.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            finalTime?.toLong() as Long
                        )
                    )
                )
            )
            Statified.seekBar?.setProgress(starttime)
            Handler().postDelayed(Statified.updateSongtime, 1000)
        }

        fun playNext(check: String) {
            if (check.equals("playNextNormal", true)) {
                Statified.currentPosition = Statified.currentPosition?.plus(1)
            } else if (check.equals("playNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
                Statified.currentPosition = randomPosition

            }
            if (Statified.currentPosition == Statified.fetchSongs?.size) {
                Statified.currentPosition = 0
            }
            Statified.currentSongHelper?.isLoop = false

            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition as Int)
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songArtists = nextSong?.artist
            Statified.currentSongHelper?.songId = nextSong?.songID as Long
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition
            updateTextViews(
                Statified.currentSongHelper?.songTitle as String,
                Statified.currentSongHelper?.songArtists as String
            )


            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(
                    Statified.myActivity,
                    Uri.parse(Statified.currentSongHelper?.songPath)
                )
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(Statified.mediaPlayer as MediaPlayer)

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity as Context,
                        R.drawable.favorite_on
                    )
                )

            } else {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity as Context,
                        R.drawable.favorite_off
                    )
                )
            }

        }


    }

    var mAccelaration: Float = 0f
    var mAccelarationCurrent: Float = 0f
    var mAccelarationLast: Float = 0f


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        activity?.title = "Now Playing"
        var view: View = inflater.inflate(R.layout.fragment_song_playing, container, false)
        Statified.seekBar = view?.findViewById(R.id.seekBar)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.playpauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.previousImageButton = view?.findViewById(R.id.previousButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.glView = view?.findViewById(R.id.visualizer_view)
        Statified.fab = view?.findViewById(R.id.favoriteIcon)
        setHasOptionsMenu(true)

        Statified.fab?.alpha = 0.8f

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glView as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(
            Statified.mSensorListner,
            Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        Statified.audioVisualization?.onPause()
        Statified.mSensorManager?.unregisterListener(Statified.mSensorListner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Statified.audioVisualization?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelaration = 0.0f
        mAccelarationLast = SensorManager.GRAVITY_EARTH
        mAccelarationCurrent = SensorManager.GRAVITY_EARTH
        bindShakeListner()

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_redirect -> {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Statified.favoriteContent = EchoDatabase(Statified.myActivity)
        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var _songID: Long? = 0
        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            _songID = arguments?.getInt("SongId")?.toLong()
            Statified.currentPosition = arguments?.getInt("songPosition")
            Statified.fetchSongs = arguments?.getParcelableArrayList("songData")


            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.songArtists = _songArtist
            Statified.currentSongHelper?.songId = _songID
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition

            Staticated.updateTextViews(
                Statified.currentSongHelper?.songTitle as String,
                Statified.currentSongHelper?.songArtists as String
            )


        } catch (e: Exception) {
            e.printStackTrace()

        }

        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        if (fromFavBottomBar != null) {
            Statified.mediaPlayer = FavoriteFragment.Statified.mediaPlayer
        } else {
            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(path))
                Statified.mediaPlayer?.prepare()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()

            }
            Statified.mediaPlayer?.start()
        }


        Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }

        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.onSongComplete()
        }
        clickHandler()

        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context, 0)
        Statified.audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle =
            Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)

        } else {
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            Statified.currentSongHelper?.isLoop = true
            Statified.currentSongHelper?.isShuffle = false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        } else {
            Statified.currentSongHelper?.isLoop = false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }

        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(
                ContextCompat.getDrawable(
                    Statified.myActivity as Context,
                    R.drawable.favorite_on
                )
            )

        } else {
            Statified.fab?.setImageDrawable(
                ContextCompat.getDrawable(
                    Statified.myActivity as Context,
                    R.drawable.favorite_off
                )
            )
        }

    }

    fun clickHandler() {
        Statified.fab?.setOnClickListener {
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity as Context,
                        R.drawable.favorite_off
                    )
                )
                Statified.favoriteContent?.deleteFavourite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()

            } else {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity as Context,
                        R.drawable.favorite_on
                    )
                )
                Statified.favoriteContent?.storeAsFavorite(
                    Statified.currentSongHelper?.songId?.toInt(),
                    Statified.currentSongHelper?.songArtists,
                    Statified.currentSongHelper?.songTitle,
                    Statified.currentSongHelper?.songPath
                )
                Toast.makeText(Statified.myActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
            }
        }
        Statified.shuffleImageButton?.setOnClickListener({
            var editorShuffle =
                Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop =
                Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()


            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }

        })
        Statified.nextImageButton?.setOnClickListener({
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Staticated.playNext("playNextLikeNormalShuffle")
            } else {
                Staticated.playNext("playNextNormal")
            }

        })
        Statified.previousImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()

        })
        Statified.loopImageButton?.setOnClickListener({
            var editorShuffle =
                Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop =
                Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            }


        })
        Statified.playpauseImageButton?.setOnClickListener({
            if (Statified.mediaPlayer?.isPlaying as Boolean) {
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                Statified.mediaPlayer?.start()
                Statified.currentSongHelper?.isPlaying = true
                Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }

        })
    }


    fun playPrevious() {
        Statified.currentPosition = Statified.currentPosition?.minus(1)
        if (Statified.currentPosition == -1) {
            Statified.currentPosition = 0
        }
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.currentSongHelper?.isLoop = false
        val nextSong = Statified.fetchSongs?.get(Statified.currentPosition as Int)
        Statified.currentSongHelper?.songPath = nextSong?.songData
        Statified.currentSongHelper?.songTitle = nextSong?.songTitle
        Statified.currentSongHelper?.songArtists = nextSong?.artist
        Statified.currentSongHelper?.songId = nextSong?.songID as Long
        Statified.currentSongHelper?.currentPosition = Statified.currentPosition
        Staticated.updateTextViews(
            Statified.currentSongHelper?.songTitle as String,
            Statified.currentSongHelper?.songArtists as String
        )


        Statified.mediaPlayer?.reset()
        try {
            Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(nextSong?.songData))
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(
                ContextCompat.getDrawable(
                    Statified.myActivity as Context,
                    R.drawable.favorite_on
                )
            )

        } else {
            Statified.fab?.setImageDrawable(
                ContextCompat.getDrawable(
                    Statified.myActivity as Context,
                    R.drawable.favorite_off
                )
            )
        }

    }

    fun bindShakeListner() {
        Statified.mSensorListner = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                mAccelarationLast = mAccelarationCurrent
                mAccelarationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()
                val delta = mAccelarationCurrent - mAccelarationLast
                mAccelaration = mAccelaration * 0.9f + delta

                if (mAccelaration > 12) {
                    val prefs =
                        Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        Staticated.playNext("PlayNextNormal")

                    } else {

                    }


                }


            }

        }
    }


}

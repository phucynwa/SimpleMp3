package com.example.simplemp3.ui.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.simplemp3.R
import com.example.simplemp3.service.PlayService
import com.example.simplemp3.service.PlayService.Companion.DEFAULT_LOOP_STATE
import com.example.simplemp3.service.PlayService.Companion.getPlayerIntent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var isServiceConnected = false
    private var isPlaying = false
    private var isLooping = DEFAULT_LOOP_STATE
    private var playService: PlayService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayService.PlayerServiceBinder
            playService = binder.getService()
            isServiceConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindService(getPlayerIntent(this), serviceConnection, BIND_AUTO_CREATE)
        displaySongDetails()
        initTimeLoader()
        initListener()
    }

    private fun displaySongDetails(songName: String = SONG_NAME, artistName: String = ARTIST_NAME) {
        textSongName.text = songName
        textArtistName.text = artistName
    }

    private fun initTimeLoader() {
        Handler().apply {
            post(object : Runnable {
                override fun run() {
                    barCurrentPosition.progress = playService?.getCurrentPercent() ?: 0
                    textCurrentTimer.text = getCurrentTimerString()
                    textTotalTimer.text = getTotalTimerString()
                    this@apply.postDelayed(this, DELAY_SEEK_TIME)
                }
            })
        }
    }

    private fun initListener() {
        imagePlayPause.setOnClickListener {
            changePlayerState()
            displayPlayerState(isPlaying)
            if (isPlaying) playService?.continuePlayer() else playService?.pausePlayer()
        }
        imageReset.setOnClickListener { playService?.setCurrentPosition(0) }
        imageLoop.setOnClickListener {
            changeLoopState()
            displayLoopState(isLooping)
            playService?.setLooping(isLooping)
        }
        barCurrentPosition.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) playService?.run { setCurrentPosition(getDuration() * progress / 100) }
            }
        })
    }

    private fun changePlayerState() {
        isPlaying = !isPlaying
    }

    private fun displayPlayerState(state: Boolean) {
        val resource = if (state) R.drawable.ic_pause_circle_outline_black_24dp
        else R.drawable.ic_play_circle_outline_black_24dp
        imagePlayPause.setImageResource(resource)
    }

    private fun changeLoopState() {
        isLooping = !isLooping
    }

    private fun displayLoopState(state: Boolean) {
        val resource = if (state) R.drawable.ic_sync_black_24dp else R.drawable.ic_sync_disabled_black_24dp
        imageLoop.setImageResource(resource)
    }

    private fun getCurrentTimerString(): String = timerFromMilliseconds(playService?.getCurrentPosition() ?: 0)

    private fun getTotalTimerString(): String = timerFromMilliseconds(playService?.getDuration() ?: 0)

    private fun timerFromMilliseconds(milliseconds: Int): String =
        String.format(TIMER_FORMAT, milliseconds / 60000, milliseconds / 1000 % 60)

    companion object {
        private const val DELAY_SEEK_TIME = 100L
        private const val SONG_NAME = "Ao Moi Ca Mau"
        private const val ARTIST_NAME = "Jang Mi"
        private const val TIMER_FORMAT = "%02d:%02d"
    }
}

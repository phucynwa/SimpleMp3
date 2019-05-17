package com.example.simplemp3.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.simplemp3.R

class PlayService : Service() {

    companion object {
        const val DEFAULT_LOOP_STATE = true

        fun getPlayerIntent(context: Context) = Intent(context, PlayService::class.java)
    }

    private var player: MediaPlayer? = null
    private val binder = PlayerServiceBinder()

    inner class PlayerServiceBinder : Binder() {
        fun getService() = this@PlayService
    }

    override fun onBind(intent: Intent?): IBinder? {
        player = MediaPlayer.create(this, R.raw.ao_moi_ca_mau).apply { isLooping = DEFAULT_LOOP_STATE }
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.stop()
    }

    fun getDuration(): Int = player?.duration ?: 0

    fun continuePlayer() = player?.run {
        seekTo(currentPosition)
        start()
    }

    fun pausePlayer() = player?.pause()

    fun setLooping(state: Boolean) {
        player?.isLooping = state
    }

    fun getCurrentPercent(): Int = player?.getCurrentPercentPosition() ?: 0

    fun getCurrentPosition(): Int = player?.currentPosition ?: 0

    fun setCurrentPosition(position: Int) = player?.seekTo(position)

    private fun MediaPlayer.getCurrentPercentPosition(): Int = currentPosition * 100 / duration
}

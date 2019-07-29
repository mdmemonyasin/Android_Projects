package com.example.echo.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.echo.R
import com.example.echo.Songs
import com.example.echo.fragments.SongPlayingFragment

class MainScreenAdapter(_songDetails: ArrayList<Songs>, _context: Context) :
    RecyclerView.Adapter<MainScreenAdapter.MyViewHolder>() {
    var songDetails: ArrayList<Songs>? = null
    var context: Context? = null

    init {
        this.context = _context
        this.songDetails = _songDetails
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MainScreenAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(p0.context)
            .inflate(R.layout.row_custom__mainscreen_adapter, p0, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if (songDetails == null) {
            return 0
        } else {
            return (songDetails as ArrayList<Songs>).size
        }
    }

    override fun onBindViewHolder(p0: MainScreenAdapter.MyViewHolder, p1: Int) {
        val songObjects = songDetails?.get(p1)
        p0.trackTitle?.text = songObjects?.songTitle
        p0.trackArtist?.text = songObjects?.artist
        p0.contentHolder?.setOnClickListener({
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", songObjects?.artist)
            args.putString("path", songObjects?.songData)
            args.putString("songTitle", songObjects?.songTitle)
            args.putInt("SongId", songObjects?.songID?.toInt() as Int)
            args.putInt("songPosition", p1)
            args.putParcelableArrayList("songData", songDetails)
            songPlayingFragment.arguments = args
            (context as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.details_fragment, songPlayingFragment)
                .addToBackStack("SongPlayingFragment")
                .commit()
        })


    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            trackTitle = view.findViewById<TextView>(R.id.trackTitle)
            trackArtist = view.findViewById<TextView>(R.id.trackArtist)
            contentHolder = view.findViewById<RelativeLayout>(R.id.contentRow)
        }
    }


}
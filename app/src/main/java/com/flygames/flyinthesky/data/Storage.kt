package com.travelgames.roadrace.data

import android.content.Context
import com.flygames.flyinthesky.R

class Storage(val context: Context) {
    private val sharedPref = context.getSharedPreferences(
        context.resources.getString(R.string.preference_file_key),
        Context.MODE_PRIVATE
    )
    fun getScore(): Int =
        sharedPref.getInt(context.resources.getString(R.string.saved_high_score_key), 0)

    fun setScore(score: Int) {
        with(sharedPref.edit()) {
            putInt(context.resources.getString(R.string.saved_high_score_key), score)
            apply()
        }
    }

}
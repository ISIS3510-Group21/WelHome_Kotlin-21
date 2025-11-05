package com.team21.myapplication.sync

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class FavoriteSyncQueue(ctx: Context) {
    private val prefs: SharedPreferences =
        ctx.getSharedPreferences("favorite_sync_queue", Context.MODE_PRIVATE)

    fun enqueue(userId: String, housingId: String, action: Action) {
        val arr = JSONArray(prefs.getString("q", "[]"))
        arr.put(JSONObject().apply {
            put("userId", userId)
            put("housingId", housingId)
            put("action", action.name) // "ADD" | "REMOVE"
        })
        prefs.edit().putString("q", arr.toString()).apply()
    }

    fun drain(): List<Item> {
        val arr = JSONArray(prefs.getString("q", "[]"))
        val out = mutableListOf<Item>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out += Item(
                userId = o.getString("userId"),
                housingId = o.getString("housingId"),
                action = Action.valueOf(o.getString("action"))
            )
        }
        prefs.edit().putString("q", "[]").apply()
        return out
    }

    data class Item(val userId: String, val housingId: String, val action: Action)
    enum class Action { ADD, REMOVE }
}

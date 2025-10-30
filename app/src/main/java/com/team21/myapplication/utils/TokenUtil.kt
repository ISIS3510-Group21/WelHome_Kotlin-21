package com.team21.myapplication.utils

import java.security.MessageDigest

/**
 * Util para tokenizar una combinación de filtros (ids + modo).
 * Esto se usa como clave en cache y en Hive (archivo).
 */
object TokenUtil {
    fun tokenFor(tagIdsOrdered: List<String>, mode: String): String {
        val key = "${tagIdsOrdered.joinToString("|")}#$mode"
        val md = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
        return md.take(8).joinToString("") { b -> "%02x".format(b) } // token corto
    }
}

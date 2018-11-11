package eu.miaounyan.isthereanynetwork.service

import java.text.SimpleDateFormat
import java.util.*

class DateFormatter() {
    fun toISO8601String(date : Date) : String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(date)
    }
}
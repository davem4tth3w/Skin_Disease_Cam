package dmi.developments.skin_disease_cam.utils

import java.text.SimpleDateFormat
import java.util.*

object Converters {
    fun formatTimestamp(timestamp: String?): String {
        if (timestamp.isNullOrBlank()) return "Unknown date"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = parser.parse(timestamp)
            val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
            if (date != null) formatter.format(date) else timestamp
        } catch (e: Exception) {
            timestamp
        }
    }
}

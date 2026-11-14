package com.st.sotan.amarmaheshkhali.data

/** Plain-text backup of the favourite list: easy to save in any notes/chat app and paste back later. */
object FavouritesBackup {
    private const val MARK = "#FAV "

    fun build(favourites: List<RecordEntity>): String {
        if (favourites.isEmpty()) return ""
        val sb = StringBuilder()
        sb.append("AMAR MOHESHKHALI - প্রিয় তথ্যের ব্যাকআপ\n")
        sb.append("এই পুরো লেখাটি কপি করে রাখুন। অ্যাপের সেটিংসে \"ফিরিয়ে আনুন\" চাপলে প্রিয় তালিকা ফিরে আসবে।\n\n")
        favourites.forEach { sb.append(MARK).append(it.recordId).append(" | ").append(it.banglaName).append('\n') }
        return sb.toString()
    }

    /** Record ids found in a backup text. Anything that is not a backup line is ignored. */
    fun parseIds(text: String): List<String> =
        text.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith(MARK) }
            .map { it.removePrefix(MARK).substringBefore(" | ").trim() }
            .filter { it.isNotEmpty() && it.length <= 100 && it.all { c -> c.isLetterOrDigit() || c == '-' || c == '_' } }
            .distinct()
            .toList()
}

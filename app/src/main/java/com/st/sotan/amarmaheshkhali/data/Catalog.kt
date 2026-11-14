package com.st.sotan.amarmaheshkhali.data

/** The 8 unions listed in the project brief (names to be verified against official sources). */
data class UnionInfo(val id: String, val bn: String, val en: String)

val UNIONS = listOf(
    UnionInfo("bara-maheshkhali", "বড় মহেশখালী", "Bara Maheshkhali"),
    UnionInfo("chhota-maheshkhali", "ছোট মহেশখালী", "Chhota Maheshkhali"),
    UnionInfo("kutubjom", "কুতুবজোম", "Kutubjom"),
    UnionInfo("hoanak", "হোয়ানক", "Hoanak"),
    UnionInfo("kalarmarchhara", "কালারমারছড়া", "Kalarmarchhara"),
    UnionInfo("shaplapur", "শাপলাপুর", "Shaplapur"),
    UnionInfo("matarbari", "মাতারবাড়ী", "Matarbari"),
    UnionInfo("dholghata", "ধলঘাটা", "Dholghata")
)

/** The 40 modules from Part 3 of the brief. */
data class CategoryInfo(val id: String, val bn: String, val en: String)

val CATEGORIES = listOf(
    CategoryInfo("introduction", "মহেশখালী পরিচিতি", "Maheshkhali Introduction"),
    CategoryInfo("union_directory", "ইউনিয়ন ও গ্রাম ডিরেক্টরি", "Union and Village Directory"),
    CategoryInfo("municipality", "পৌরসভা", "Municipality Information"),
    CategoryInfo("government_offices", "সরকারি অফিস", "Government Offices"),
    CategoryInfo("education", "শিক্ষা ডিরেক্টরি", "Education Directory"),
    CategoryInfo("healthcare", "হাসপাতাল ও স্বাস্থ্যসেবা", "Hospital and Healthcare Directory"),
    CategoryInfo("emergency", "জরুরি যোগাযোগ", "Emergency Contacts"),
    CategoryInfo("tourism", "পর্যটন ও ভ্রমণ গাইড", "Tourism and Travel Guide"),
    CategoryInfo("adinath_temple", "আদিনাথ মন্দির", "Adinath Temple"),
    CategoryInfo("sonadia_island", "সোনাদিয়া দ্বীপ", "Sonadia Island"),
    CategoryInfo("beaches_hills", "সমুদ্রসৈকত ও পাহাড়", "Beaches and Hills"),
    CategoryInfo("matarbari_port", "মাতারবাড়ী গভীর সমুদ্রবন্দর", "Matarbari Deep Sea Port"),
    CategoryInfo("matarbari_power", "মাতারবাড়ী বিদ্যুৎ প্রকল্প", "Matarbari Power Projects"),
    CategoryInfo("industry", "শিল্প উন্নয়ন", "Industrial Development"),
    CategoryInfo("infrastructure", "সড়ক, সেতু ও অবকাঠামো", "Roads, Bridges and Infrastructure"),
    CategoryInfo("agriculture", "কৃষি", "Agriculture"),
    CategoryInfo("betel_leaf", "পান চাষ", "Betel Leaf Economy"),
    CategoryInfo("salt", "লবণ উৎপাদন", "Salt Production"),
    CategoryInfo("mida", "মহেশখালী উন্নয়ন কর্তৃপক্ষ (মিডা)", "Maheshkhali Development Authority (MIDA)"),
    CategoryInfo("dry_fish", "শুঁটকি শিল্প", "Dry Fish Industry"),
    CategoryInfo("shrimp", "চিংড়ি ও জলজ চাষ", "Shrimp and Aquaculture"),
    CategoryInfo("markets", "বাজার ও ব্যবসা", "Local Markets and Businesses"),
    CategoryInfo("politics", "রাজনীতি ও জনপ্রতিনিধি", "Politics and Public Representatives"),
    CategoryInfo("elections", "নির্বাচন ও স্থানীয় সরকার", "Election and Local Government Information"),
    CategoryInfo("social_orgs", "সামাজিক সংগঠন", "Social Organizations"),
    CategoryInfo("culture", "সাংস্কৃতিক ঐতিহ্য", "Cultural Heritage"),
    CategoryInfo("sports_youth", "ক্রীড়া ও যুব সংগঠন", "Sports and Youth Organizations"),
    CategoryInfo("transport", "সড়ক ও পরিবহন", "Roads and Transport"),
    CategoryInfo("boat_routes", "নৌপথ ও ঘাট", "Boat Routes and Jetties"),
    CategoryInfo("news_archive", "স্থানীয় সংবাদ আর্কাইভ", "Local News Archive"),
    CategoryInfo("maps", "অফলাইন মানচিত্র", "Offline Maps"),
    CategoryInfo("photos", "ফটো গ্যালারি", "Photo Gallery"),
    CategoryInfo("videos", "ভিডিও গ্যালারি", "Video Gallery")
)

fun categoryName(id: String, lang: String): String {
    val c = CATEGORIES.firstOrNull { it.id == id } ?: return id
    return if (lang == "bn") c.bn else c.en
}

fun unionName(id: String, lang: String): String {
    val u = UNIONS.firstOrNull { it.id == id } ?: return ""
    return if (lang == "bn") u.bn else u.en
}

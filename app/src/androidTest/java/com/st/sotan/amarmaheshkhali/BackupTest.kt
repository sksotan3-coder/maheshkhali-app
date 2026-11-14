package com.st.sotan.amarmaheshkhali

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.st.sotan.amarmaheshkhali.data.FavouritesBackup
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupTest {
    @Test
    fun parsesOnlyBackupLines() {
        val text = """
            AMAR MOHESHKHALI - backup
            random chat text
            #FAV photo-01 | সবুজ পাহাড়
            #FAV edu-college-106372 | মহেশখালী কলেজ
            #FAV photo-01 | duplicate
            #FAV bad id with spaces | x
            #FAV  | empty
        """.trimIndent()
        assertEquals(listOf("photo-01", "edu-college-106372"), FavouritesBackup.parseIds(text))
    }

    @Test
    fun garbageGivesNothing() {
        assertEquals(emptyList<String>(), FavouritesBackup.parseIds(""))
        assertEquals(emptyList<String>(), FavouritesBackup.parseIds("hello\n#FAVOURITE x"))
        assertEquals("", FavouritesBackup.build(emptyList()))
    }
}

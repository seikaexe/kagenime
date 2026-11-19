package com.fuwanime

import com.fuwanime.data.remote.releaseFromTag
import com.fuwanime.data.remote.tagFromReleaseLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubFallbackTest {
    @Test fun readsTheTagFromTheRedirect() {
        assertEquals("v2.7.0", tagFromReleaseLocation("https://github.com/seikaexe/kagenime/releases/tag/v2.7.0"))
        assertNull(tagFromReleaseLocation(null))
        assertNull(tagFromReleaseLocation("https://github.com/seikaexe/kagenime/releases"))
    }

    @Test fun rebuildsTheReleaseFromItsTag() {
        val r = releaseFromTag("seikaexe/kagenime", "v2.7.0")
        assertEquals("v2.7.0", r.tagName)
        assertEquals(
            "https://github.com/seikaexe/kagenime/releases/download/v2.7.0/kagenime.apk",
            r.assets.single().url,
        )
    }
}

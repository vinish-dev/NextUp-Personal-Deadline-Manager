package com.vinish.nextup

import com.vinish.nextup.model.toSentenceCase
import com.vinish.nextup.model.toTitleCase
import org.junit.Assert.assertEquals
import org.junit.Test

class StringFormattingTest {

    @Test
    fun `toTitleCase formats words to title case and preserves acronyms`() {
        assertEquals("Submit Lab Report", "submit lab report".toTitleCase())
        assertEquals("Pay Hostel Fees", "pay hostel fees".toTitleCase())
        assertEquals("Complete DSA Assignment", "complete dsa assignment".toTitleCase())
        assertEquals("Submit DBMS Assignment", "submit dbms assignment".toTitleCase())
        assertEquals("Design UI/UX Mockups", "design ui/ux mockups".toTitleCase())
        assertEquals("Fix API Endpoint", "fix api endpoint".toTitleCase())
    }

    @Test
    fun `toSentenceCase capitalizes first letter of sentences while preserving proper nouns and casing`() {
        assertEquals(
            "Complete the final report and upload it on Moodle.",
            "complete the final report and upload it on Moodle.".toSentenceCase()
        )
        assertEquals(
            "First sentence. Second sentence.",
            "first sentence. second sentence.".toSentenceCase()
        )
        assertEquals(
            "Already Capitalized Sentence.",
            "Already Capitalized Sentence.".toSentenceCase()
        )
    }
}

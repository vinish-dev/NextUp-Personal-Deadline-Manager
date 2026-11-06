package com.vinish.nextup

import com.vinish.nextup.model.toSentenceCase
import com.vinish.nextup.model.toTitleCase
import com.vinish.nextup.ui.quickadd.parseQuickAddInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun `parseQuickAddInput extracts first line as title and remaining lines as description`() {
        val input = """
            Complete DBMS assignment
            Cover normalization, transaction management and indexing. Refer to Module 3 notes.
        """.trimIndent()
        val (title, description) = parseQuickAddInput(input)
        assertEquals("Complete DBMS assignment", title)
        assertEquals("Cover normalization, transaction management and indexing. Refer to Module 3 notes.", description)
    }

    @Test
    fun `parseQuickAddInput handles single line without description`() {
        val (title, description) = parseQuickAddInput("Complete DBMS assignment")
        assertEquals("Complete DBMS assignment", title)
        assertNull(description)
    }

    @Test
    fun `parseQuickAddInput handles single line with trailing newline without description`() {
        val (title, description) = parseQuickAddInput("Complete DBMS assignment\n")
        assertEquals("Complete DBMS assignment", title)
        assertNull(description)
    }

    @Test
    fun `parseQuickAddInput handles multiple description lines preserving structure`() {
        val input = "Meeting with team\n1. Discuss roadmap\n2. Review backlog"
        val (title, description) = parseQuickAddInput(input)
        assertEquals("Meeting with team", title)
        assertEquals("1. Discuss roadmap\n2. Review backlog", description)
    }
}

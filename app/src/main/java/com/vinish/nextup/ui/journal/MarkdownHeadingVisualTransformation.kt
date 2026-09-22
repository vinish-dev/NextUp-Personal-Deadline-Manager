package com.vinish.nextup.ui.journal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp

/**
 * VisualTransformation that formats Markdown headings (# H1, ## H2, ### H3):
 * - When the user's cursor is on the heading line, the hash prefix (e.g. "## ") is displayed and styled.
 * - When the user's cursor is NOT on the heading line, the hash prefix is hidden and the heading text
 *   is rendered flush to the left margin (no invisible space shifting the text).
 * - Offsets are bidirectionally mapped so tapping on the heading places the cursor correctly and reveals the hashes.
 */
class MarkdownHeadingVisualTransformation(
    private val selection: TextRange,
    private val primaryColor: Color,
    private val headingColor: Color
) : VisualTransformation {

    data class CollapsedPrefix(
        val origStart: Int,
        val prefixLength: Int,
        val transStart: Int
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        if (!rawText.contains('#')) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val regex = Regex("^(#{1,3})[ \\t]+(.*)$", RegexOption.MULTILINE)
        val matches = regex.findAll(rawText).toList()
        if (matches.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val transformedBuilder = AnnotatedString.Builder()
        val collapsedList = mutableListOf<CollapsedPrefix>()
        var lastCopiedOrig = 0
        var currentTransLength = 0

        for (match in matches) {
            val lineStart = match.range.first
            val lineEnd = match.range.last + 1

            // Copy preceding plain text unchanged
            if (lineStart > lastCopiedOrig) {
                val segment = rawText.substring(lastCopiedOrig, lineStart)
                transformedBuilder.append(segment)
                currentTransLength += segment.length
            }

            val hashes = match.groupValues[1]
            val title = match.groupValues[2]
            val prefixLength = match.value.length - title.length
            val hashesLength = hashes.length

            val (fontSize, fontWeight) = when (hashesLength) {
                1 -> Pair(22.sp, FontWeight.Bold)
                2 -> Pair(19.sp, FontWeight.Bold)
                else -> Pair(17.sp, FontWeight.SemiBold)
            }

            // Check if cursor/selection intersects with this heading line
            val isCursorOnLine = selection.min <= lineEnd && selection.max >= lineStart

            if (isCursorOnLine) {
                // User is on this line: show the hashes and title
                val headingStartInTrans = currentTransLength
                transformedBuilder.append(match.value)
                currentTransLength += match.value.length

                // Style the '#' hashes
                transformedBuilder.addStyle(
                    SpanStyle(
                        color = primaryColor.copy(alpha = 0.55f),
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize
                    ),
                    headingStartInTrans,
                    headingStartInTrans + hashesLength
                )

                // Style the heading text
                transformedBuilder.addStyle(
                    SpanStyle(
                        color = headingColor,
                        fontWeight = fontWeight,
                        fontSize = fontSize
                    ),
                    headingStartInTrans + hashesLength,
                    headingStartInTrans + match.value.length
                )
            } else {
                // User is not on this line: collapse hashes and space, show title flush to left
                collapsedList.add(
                    CollapsedPrefix(
                        origStart = lineStart,
                        prefixLength = prefixLength,
                        transStart = currentTransLength
                    )
                )

                val headingStartInTrans = currentTransLength
                transformedBuilder.append(title)
                currentTransLength += title.length

                // Style title with heading font size and weight (flush left, no shift!)
                transformedBuilder.addStyle(
                    SpanStyle(
                        color = headingColor,
                        fontWeight = fontWeight,
                        fontSize = fontSize
                    ),
                    headingStartInTrans,
                    headingStartInTrans + title.length
                )
            }

            lastCopiedOrig = lineEnd
        }

        // Copy remainder of the text
        if (lastCopiedOrig < rawText.length) {
            transformedBuilder.append(rawText.substring(lastCopiedOrig))
        }

        val transformed = transformedBuilder.toAnnotatedString()
        val offsetMapping = MarkdownHeadingOffsetMapping(
            collapsedList = collapsedList,
            origLength = rawText.length,
            transLength = transformed.length
        )

        return TransformedText(transformed, offsetMapping)
    }

    private class MarkdownHeadingOffsetMapping(
        private val collapsedList: List<CollapsedPrefix>,
        private val origLength: Int,
        private val transLength: Int
    ) : OffsetMapping {

        override fun originalToTransformed(offset: Int): Int {
            val clamped = offset.coerceIn(0, origLength)
            var shift = 0
            for (item in collapsedList) {
                if (clamped < item.origStart) {
                    break
                } else if (clamped < item.origStart + item.prefixLength) {
                    // Inside the collapsed "## " prefix -> map to start of title in transformed
                    return item.transStart
                } else {
                    shift += item.prefixLength
                }
            }
            return (clamped - shift).coerceIn(0, transLength)
        }

        override fun transformedToOriginal(offset: Int): Int {
            val clamped = offset.coerceIn(0, transLength)
            var shift = 0
            for (item in collapsedList) {
                if (clamped < item.transStart) {
                    break
                } else {
                    shift += item.prefixLength
                }
            }
            return (clamped + shift).coerceIn(0, origLength)
        }
    }
}

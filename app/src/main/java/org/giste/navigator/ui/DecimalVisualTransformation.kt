package org.giste.navigator.ui

import android.icu.text.DecimalFormatSymbols
import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.math.max

private const val CLASS_NAME = "DecimalVisualTransformation"

class DecimalVisualTransformation(
    private val numberOfIntegerDigits: Int,
    private val numberOfDecimals: Int = 2,
    private val fixedCursorAtTheEnd: Boolean = true,
    private val symbols: DecimalFormatSymbols = DecimalFormatSymbols.getInstance()
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val thousandsSeparator = symbols.groupingSeparator
        val decimalSeparator = symbols.decimalSeparator
        val zeroDigit = symbols.zeroDigit
        Log.v(
            CLASS_NAME,
            "Thousands: '$thousandsSeparator'; Decimal: '$decimalSeparator'; Zero: '$zeroDigit'"
        )

        val inputText = text.text
        Log.d(CLASS_NAME, "Text: '$inputText'")

        val intPart = inputText
            .dropLast(numberOfDecimals)
            .take(numberOfIntegerDigits)
            .reversed()
            .chunked(3)
            .joinToString(thousandsSeparator.toString())
            .reversed()
            .ifEmpty { zeroDigit.toString() }

        val fractionPart = inputText.takeLast(numberOfDecimals).let {
            if (it.length != numberOfDecimals) {
                List(numberOfDecimals - it.length) {
                    zeroDigit
                }.joinToString("") + it
            } else {
                it
            }
        }
        Log.v(CLASS_NAME, "Integer: $intPart; Fraction: $fractionPart")

        val formattedNumber = intPart + decimalSeparator + fractionPart
        Log.d(CLASS_NAME, "Formatted: $formattedNumber")

        val newText = AnnotatedString(
            text = formattedNumber,
            spanStyles = text.spanStyles,
            paragraphStyles = text.paragraphStyles
        )

        val offsetMapping = if (fixedCursorAtTheEnd) {
            FixedCursorOffsetMapping(
                contentLength = inputText.length,
                formattedContentLength = formattedNumber.length
            )
        } else {
            MovableCursorOffsetMapping(
                unmaskedText = text.toString(),
                maskedText = newText.toString(),
                decimalDigits = numberOfDecimals
            )
        }

        Log.d(CLASS_NAME, "New text: ${newText.text}")
        return TransformedText(newText, offsetMapping)
    }

    private class FixedCursorOffsetMapping(
        private val contentLength: Int,
        private val formattedContentLength: Int,
    ) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = formattedContentLength
        override fun transformedToOriginal(offset: Int): Int = contentLength
    }

    private class MovableCursorOffsetMapping(
        private val unmaskedText: String,
        private val maskedText: String,
        private val decimalDigits: Int
    ) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int =
            when {
                unmaskedText.length <= decimalDigits -> {
                    maskedText.length - (unmaskedText.length - offset)
                }

                else -> {
                    offset + offsetMaskCount(offset, maskedText)
                }
            }

        override fun transformedToOriginal(offset: Int): Int =
            when {
                unmaskedText.length <= decimalDigits -> {
                    max(unmaskedText.length - (maskedText.length - offset), 0)
                }

                else -> {
                    offset - maskedText.take(offset).count { !it.isDigit() }
                }
            }

        private fun offsetMaskCount(offset: Int, maskedText: String): Int {
            var maskOffsetCount = 0
            var dataCount = 0
            for (maskChar in maskedText) {
                if (!maskChar.isDigit()) {
                    maskOffsetCount++
                } else if (++dataCount > offset) {
                    break
                }
            }
            return maskOffsetCount
        }
    }
}
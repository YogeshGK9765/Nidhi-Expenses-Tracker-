package com.example

import com.example.domain.MoneyFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMoneyFormatterFormatting() {
        assertEquals("₹80.50", MoneyFormatter.formatPaise(8050L))
        assertEquals("₹80", MoneyFormatter.formatPaise(8000L))
        assertEquals("₹8,250", MoneyFormatter.formatPaise(825000L))
        assertEquals("₹1,00,000", MoneyFormatter.formatPaise(10000000L))
        assertEquals("₹520", MoneyFormatter.formatPaise(52000L))
        assertEquals("- ₹80", MoneyFormatter.formatPaise(-8000L, includePrefixSign = true))
    }

    @Test
    fun testMoneyFormatterParsing() {
        assertEquals(8050L, MoneyFormatter.parseInputToPaise("80.50"))
        assertEquals(8050L, MoneyFormatter.parseInputToPaise("80.5"))
        assertEquals(8000L, MoneyFormatter.parseInputToPaise("80"))
        assertEquals(825000L, MoneyFormatter.parseInputToPaise("8,250"))
        assertEquals(8005L, MoneyFormatter.parseInputToPaise("80.05"))
        assertEquals(0L, MoneyFormatter.parseInputToPaise(""))
    }
}

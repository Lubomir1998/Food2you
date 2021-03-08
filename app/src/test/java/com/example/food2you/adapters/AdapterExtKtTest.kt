package com.example.food2you.adapters

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdapterExtKtTest {

    @Test
    fun `price without decimals returns 2 digits after the dot`() {
        val price = formattedStringPrice("12")

        assertThat(price).isEqualTo("12.00")
    }

    @Test
    fun `price with 1 decimal returns 2 digits after the dot`() {
        val price = formattedStringPrice("12.4")

        assertThat(price).isEqualTo("12.40")
    }

    @Test
    fun `price with 2 decimals remains the same`() {
        val price = formattedStringPrice("12.40")

        assertThat(price).isEqualTo("12.40")
    }

    @Test
    fun `price with more than 2 decimals returns 2 digits after the dot`() {
        val price = formattedStringPrice("12.4047384424634")

        assertThat(price).isEqualTo("12.40")
    }

}
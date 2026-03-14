package com.huanchengfly.tieba.core.theme2.semantic

import com.materialkolor.palettes.CorePalette
import org.junit.Test

class ThemeMcuPalettePreviewTest {
    @Test
    fun printSeedPalette() {
        val seed = 0xFF4477E0.toInt()
        val palette = CorePalette.of(seed)
        val neutralVariant = palette.n2
        val secondary = palette.a2

        fun hex(argb: Int): String = String.format("#%08X", argb)

        println("seed=${hex(seed)}")
        println(
            "neutralVariant tones: " +
                "99=${hex(neutralVariant.tone(99))}, " +
                "95=${hex(neutralVariant.tone(95))}, " +
                "60=${hex(neutralVariant.tone(60))}, " +
                "40=${hex(neutralVariant.tone(40))}, " +
                "20=${hex(neutralVariant.tone(20))}, " +
                "10=${hex(neutralVariant.tone(10))}, " +
                "0=${hex(neutralVariant.tone(0))}"
        )
        println(
            "secondary tones: " +
                "100=${hex(secondary.tone(100))}, " +
                "80=${hex(secondary.tone(80))}, " +
                "40=${hex(secondary.tone(40))}, " +
                "20=${hex(secondary.tone(20))}"
        )
    }
}

package com.anyNews.anynews.Utils;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TtsLanguageLocalesTest {

    @Test
    public void eachSupportedLanguageUsesItsOwnLocale() {
        assertLocale(1, "ml", "IN");
        assertLocale(2, "hi", "IN");
        assertLocale(3, "te", "IN");
        assertLocale(4, "ta", "IN");
        assertLocale(5, "kn", "IN");
        assertLocale(6, "or", "IN");
        assertLocale(7, "en", "IN");
        assertLocale(8, "bn", "IN");
        assertLocale(9, "mr", "IN");
        assertLocale(10, "gu", "IN");
    }

    @Test
    public void hindiAndKannadaDoNotFallThroughToTelugu() {
        assertNotEquals("te", TtsLanguageLocales.forLanguageId(2).getLanguage());
        assertNotEquals("te", TtsLanguageLocales.forLanguageId(5).getLanguage());
    }

    @Test
    public void unknownLanguageDoesNotUseTelugu() {
        Locale unknown = TtsLanguageLocales.forLanguageId(0);
        assertEquals("en", unknown.getLanguage());
        assertNotEquals("te", unknown.getLanguage());
        assertNotEquals("te", TtsLanguageLocales.forLanguageId(99).getLanguage());
    }

    private static void assertLocale(int languageId, String language, String country) {
        Locale locale = TtsLanguageLocales.forLanguageId(languageId);
        assertEquals(language, locale.getLanguage());
        assertEquals(country, locale.getCountry());
    }
}

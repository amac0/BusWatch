// ABOUTME: Unit tests for the legal document addresses shown in the app
// ABOUTME: Pins them to the GitHub Pages site the Play Store listing points at
package com.buswatch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegalLinksTest {
    @Test
    fun `privacy policy matches the store listing address`() {
        assertEquals("https://amac0.github.io/BusWatch/privacy-policy.html", LegalLinks.PRIVACY_POLICY_URL)
    }

    @Test
    fun `terms live next to the privacy policy`() {
        assertEquals("https://amac0.github.io/BusWatch/terms-of-service.html", LegalLinks.TERMS_OF_SERVICE_URL)
        assertTrue(LegalLinks.TERMS_OF_SERVICE_URL.startsWith("https://"))
    }
}

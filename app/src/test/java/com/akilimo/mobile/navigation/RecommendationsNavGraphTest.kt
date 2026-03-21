package com.akilimo.mobile.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Validates that nav_recommendations.xml contains all required destinations and actions.
 *
 * These tests run on the JVM (no device needed) and guard against:
 * - Accidental removal of a destination (would cause NavController to crash at runtime)
 * - Missing actions from the start destination
 */
class RecommendationsNavGraphTest {

    private val navFile = File("src/main/res/navigation/nav_recommendations.xml")

    private fun parseFragmentIds(): Set<String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(navFile)
        doc.documentElement.normalize()
        val fragments = doc.getElementsByTagName("fragment")
        return (0 until fragments.length)
            .map { i ->
                (fragments.item(i) as Element)
                    .getAttribute("android:id")
                    .removePrefix("@+id/")
                    .removePrefix("@id/")
            }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    @Test
    fun `nav_recommendations contains exactly 20 fragment destinations`() {
        val ids = parseFragmentIds()
        assertEquals(
            "Expected 20 destinations — add one here if you add a new use-case fragment.\n  Found: $ids",
            20, ids.size
        )
    }

    @Test
    fun `nav_recommendations contains all recommendation sub-screen fragments`() {
        val ids = parseFragmentIds()
        val required = setOf(
            "recommendationsFragment",
            "frFragment",
            "bppFragment",
            "sphFragment",
            "icMaizeFragment",
            "icSweetPotatoFragment"
        )
        assertTrue(
            "Missing recommendation sub-fragments: ${required - ids}",
            ids.containsAll(required)
        )
    }

    @Test
    fun `nav_recommendations contains all use-case destinations`() {
        val ids = parseFragmentIds()
        val required = setOf(
            "fertilizerFragment",
            "interCropFertilizersFragment",
            "sweetPotatoInterCropFertilizersFragment",
            "investmentAmountFragment",
            "cassavaMarketFragment",
            "cassavaYieldFragment",
            "datesFragment",
            "manualTillageCostFragment",
            "tractorAccessFragment",
            "weedControlCostsFragment",
            "maizeMarketFragment",
            "maizePerformanceFragment",
            "sweetPotatoMarketFragment",
            "getRecommendationFragment"
        )
        assertTrue(
            "Missing use-case destinations: ${required - ids}",
            ids.containsAll(required)
        )
    }

    @Test
    fun `recommendationsFragment has all navigation actions to sub-screens`() {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(navFile)
        doc.documentElement.normalize()
        val fragments = doc.getElementsByTagName("fragment")

        var actionIds = emptySet<String>()
        for (i in 0 until fragments.length) {
            val element = fragments.item(i) as Element
            val id = element.getAttribute("android:id")
            if ("recommendationsFragment" in id) {
                val actions = element.getElementsByTagName("action")
                actionIds = (0 until actions.length)
                    .map { j ->
                        (actions.item(j) as Element)
                            .getAttribute("android:id")
                            .removePrefix("@+id/")
                    }
                    .toSet()
                break
            }
        }

        val required = setOf(
            "action_recommendations_to_fr",
            "action_recommendations_to_bpp",
            "action_recommendations_to_sph",
            "action_recommendations_to_icMaize",
            "action_recommendations_to_icSweetPotato"
        )
        assertTrue(
            "Missing actions in recommendationsFragment: ${required - actionIds}",
            actionIds.containsAll(required)
        )
    }
}

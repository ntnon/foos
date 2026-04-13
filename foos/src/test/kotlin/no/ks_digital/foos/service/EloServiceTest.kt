package no.ks_digital.foos.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested

class EloServiceTest {

    private val eloService = EloService()

    @Nested
    inner class KFactorTests {

        @Test
        fun `k-factor equals base k-factor when no games played`() {
            val k = eloService.calculateKFactor(0)
            assertEquals(EloService.BASE_K_FACTOR, k, 0.001)
        }

        @Test
        fun `k-factor decreases as games played increases`() {
            val kEarly = eloService.calculateKFactor(10)
            val kMid = eloService.calculateKFactor(50)
            val kLate = eloService.calculateKFactor(200)
            assertTrue(kEarly > kMid)
            assertTrue(kMid > kLate)
        }

        @Test
        fun `k-factor at games threshold is half of base`() {
            // At games = GAMES_THRESHOLD: K = BASE_K / (1 + 1) = BASE_K / 2
            val k = eloService.calculateKFactor(EloService.GAMES_THRESHOLD.toInt())
            assertEquals(EloService.BASE_K_FACTOR / 2.0, k, 0.001)
        }

        @Test
        fun `k-factor is always positive`() {
            listOf(0, 1, 10, 100, 1000).forEach { games ->
                assertTrue(eloService.calculateKFactor(games) > 0)
            }
        }
    }

    @Nested
    inner class PointFactorTests {

        @Test
        fun `point factor is at minimum when scores are equal`() {
            val factor = eloService.calculatePointFactor(5, 5)
            // log10(0 + 1) = 0, so factor = BASE_POINT_FACTOR + 0 = 2.0
            assertEquals(EloService.BASE_POINT_FACTOR, factor, 0.001)
        }

        @Test
        fun `point factor increases with larger score margin`() {
            val smallMargin = eloService.calculatePointFactor(6, 5)
            val largeMargin = eloService.calculatePointFactor(10, 0)
            assertTrue(largeMargin > smallMargin)
        }

        @Test
        fun `point factor is symmetric regardless of which team wins`() {
            val factor1 = eloService.calculatePointFactor(10, 5)
            val factor2 = eloService.calculatePointFactor(5, 10)
            assertEquals(factor1, factor2, 0.001)
        }

        @Test
        fun `point factor is always at least the base value`() {
            listOf(Pair(5, 5), Pair(10, 5), Pair(10, 0)).forEach { (s1, s2) ->
                assertTrue(eloService.calculatePointFactor(s1, s2) >= EloService.BASE_POINT_FACTOR)
            }
        }
    }

    @Nested
    inner class ExpectedScoreTests {

        @Test
        fun `equal ratings produce expected score of 0_5`() {
            val expected = eloService.calculatePlayerExpectedScore(1600.0, 1600.0)
            assertEquals(0.5, expected, 0.001)
        }

        @Test
        fun `higher rated player has expected score above 0_5`() {
            val expected = eloService.calculatePlayerExpectedScore(1800.0, 1600.0)
            assertTrue(expected > 0.5)
        }

        @Test
        fun `lower rated player has expected score below 0_5`() {
            val expected = eloService.calculatePlayerExpectedScore(1400.0, 1600.0)
            assertTrue(expected < 0.5)
        }

        @Test
        fun `expected scores for player and opponent against each other sum to 1`() {
            val playerRating = 1700.0
            val opponentRating = 1500.0
            val expectedPlayer = eloService.calculatePlayerExpectedScore(playerRating, opponentRating)
            val expectedOpponent = eloService.calculatePlayerExpectedScore(opponentRating, playerRating)
            assertEquals(1.0, expectedPlayer + expectedOpponent, 0.001)
        }

        @Test
        fun `total expected score against two equal opponents equals score against one`() {
            val playerRating = 1600.0
            val opponentRating = 1600.0
            val total = eloService.calculatePlayerTotalExpectedScore(playerRating, opponentRating, opponentRating)
            assertEquals(0.5, total, 0.001)
        }

        @Test
        fun `total expected score is average of individual expected scores`() {
            val playerRating = 1700.0
            val opp1Rating = 1500.0
            val opp2Rating = 1800.0
            val e1 = eloService.calculatePlayerExpectedScore(playerRating, opp1Rating)
            val e2 = eloService.calculatePlayerExpectedScore(playerRating, opp2Rating)
            val total = eloService.calculatePlayerTotalExpectedScore(playerRating, opp1Rating, opp2Rating)
            assertEquals((e1 + e2) / 2.0, total, 0.001)
        }
    }

    @Nested
    inner class TeammateAdjustmentTests {

        @Test
        fun `equal ratings produce adjustment of 1_0`() {
            val adjustment = eloService.calculateTeammateAdjustment(1600.0, 1600.0)
            assertEquals(1.0, adjustment, 0.001)
        }

        @Test
        fun `stronger player than teammate gets adjustment above 1`() {
            // Player stronger than teammate: ratingDifference > 0 → adjustment > 1.0
            val adjustment = eloService.calculateTeammateAdjustment(1800.0, 1400.0)
            assertTrue(adjustment > 1.0)
        }

        @Test
        fun `weaker player than teammate gets adjustment below 1`() {
            // Player weaker than teammate: ratingDifference < 0 → adjustment < 1.0
            val adjustment = eloService.calculateTeammateAdjustment(1400.0, 1800.0)
            assertTrue(adjustment < 1.0)
        }

        @Test
        fun `adjustment is capped at 1_2 maximum`() {
            // Extreme positive difference is capped at +0.2 → adjustment = 1.2
            val adjustment = eloService.calculateTeammateAdjustment(5000.0, 1000.0)
            assertEquals(1.2, adjustment, 0.001)
        }

        @Test
        fun `adjustment is capped at 0_8 minimum`() {
            // Extreme negative difference is capped at -0.2 → adjustment = 0.8
            val adjustment = eloService.calculateTeammateAdjustment(1000.0, 5000.0)
            assertEquals(0.8, adjustment, 0.001)
        }
    }

    @Nested
    inner class NewRatingTests {

        @Test
        fun `winner gains rating points`() {
            val before = 1600.0
            val after = eloService.calculateNewPlayerRating(
                playerRating = before,
                teammateRating = 1600.0,
                opponent1Rating = 1600.0,
                opponent2Rating = 1600.0,
                gamesPlayed = 10,
                team1Score = 10,
                team2Score = 5,
                won = true
            )
            assertTrue(after > before)
        }

        @Test
        fun `loser loses rating points`() {
            val before = 1600.0
            val after = eloService.calculateNewPlayerRating(
                playerRating = before,
                teammateRating = 1600.0,
                opponent1Rating = 1600.0,
                opponent2Rating = 1600.0,
                gamesPlayed = 10,
                team1Score = 5,
                team2Score = 10,
                won = false
            )
            assertTrue(after < before)
        }

        @Test
        fun `upset win yields larger rating gain than expected win`() {
            // Underdog winning (lower rated player beating higher rated opponents)
            val undergogGain = eloService.calculateNewPlayerRating(
                playerRating = 1400.0,
                teammateRating = 1400.0,
                opponent1Rating = 1800.0,
                opponent2Rating = 1800.0,
                gamesPlayed = 10,
                team1Score = 10,
                team2Score = 5,
                won = true
            ) - 1400.0

            // Favourite winning (higher rated player beating lower rated opponents)
            val favouriteGain = eloService.calculateNewPlayerRating(
                playerRating = 1800.0,
                teammateRating = 1800.0,
                opponent1Rating = 1400.0,
                opponent2Rating = 1400.0,
                gamesPlayed = 10,
                team1Score = 10,
                team2Score = 5,
                won = true
            ) - 1800.0

            assertTrue(undergogGain > favouriteGain)
        }

        @Test
        fun `fewer games played results in larger rating change`() {
            val ratingChangeFewGames = Math.abs(
                eloService.calculateNewPlayerRating(
                    playerRating = 1600.0,
                    teammateRating = 1600.0,
                    opponent1Rating = 1600.0,
                    opponent2Rating = 1600.0,
                    gamesPlayed = 5,
                    team1Score = 10,
                    team2Score = 5,
                    won = true
                ) - 1600.0
            )

            val ratingChangeManyGames = Math.abs(
                eloService.calculateNewPlayerRating(
                    playerRating = 1600.0,
                    teammateRating = 1600.0,
                    opponent1Rating = 1600.0,
                    opponent2Rating = 1600.0,
                    gamesPlayed = 200,
                    team1Score = 10,
                    team2Score = 5,
                    won = true
                ) - 1600.0
            )

            assertTrue(ratingChangeFewGames > ratingChangeManyGames)
        }

        @Test
        fun `larger score margin results in larger rating change`() {
            val smallMarginChange = Math.abs(
                eloService.calculateNewPlayerRating(
                    playerRating = 1600.0,
                    teammateRating = 1600.0,
                    opponent1Rating = 1600.0,
                    opponent2Rating = 1600.0,
                    gamesPlayed = 10,
                    team1Score = 6,
                    team2Score = 5,
                    won = true
                ) - 1600.0
            )

            val largeMarginChange = Math.abs(
                eloService.calculateNewPlayerRating(
                    playerRating = 1600.0,
                    teammateRating = 1600.0,
                    opponent1Rating = 1600.0,
                    opponent2Rating = 1600.0,
                    gamesPlayed = 10,
                    team1Score = 10,
                    team2Score = 0,
                    won = true
                ) - 1600.0
            )

            assertTrue(largeMarginChange > smallMarginChange)
        }
    }

    @Nested
    inner class NewTeamRatingsTests {

        @Test
        fun `both team members gain rating on win`() {
            val (p1After, p2After) = eloService.calculateNewTeamRatings(
                player1Rating = 1600.0,
                player2Rating = 1600.0,
                opponent1Rating = 1600.0,
                opponent2Rating = 1600.0,
                player1Games = 10,
                player2Games = 10,
                team1Score = 10,
                team2Score = 5,
                won = true
            )
            assertTrue(p1After > 1600.0)
            assertTrue(p2After > 1600.0)
        }

        @Test
        fun `both team members lose rating on loss`() {
            val (p1After, p2After) = eloService.calculateNewTeamRatings(
                player1Rating = 1600.0,
                player2Rating = 1600.0,
                opponent1Rating = 1600.0,
                opponent2Rating = 1600.0,
                player1Games = 10,
                player2Games = 10,
                team1Score = 5,
                team2Score = 10,
                won = false
            )
            assertTrue(p1After < 1600.0)
            assertTrue(p2After < 1600.0)
        }

        @Test
        fun `players with different games played get different rating changes`() {
            val (p1After, p2After) = eloService.calculateNewTeamRatings(
                player1Rating = 1600.0,
                player2Rating = 1600.0,
                opponent1Rating = 1600.0,
                opponent2Rating = 1600.0,
                player1Games = 5,   // fewer games = higher K = bigger change
                player2Games = 200, // many games = lower K = smaller change
                team1Score = 10,
                team2Score = 5,
                won = true
            )
            val p1Gain = p1After - 1600.0
            val p2Gain = p2After - 1600.0
            assertTrue(p1Gain > p2Gain)
        }
    }
}

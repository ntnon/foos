package no.ks_digital.foos.service

import kotlin.math.log10
import kotlin.math.pow
import org.springframework.stereotype.Service

/**
 * Elo Rating System for 2v2 Foosball
 * Based on: https://medium.com/data-science/developing-an-elo-based-data-driven-ranking-system-for-2v2-multiplayer-games
 *
 * Key differences from chess:
 * - Uses divisor of 500 instead of 400 to account for chance element in foosball
 * - Calculates per-player expected scores against both opponents
 * - Adjusts ratings based on teammate skill level
 */
@Service
class EloService {

    companion object {
        // K-Factor Configuration
        /** Base K-factor for rating volatility */
        const val BASE_K_FACTOR = 50.0

        /** Games threshold for K-factor scaling */
        const val GAMES_THRESHOLD = 75.0

        // Point Factor Configuration
        /** Base point factor multiplier */
        const val BASE_POINT_FACTOR = 2.0

        /** Exponent for logarithmic scaling of score margin */
        const val POINT_FACTOR_EXPONENT = 3.0

        // Elo Calculation Configuration - 2v2 Specific
        /** Foosball Elo rating divisor (500 instead of chess's 400 to account for chance) */
        const val ELO_RATING_DIVISOR = 500.0

        // Initial Rating
        /** Default starting Elo rating for new players/pairs */
        const val DEFAULT_INITIAL_RATING = 1600.0

        // Win/Loss Constants
        /** Score for a win */
        const val WIN_SCORE = 1.0

        /** Score for a loss */
        const val LOSS_SCORE = 0.0

        /** Score for a draw */
        const val DRAW_SCORE = 0.5
    }

    /**
     * Calculate dynamic K-factor based on number of games played
     * Formula: K = BASE_K_FACTOR / (1 + (games / GAMES_THRESHOLD))
     */
    fun calculateKFactor(gameCount: Int): Double {
        return BASE_K_FACTOR / (1.0 + (gameCount / GAMES_THRESHOLD))
    }

    /**
     * Calculate point factor based on score margin
     * Formula: P = BASE_POINT_FACTOR + (log10(|score1 - score2| + 1))^POINT_FACTOR_EXPONENT
     */
    fun calculatePointFactor(team1Score: Int, team2Score: Int): Double {
        val scoreDifference = kotlin.math.abs(team1Score - team2Score)
        val logComponent = log10((scoreDifference + 1).toDouble())
        return BASE_POINT_FACTOR + logComponent.pow(POINT_FACTOR_EXPONENT)
    }

    /**
     * Calculate expected win probability for a single player against a single opponent
     * Formula: E = 1 / (1 + 10^((opponent_rating - player_rating) / ELO_RATING_DIVISOR))
     *
     * Uses DIVISOR of 500 for foosball (vs 400 for chess) to account for chance element
     */
    fun calculatePlayerExpectedScore(playerRating: Double, opponentRating: Double): Double {
        val ratingDifference = opponentRating - playerRating
        return 1.0 / (1.0 + 10.0.pow(ratingDifference / ELO_RATING_DIVISOR))
    }

    /**
     * Calculate a player's expected score against both opposing team members
     *
     * Formula from article:
     * E_P1 = (1/(1 + 10^((RP3 - RP1)/500)) + 1/(1 + 10^((RP4 - RP1)/500))) / 2
     *
     * Where:
     * - RP1 = Player 1's rating
     * - RP3, RP4 = Opponents' ratings
     * - Result = Average of expected scores against each opponent
     */
    fun calculatePlayerTotalExpectedScore(
        playerRating: Double,
        opponent1Rating: Double,
        opponent2Rating: Double
    ): Double {
        val expectedVsOpponent1 = calculatePlayerExpectedScore(playerRating, opponent1Rating)
        val expectedVsOpponent2 = calculatePlayerExpectedScore(playerRating, opponent2Rating)
        return (expectedVsOpponent1 + expectedVsOpponent2) / 2.0
    }

    /**
     * Calculate a team's expected score (average of both players' expected scores)
     *
     * Formula from article:
     * E_T1 = (E_P1 + E_P2) / 2
     *
     * Where:
     * - E_P1 = Player 1's expected score against both opponents
     * - E_P2 = Player 2's expected score against both opponents
     * - Result = Team's combined expected score
     */
    fun calculateTeamExpectedScore(
        player1Rating: Double,
        player2Rating: Double,
        opponent1Rating: Double,
        opponent2Rating: Double
    ): Double {
        val player1Expected = calculatePlayerTotalExpectedScore(player1Rating, opponent1Rating, opponent2Rating)
        val player2Expected = calculatePlayerTotalExpectedScore(player2Rating, opponent1Rating, opponent2Rating)
        return (player1Expected + player2Expected) / 2.0
    }

    /**
     * Calculate teammate skill adjustment factor
     * Stronger teammates (higher rating) reduce individual gains on wins
     * Weaker teammates (lower rating) increase individual gains on wins
     */
    fun calculateTeammateAdjustment(playerRating: Double, teammateRating: Double): Double {
        val ratingDifference = playerRating - teammateRating
        // If player is stronger than teammate, reduce gains; if weaker, increase gains
        // Scale from 0.8 to 1.2 based on rating difference
        return 1.0 + (ratingDifference / 1000.0).coerceIn(-0.2, 0.2)
    }

    /**
     * Calculate new rating for a single player after a 2v2 match
     *
     * Formula: new_rating = old_rating + (K × P) × teammate_adjustment × (E_actual - E_expected)
     *
     * @param playerRating Current player's Elo rating
     * @param teammateRating Player's teammate's Elo rating
     * @param opponent1Rating First opponent's Elo rating
     * @param opponent2Rating Second opponent's Elo rating
     * @param gamesPlayed Total games played by this player
     * @param team1Score Player's team score
     * @param team2Score Opponent team's score
     * @param won Whether the player's team won
     * @return New Elo rating for the player
     */
    fun calculateNewPlayerRating(
        playerRating: Double,
        teammateRating: Double,
        opponent1Rating: Double,
        opponent2Rating: Double,
        gamesPlayed: Int,
        team1Score: Int,
        team2Score: Int,
        won: Boolean
    ): Double {
        // Calculate K-factor based on games played
        val kFactor = calculateKFactor(gamesPlayed)

        // Calculate point factor based on score margin
        val pointFactor = calculatePointFactor(team1Score, team2Score)

        // Calculate expected score against both opponents
        val expectedScore = calculatePlayerTotalExpectedScore(playerRating, opponent1Rating, opponent2Rating)

        // Calculate teammate adjustment (stronger teammate reduces gains, weaker increases)
        val teammateAdjustment = calculateTeammateAdjustment(playerRating, teammateRating)

        // Actual outcome: WIN_SCORE if won, LOSS_SCORE if lost
        val actualOutcome = if (won) WIN_SCORE else LOSS_SCORE

        // Calculate rating change with teammate adjustment
        val ratingChange = (kFactor * pointFactor * teammateAdjustment) * (actualOutcome - expectedScore)

        // Return new rating
        return playerRating + ratingChange
    }

    /**
     * Calculate new ratings for both players on a team after a 2v2 match
     *
     * @return Pair of new ratings (player1NewRating, player2NewRating)
     */
    fun calculateNewTeamRatings(
        player1Rating: Double,
        player2Rating: Double,
        opponent1Rating: Double,
        opponent2Rating: Double,
        player1Games: Int,
        player2Games: Int,
        team1Score: Int,
        team2Score: Int,
        won: Boolean
    ): Pair<Double, Double> {
        val newPlayer1Rating = calculateNewPlayerRating(
            playerRating = player1Rating,
            teammateRating = player2Rating,
            opponent1Rating = opponent1Rating,
            opponent2Rating = opponent2Rating,
            gamesPlayed = player1Games,
            team1Score = team1Score,
            team2Score = team2Score,
            won = won
        )

        val newPlayer2Rating = calculateNewPlayerRating(
            playerRating = player2Rating,
            teammateRating = player1Rating,
            opponent1Rating = opponent1Rating,
            opponent2Rating = opponent2Rating,
            gamesPlayed = player2Games,
            team1Score = team1Score,
            team2Score = team2Score,
            won = won
        )

        return Pair(newPlayer1Rating, newPlayer2Rating)
    }
}





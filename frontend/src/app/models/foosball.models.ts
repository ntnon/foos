/**
 * Data Models and Interfaces for Foosball Tracker
 * These interfaces mirror the backend JPA entities
 */

/**
 * Spring Page response wrapper
 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;       // current page (0-based)
  first: boolean;
  last: boolean;
}

/**
 * Player - Represents an individual player
 */
export interface Player {
  playerId: number;
  name: string;
}

/**
 * Request to create a new player
 */
export interface CreatePlayerRequest {
  name: string;
}

/**
 * Request to update an existing player
 */
export interface UpdatePlayerRequest {
  name: string;
}

export type TeamColor = 'RED' | 'BLUE';

/**
 * Team - Represents a team of two players
 */
export interface Team {
  teamId: number;
  offense: Player;
  defense: Player;
  teamColor: TeamColor;
}

/**
 * Request to create a new team
 */
export interface CreateTeamRequest {
  offense: number;
  defense: number;
  teamColor: TeamColor;
}

/**
 * Request to update an existing team
 */
export interface UpdateTeamRequest {
  offense?: number;
  defense?: number;
  teamColor?: TeamColor;
}

/**
 * MatchPlayer - A player's Elo snapshot for a specific match
 */
export interface MatchPlayer {
  playerId: number;
  playerName: string;
  initialRating: number;
  ratingChange: number;
  newRating: number;
}

/**
 * MatchTeam - A team's result in a match, with player Elo snapshots
 */
export interface MatchTeam {
  teamId: number;
  teamColor: string;
  gameScore: number;
  pairWins: number;
  offense: MatchPlayer;
  defense: MatchPlayer;
  pairStats: TeamStats;
}

/**
 * Match - The rich response DTO for a foosball match.
 *
 * This is the single public representation of a match.
 * Contains everything: scores, Elo changes, historical head-to-head, winner.
 *
 * For creating a match, use CreateMatchRequest instead.
 */
export interface Match {
  matchId: number;
  matchDate: string;
  team1: MatchTeam;
  team2: MatchTeam;
  winner: string | null;
  winnerColor: string | null;
}

export interface TeamRequest {
  offense: number;
  defense: number;
  teamColor: TeamColor;
}

/**
 * Request to create a new match
 * Winner is automatically determined from game scores
 * Match date is set server-side to the current date
 */
export interface CreateMatchRequest {
  team1: TeamRequest;
  team2: TeamRequest;
  team1GameScore: number;
  team2GameScore: number;
}

/**
 * Request to update an existing match
 */
export interface UpdateMatchRequest {
  matchDate?: string;
  team1GameScore?: number;
  team2GameScore?: number;
}

export interface RivalTeamSummary {
  rivalTeamStatsId: number;
  rival1Id: number;
  rival1Name: string;
  rival2Id: number;
  rival2Name: string;
  matchesAgainst: number;
  lossesAgainst: number;
  lossRate: number;
}

export interface PlayerPositionBreakdown {
  playerId: number;
  playerName: string;
  matchesAsOffense: number;
  matchesAsDefense: number;
  winsAsOffense: number;
  winsAsDefense: number;
  offenseWinRate: number;
  defenseWinRate: number;
}

export interface PartnerSummary {
  partnerId: number;
  partnerName: string;
  matchesTogether: number;
  winsTogether: number;
  winRate: number;
}

export interface RivalSummary {
  rivalId: number;
  rivalName: string;
  matchesAgainst: number;
  lossesAgainst: number;
  lossRate: number;
}

/**
 * Team Statistics (pair of players, position/color agnostic)
 */
export interface TeamStats {
  teamStatsId: number;
  player1Id: number;
  player1Name: string;
  player2Id: number;
  player2Name: string;
  totalMatches: number;
  wins: number;
  losses: number;
  draws: number;
  winRate: number;
  preferredColor?: string;
  currentWinStreak: number;
  longestWinStreak: number;
  hotStreak: boolean;
  averageScoreDifference: number;
  totalPointsScored: number;
  totalPointsAllowed: number;
  avgPointsScoredPerMatch: number;
  avgPointsAllowedPerMatch: number;
  matchesAsRed: number;
  winsAsRed: number;
  matchesAsBlue: number;
  winsAsBlue: number;
  colorWinRateRed: number;
  colorWinRateBlue: number;
  avgMatchesPerWeek: number;
  lastPlayed: string;
  eloRating: number;
  eloHistory: number[];
  eloTrend: number[];
  rivalTeams: RivalTeamSummary[];
  positionBreakdown: PlayerPositionBreakdown[];
}

/**
 * Individual Player Statistics
 */
export interface PlayerStatsResponse {
  playerStatsId: number;
  playerId: number;
  playerName: string;
  totalMatches: number;
  wins: number;
  losses: number;
  draws: number;
  winRate: number;
  bestPosition?: string;
  mostWinningColor?: string;
  currentWinStreak: number;
  longestWinStreak: number;
  hotStreak: boolean;
  matchesAsOffense: number;
  winsAsOffense: number;
  matchesAsDefense: number;
  winsAsDefense: number;
  positionWinRateOffense: number;
  positionWinRateDefense: number;
  matchesAsRed: number;
  winsAsRed: number;
  matchesAsBlue: number;
  winsAsBlue: number;
  colorWinRateRed: number;
  colorWinRateBlue: number;
  lastPlayed: string;
  eloRating: number;
  eloHistory: number[];
  eloTrend: number[];
  bestPartners: PartnerSummary[];
  worstEnemies: RivalSummary[];
}

export interface TeamLeaderboard {
  byElo: TeamStats[];
  byWinRate: TeamStats[];
  byWins: TeamStats[];
  byWinStreak: TeamStats[];
  mostActive: TeamStats[];
}

export interface PlayerLeaderboard {
  byElo: PlayerStatsResponse[];
  byWinRate: PlayerStatsResponse[];
  byWins: PlayerStatsResponse[];
  byWinStreak: PlayerStatsResponse[];
  mostActive: PlayerStatsResponse[];
  bestOffense: PlayerStatsResponse[];
  bestDefense: PlayerStatsResponse[];
}

export interface LeaderboardResponse {
  teams: TeamLeaderboard;
  players: PlayerLeaderboard;
}

/** Alias for LeaderboardResponse for cleaner naming in components */
export type Leaderboard = LeaderboardResponse;

/**
 * Alias for PlayerStatsResponse for cleaner naming in components
 */
export type PlayerStats = PlayerStatsResponse;

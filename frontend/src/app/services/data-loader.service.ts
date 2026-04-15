import { Injectable, inject, signal, computed } from '@angular/core';
import { FoosballApiService } from './foosball-api.service';
import { Player, Team, Match, TeamStats, PlayerStats, Leaderboard } from '../models/foosball.models';

@Injectable({
  providedIn: 'root'
})
export class DataLoaderService {
  private apiService = inject(FoosballApiService);

  // State signals
  players = signal<Player[]>([]);
  teams = signal<Team[]>([]);
  matches = signal<Match[]>([]);
  teamStats = signal<TeamStats[]>([]);
  playerStats = signal<PlayerStats[]>([]);
  leaderboard = signal<Leaderboard | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);

  todaysMatches = computed(() => {
    const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
    return this.matches().filter(m => m.matchDate === today);
  });

  recentMatches = computed(() => {
    return this.matches().slice(0, 5);
  });

  /**
   * Load all data (players, teams, recent matches with complete result data)
   * MatchResult objects from backend include:
   * - Player Elo changes (initial, change, new)
   * - Historical head-to-head records
   * - Winner information
   * - Team and player details
   */
  loadAllData() {
    this.loading.set(true);
    this.error.set(null);

    // Load players
    this.apiService.getAllPlayers().subscribe({
      next: (data) => this.players.set(data),
      error: (err) => {
        console.warn('Backend unavailable - players:', err.message);
        this.players.set([]);
        this.error.set('Cannot reach the backend. Make sure the server is running.');
        this.loading.set(false);
      }
    });

    // Load teams
    this.apiService.getAllTeams().subscribe({
      next: (data) => this.teams.set(data),
      error: (err) => {
        console.warn('Backend unavailable - teams:', err.message);
        this.teams.set([]);
      }
    });

    // Load recent matches with scores (includes historical head-to-head)
    this.loadRecentMatches();
  }

  /**
   * Reload recent matches with scores from backend
   * Call this after a new match is submitted to update the display
   */
  loadRecentMatches() {
    this.apiService.getRecentMatches(50).subscribe({
      next: (data) => this.matches.set(data),
      error: (err) => {
        console.warn('Backend unavailable - matches:', err.message);
        this.matches.set([]);
      },
      complete: () => this.loading.set(false)
    });
  }

  /**
   * Load all stats data (player pair stats, player stats, leaderboard)
   */
  loadStatsData() {
    // Load team stats
    this.apiService.getAllTeamStats().subscribe({
      next: (data) => this.teamStats.set(data),
      error: (err) => {
        console.warn('Backend unavailable - team stats:', err.message);
        this.teamStats.set([]);
      }
    });

    // Load player stats
    this.apiService.getAllPlayerStats().subscribe({
      next: (data) => this.playerStats.set(data),
      error: (err) => {
        console.warn('Backend unavailable - player stats:', err.message);
        this.playerStats.set([]);
      }
    });

    // Load leaderboard
    this.apiService.getLeaderboard().subscribe({
      next: (data) => this.leaderboard.set(data),
      error: (err) => {
        console.warn('Backend unavailable - leaderboard:', err.message);
        this.leaderboard.set(null);
      }
    });
  }
}

/**
 * Foosball API Service
 * Handles all HTTP requests to the backend API
 */

import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  Player,
  CreatePlayerRequest,
  UpdatePlayerRequest,
  Team,
  CreateTeamRequest,
  UpdateTeamRequest,
  Match,
  CreateMatchRequest,
  UpdateMatchRequest,
  TeamStats,
  PlayerStats,
  LeaderboardResponse,
  PageResponse,
} from '../models/foosball.models';

@Injectable({
  providedIn: 'root',
})
export class FoosballApiService {
  private http = inject(HttpClient);
  private apiUrl = '/api';

  // Service-level signal — single source of truth for all components
  players = signal<Player[]>([]);

  getAllPlayers(): Observable<Player[]> {
    return this.http.get<Player[]>(`${this.apiUrl}/players`).pipe(
      map(p => [...p].sort((a, b) => a.name.localeCompare(b.name)))
    );
  }

  getPlayersPage(search?: string, page = 0, size = 20): Observable<PageResponse<Player>> {
    let params = new HttpParams().set('paginate', 'true').set('page', page).set('size', size);
    if (search) params = params.set('search', search);
    return this.http.get<PageResponse<Player>>(`${this.apiUrl}/players`, { params });
  }

  loadPlayers(): void {
    this.getAllPlayers().subscribe(players => this.players.set(players));
  }

  // ============ PLAYER ENDPOINTS ============

  getPlayerById(id: number): Observable<Player> {
    return this.http.get<Player>(`${this.apiUrl}/players/${id}`);
  }

  getPlayerByName(name: string): Observable<Player> {
    return this.http.get<Player>(`${this.apiUrl}/players/name/${name}`);
  }

  createPlayer(request: CreatePlayerRequest): Observable<Player> {
    return new Observable(observer => {
      this.http.post<Player>(`${this.apiUrl}/players`, request).subscribe({
        next: (player) => { observer.next(player); observer.complete(); this.loadPlayers(); },
        error: (err) => observer.error(err)
      });
    });
  }

  updatePlayer(id: number, request: UpdatePlayerRequest): Observable<Player> {
    return this.http.put<Player>(`${this.apiUrl}/players/${id}`, request);
  }

  deletePlayer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/players/${id}`);
  }

  // ============ TEAM ENDPOINTS ============

  getAllTeams(): Observable<Team[]> {
    return this.http.get<Team[]>(`${this.apiUrl}/teams`);
  }

  getTeamsPage(search?: string, page = 0, size = 20): Observable<PageResponse<Team>> {
    let params = new HttpParams().set('paginate', 'true').set('page', page).set('size', size);
    if (search) params = params.set('search', search);
    return this.http.get<PageResponse<Team>>(`${this.apiUrl}/teams`, { params });
  }

  getTeamById(id: number): Observable<Team> {
    return this.http.get<Team>(`${this.apiUrl}/teams/${id}`);
  }

  createTeam(request: CreateTeamRequest): Observable<Team> {
    return this.http.post<Team>(`${this.apiUrl}/teams`, request);
  }

  updateTeam(id: number, request: UpdateTeamRequest): Observable<Team> {
    return this.http.put<Team>(`${this.apiUrl}/teams/${id}`, request);
  }

  deleteTeam(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/teams/${id}`);
  }

  // ============ MATCH ENDPOINTS ============

  getAllMatches(limit: number = 10): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.apiUrl}/matches?limit=${limit}`);
  }

  getRecentMatches(limit: number = 10): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.apiUrl}/matches/recent?limit=${limit}`);
  }

  createMatch(request: CreateMatchRequest): Observable<Match> {
    return this.http.post<Match>(`${this.apiUrl}/matches`, request);
  }

  submitMatch(request: CreateMatchRequest): Observable<Match> {
    return this.createMatch(request);
  }

  getMatchesByPlayer(playerId: number, limit = 20): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.apiUrl}/matches/player/${playerId}?limit=${limit}`);
  }

  getMatchesByTeam(player1Id: number, player2Id: number, limit = 20): Observable<Match[]> {
    return this.http.get<Match[]>(`${this.apiUrl}/matches/team/${player1Id}/${player2Id}?limit=${limit}`);
  }

  updateMatch(id: number, request: UpdateMatchRequest): Observable<Match> {
    return this.http.put<Match>(`${this.apiUrl}/matches/${id}`, request);
  }

  deleteMatch(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/matches/${id}`);
  }

  // ============ STATS ENDPOINTS ============

  getAllTeamStats(): Observable<TeamStats[]> {
    return this.http.get<TeamStats[]>(`${this.apiUrl}/stats/teams`);
  }

  getTeamStatsPage(search?: string, page = 0, size = 20): Observable<PageResponse<TeamStats>> {
    let params = new HttpParams().set('paginate', 'true').set('page', page).set('size', size);
    if (search) params = params.set('search', search);
    return this.http.get<PageResponse<TeamStats>>(`${this.apiUrl}/stats/teams`, { params });
  }

  getAllPlayerStats(): Observable<PlayerStats[]> {
    return this.http.get<PlayerStats[]>(`${this.apiUrl}/stats/players`);
  }

  getPlayerStatsPage(search?: string, page = 0, size = 20): Observable<PageResponse<PlayerStats>> {
    let params = new HttpParams().set('paginate', 'true').set('page', page).set('size', size);
    if (search) params = params.set('search', search);
    return this.http.get<PageResponse<PlayerStats>>(`${this.apiUrl}/stats/players`, { params });
  }

  getTeamStats(player1Id: number, player2Id: number): Observable<TeamStats> {
    return this.http.get<TeamStats>(`${this.apiUrl}/stats/teams/${player1Id}/${player2Id}`);
  }

  getTeamStatsById(id: number): Observable<TeamStats> {
    return this.http.get<TeamStats>(`${this.apiUrl}/stats/teams/${id}`);
  }

  getPlayerStats(playerId: number): Observable<PlayerStats> {
    return this.http.get<PlayerStats>(`${this.apiUrl}/stats/players/${playerId}`);
  }

  getLeaderboard(): Observable<LeaderboardResponse> {
    return this.http.get<LeaderboardResponse>(`${this.apiUrl}/stats/leaderboard`);
  }
}

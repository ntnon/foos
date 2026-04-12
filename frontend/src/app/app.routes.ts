import { Routes } from '@angular/router';
import { MatchEntryComponent } from './pages/landing-page/match-entry/match-entry.component';
import { PlayerComponent } from './pages/stats/player.component';
import { PlayerListComponent } from './pages/stats/players.component';
import { TeamComponent } from './pages/stats/team.component';
import { TeamListComponent } from './pages/stats/teams.component';
import { LeaderboardComponent } from './pages/leaderboard/leaderboard.component';
import { MatchesComponent } from './pages/matches/matches.component';
import { LandingPageComponent } from './pages/landing-page/landing-page.component';

export const routes: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'match-entry', component: MatchEntryComponent },
  { path: 'stats/players', component: PlayerListComponent },
  { path: 'stats/players/:id', component: PlayerComponent },
  { path: 'stats/teams', component: TeamListComponent },
  { path: 'stats/teams/:id', component: TeamComponent },
  { path: 'stats/leaderboard', component: LeaderboardComponent },
  { path: 'matches', component: MatchesComponent },
  { path: '**', redirectTo: '' },
];

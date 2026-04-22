import { Routes } from '@angular/router';
import { MatchEntryComponent } from './components/data-entry/match-entry.component';
import { PlayerComponent } from './components/player/player-profile.component';
import { PlayerListComponent } from './components/player/player-table.component';
import { TeamProfileComponent } from './components/team/team-profile.component';
import { TeamListComponent } from './components/team/team-table.component';
import { LeaderboardComponent } from './pages/leaderboard/leaderboard.component';
import { MatchesComponent } from './pages/matches/matches.component';
import { LandingPageComponent } from './pages/landing-page/landing-page.component';

export const routes: Routes = [
  { path: '', component: LandingPageComponent },
  { path: 'match-entry', component: MatchEntryComponent },
  { path: 'stats/players', component: PlayerListComponent },
  { path: 'stats/players/:id', component: PlayerComponent },
  { path: 'stats/teams', component: TeamListComponent },
  { path: 'stats/teams/:id', component: TeamProfileComponent },
  { path: 'stats/leaderboard', component: LeaderboardComponent },
  { path: 'matches', component: MatchesComponent },
  { path: '**', redirectTo: '' },
];

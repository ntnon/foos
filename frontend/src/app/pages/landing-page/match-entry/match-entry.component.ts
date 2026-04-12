import {Component, signal, computed, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FoosballApiService } from '../../../services/foosball-api.service';
import { DataLoaderService } from '../../../services/data-loader.service';
import {form, required} from '@angular/forms/signals';
import {TeamComponent} from './team-entry.component';
import { ScoreEntryComponent } from './score-entry.component';
import { validate, submit } from '@angular/forms/signals';
import { TeamColor } from '../../../models/foosball.models';
import { SoccerFieldComponent } from '../../../components/soccer-field.component';

@Component({
  selector: 'match-entry',
  imports: [CommonModule, TeamComponent, ScoreEntryComponent, SoccerFieldComponent],
  template: `
    <form (submit)="onSubmit($event)">
      <div class="match-width-container">

        <div class="field-wrapper">
          <soccer-field />
          <div class="match-entry-container flex flex-col space-y-10">

            <div class="team-section flex flex-col gap-10 rounded-lg py-4 px-10 border-l-[30px] border-r-[30px] border-l-brand-600 border-r-brand-600 bg-brand-100"
                 [class.winner]="winner() === 'BLUE' && !isSubmitting()"
                 [class.submitting-blue]="isSubmitting()"
                 [class.entering-blue]="isResetting()"
                 [class.offscreen]="!isSubmitting() && !isResetting() && isOffscreen()">
              <team-component [teamColor]="'BLUE'" [teamForm]="matchForm.teamBlue"/>
              <score-tracker [teamColor]="'BLUE'" [formField]="matchForm.team1GameScore" [targetScore]="targetScore()"/>
            </div>

            <div class="team-section flex flex-col gap-10 rounded-lg py-4 px-10 border-l-[30px] border-r-[30px] border-l-team-red-600 border-r-team-red-600 bg-team-red-100"
                 [class.winner]="winner() === 'RED' && !isSubmitting() "
                 [class.submitting-red]="isSubmitting()"
                 [class.entering-red]="isResetting()"
                 [class.offscreen]="!isSubmitting() && !isResetting() && isOffscreen()">
              <score-tracker [teamColor]="'RED'" [formField]="matchForm.team2GameScore" [targetScore]="targetScore()"/>
              <team-component [teamColor]="'RED'" [teamForm]="matchForm.teamRed"/>
            </div>

          </div>
        </div>

        <!-- Errors + Submit — outside the field, same width -->
        <div class="flex flex-col items-center gap-2 mt-3">
          @for (error of matchForm().errors(); track error) {
            <span class="text-sm">❌{{ error.message }}</span>
          }
          @for (error of matchForm.team1GameScore().errors(); track error) {
            <span class="text-sm">❌{{ error.message }}</span>
          }
          @for (error of matchForm.team2GameScore().errors(); track error) {
            <span class="text-sm">❌{{ error.message }}</span>
          }
          @if (apiError()) {
            <span class="text-sm">❌{{ apiError() }}</span>
          }

          <button
            type="submit"
            [disabled]="isSubmitting()"
            class="w-full py-3 mt-8 h-15 bg-amber-500 text-white font-extrabold rounded-md shadow-lg hover:bg-amber-700 active:scale-95 transition-all duration-150 cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed disabled:active:scale-100 text-lg tracking-wide"
          >
            @if (isSubmitting()) {
              <span class="inline-flex items-center justify-center gap-2">
                <svg class="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
                </svg>
                Submitting...
              </span>
            } @else {
              ⚽ Submit Match
            }
          </button>
        </div>

      </div>
    </form>
  `,
  styles: [`
    .match-width-container {
      max-width: 800px;
      margin: 0 auto;
      padding: 0 16px;
    }

    .field-wrapper {
      position: relative;
      border-radius: 6px;
      overflow: hidden;
      padding-top: 30px;
      padding-bottom: 30px;
      padding-left: 30px;
      padding-right: 30px;
      box-shadow: 0 8px 40px rgba(0,0,0,0.35), 0 0 0 6px #2a6b20;
    }

    .match-entry-container {
      position: relative;
      z-index: 1;
      border-radius: 12px;
      padding: 16px;
      background: transparent;
      border: none;
      gap: 12px;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .team-section {
      transition: transform 0.5s ease-in, opacity 0.5s ease-in;
    }

    .team-section.submitting-blue { animation: slideOutTop    0.9s ease-in  forwards !important; }
    .team-section.submitting-red  { animation: slideOutBottom 0.9s ease-in  forwards !important; }
    .team-section.entering-blue   { animation: slideInFromTop    1.0s ease-out forwards !important; }
    .team-section.entering-red    { animation: slideInFromBottom 1.0s ease-out forwards !important; }
    .team-section.offscreen       { opacity: 0 !important; transform: translateY(-140%) !important; animation: none !important; transition: none !important; }

    @keyframes slideInFromTop    { 0% { transform: translateY(-140%); opacity: 0; } 100% { transform: translateY(0); opacity: 1; } }
    @keyframes slideInFromBottom { 0% { transform: translateY( 140%); opacity: 0; } 100% { transform: translateY(0); opacity: 1; } }
    @keyframes slideOutTop       { 0% { transform: translateY(0);     opacity: 1; } 100% { transform: translateY(-140%); opacity: 0; } }
    @keyframes slideOutBottom    { 0% { transform: translateY(0);     opacity: 1; } 100% { transform: translateY( 140%); opacity: 0; } }

    .team-section.winner {
      animation: winnerPulse 1s ease-in-out infinite alternate;
      border-left-color: gold !important;
      border-right-color: gold !important;
    }
    @keyframes winnerPulse {
      from { box-shadow: 0 0 8px 2px gold; transform: scale(1); }
      to   { box-shadow: 0 0 24px 8px gold; transform: scale(1.02); }
    }

    .team-section {
      transition: transform 0.5s ease-in, opacity 0.5s ease-in;
    }
  `]
})
export class MatchEntryComponent {
  apiService = inject(FoosballApiService);
  dataLoaderService = inject(DataLoaderService);
  hasSubmitted = signal(false);
  isSubmitting = signal(false);
  isResetting = signal(false);
  isOffscreen = signal(false);
  apiError = signal<string | null>(null);
  apiSuccess = signal(false);

  targetScore = signal<number>(10);

  winner = computed(() => {
    if(this.matchForm().invalid()) return null;
    const v = this.matchModel();
    if (v.team1GameScore === this.targetScore() && v.team2GameScore === this.targetScore())
      return null;
    if (v.team1GameScore === this.targetScore()) return 'BLUE';
    if (v.team2GameScore === this.targetScore()) return 'RED';
    return null;
    }
  )

  matchModel = signal({
    teamBlue: {offense: '', defense: '', teamColor: 'BLUE'},
    teamRed: {offense: '', defense: '', teamColor: 'RED'},
    team1GameScore: 0,
    team2GameScore: 0,
  });

  matchForm = form(this.matchModel, (s) => {
    required(s.team1GameScore, {message: 'Team 1 score is required'});
    required(s.team2GameScore, {message: 'Team 2 score is required'});
    required(s.teamBlue.offense, {message: 'Team 1 Player 1 is required'});
    required(s.teamBlue.defense, {message: 'Team 1 Player 2 is required'});
    required(s.teamRed.offense, {message: 'Team 2 Player 1 is required'});
    required(s.teamRed.defense, {message: 'Team 2 Player 2 is required'});
    validate(s.teamBlue, ({value}) => {
      if (value().offense && value().offense === value().defense)
        return {kind: 'duplicate', message: 'Team 1 players cannot be the same'};
      return null;
    });
    validate(s.teamRed, ({value}) => {
      if (value().offense && value().offense === value().defense)
        return {kind: 'duplicate', message: 'Team 2 players cannot be the same'};
      return null;
    });
    validate(s, ({value}) => {
      const v = value();
      if (v.team1GameScore === this.targetScore() && v.team2GameScore === this.targetScore())
        return {kind: 'tie', message: 'There can be only one winner'};
      return null;
    });
    validate(s.team1GameScore, ({value}) => {
      if (value() > this.targetScore())
        return {kind: 'overScore', message: 'Score cannot exceed 10'};
      return null;
    });
    validate(s.team2GameScore, ({value}) => {
      if (value() > this.targetScore())
        return {kind: 'overScore', message: 'Score cannot exceed 10'};
      return null;
    });
    validate(s, () => {
      if (!this.hasSubmitted()) return null;
      if(!this.winner())
        return {kind: 'dnf', message: 'There must be exactly one winner'};
      return null;
    });
    validate(s, ({valueOf}) => {
      const allPlayers = [valueOf(s.teamBlue.offense), valueOf(s.teamBlue.defense), valueOf(s.teamRed.offense), valueOf(s.teamRed.defense)]
      const uniquePlayers = new Set(allPlayers)
      if (allPlayers.length !== uniquePlayers.size && !uniquePlayers.has(''))
        return {kind: 'duplicate', message: 'A player can only fill one spot'};
      return null;
    });
  });



  onSubmit(event: Event) {
    event.preventDefault();
    this.hasSubmitted.set(true);
    if (!this.matchForm().valid()) return;
    this.isSubmitting.set(true);
    this.apiError.set(null);

    submit(this.matchForm, async () => {
      try {
        const v = this.matchModel();
        const matchRequest = {
          team1: {
            offense: Number(v.teamBlue.offense),
            defense: Number(v.teamBlue.defense),
            teamColor: 'BLUE' as TeamColor
          },
          team2: {
            offense: Number(v.teamRed.offense),
            defense: Number(v.teamRed.defense),
            teamColor: 'RED' as TeamColor
          },
          team1GameScore: v.team1GameScore,
          team2GameScore: v.team2GameScore,
        };

        const result = await this.apiService.submitMatch(matchRequest).toPromise();

        if (result) {
          this.dataLoaderService.loadRecentMatches();

          // 1. Let slide-OUT animation play
          await new Promise(resolve => setTimeout(resolve, 900));

          // 2. Go offscreen instantly — no flash between animations
          this.isSubmitting.set(false);
          this.isOffscreen.set(true);

          // 3. Reset form while completely hidden
          this.matchModel.set({
            teamBlue: { offense: '', defense: '', teamColor: 'BLUE' as TeamColor },
            teamRed: { offense: '', defense: '', teamColor: 'RED' as TeamColor },
            team1GameScore: 0,
            team2GameScore: 0,
          });
          this.hasSubmitted.set(false);

          // 4. Wait one frame so Angular renders the reset state while still hidden
          await new Promise(resolve => requestAnimationFrame(resolve));
          await new Promise(resolve => requestAnimationFrame(resolve));

          // 5. Slide IN — remove offscreen and start entering animation simultaneously
          this.isOffscreen.set(false);
          this.isResetting.set(true);
          await new Promise(resolve => setTimeout(resolve, 1000));
          this.isResetting.set(false);
          this.apiSuccess.set(false);
        }
      } catch (error: any) {
        this.isSubmitting.set(false);
        this.apiError.set(
          error?.error?.message ||
          error?.message ||
          'Failed to submit match. Please try again.'
        );
        await new Promise(resolve => setTimeout(resolve, 3000));
        this.apiError.set(null);
      }
    });
  }



}

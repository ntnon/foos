import {Component, signal, computed, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FoosballApiService } from '../../services/foosball-api.service';
import { DataLoaderService } from '../../services/data-loader.service';
import {form, required} from '@angular/forms/signals';
import {TeamEntryComponent} from './team-entry.component';
import { ScoreEntryComponent } from './score-entry.component';
import { validate, submit } from '@angular/forms/signals';
import { TeamColor } from '../../models/foosball.models';
import { SoccerFieldComponent } from '../../components/soccer-field.component';
import { CreateMatchRequest, MatchFormModel } from '../../models/foosball.models';


@Component({
  selector: 'match-entry',
  imports: [CommonModule, TeamEntryComponent, ScoreEntryComponent, SoccerFieldComponent],
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
              <team-entry [teamColor]="'BLUE'" [teamForm]="matchForm.team1"/>
              <score-tracker [teamColor]="'BLUE'" [formField]="matchForm.team1GameScore" [targetScore]="targetScore()"/>
            </div>

            <div class="team-section flex flex-col gap-10 rounded-lg py-4 px-10 border-l-[30px] border-r-[30px] border-l-team-red-600 border-r-team-red-600 bg-team-red-100"
                 [class.winner]="winner() === 'RED' && !isSubmitting() "
                 [class.submitting-red]="isSubmitting()"
                 [class.entering-red]="isResetting()"
                 [class.offscreen]="!isSubmitting() && !isResetting() && isOffscreen()">
              <score-tracker [teamColor]="'RED'" [formField]="matchForm.team2GameScore" [targetScore]="targetScore()"/>
              <team-entry [teamColor]="'RED'" [teamForm]="matchForm.team2"/>
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
                Sender inn...
              </span>
            } @else {
              ⚽ Send inn kamp
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
    const v = this.matchModel();
    const target = this.targetScore();
    const blueWon = v.team1GameScore === target;
    const redWon = v.team2GameScore === target;

    if (blueWon === redWon) return null;
    return blueWon ? 'BLUE' : 'RED';
  });

  matchModel = signal<MatchFormModel>({
    team1: {offense: '', defense: '', teamColor: 'BLUE'},
    team2: {offense: '', defense: '', teamColor: 'RED'},
    team1GameScore: 0,
    team2GameScore: 0,
  });

  matchForm = form(this.matchModel, (s) => {
    required(s.team1.offense, {message: 'Spiller 1 på lag 1 er påkrevd'});
    required(s.team1.defense, {message: 'Spiller 2 på lag 1 er påkrevd'});
    required(s.team2.offense, {message: 'Spiller 1 på lag 2 er påkrevd'});
    required(s.team2.defense, { message: 'Spiller 2 på lag 2 er påkrevd' });

    // validators
    validate(s.team1, ({value}) => {
      if (value().offense && value().offense === value().defense)
        return {kind: 'duplicate', message: 'Spillerne på lag 1 kan ikke være like'};
      return null;
    });
    validate(s.team2, ({value}) => {
      if (value().offense && value().offense === value().defense)
        return {kind: 'duplicate', message: 'Spillerne på lag 2 kan ikke være like'};
      return null;
    });
    validate(s, ({value}) => {
      const v = value();
      if (v.team1GameScore === this.targetScore() && v.team2GameScore === this.targetScore())
        return {kind: 'tie', message: 'Det kan bare være én vinner'};
      return null;
    });
    validate(s.team1GameScore, ({value}) => {
      if (value() > this.targetScore())
        return {kind: 'overScore', message: 'Poengsummen kan ikke overstige 10'};
      return null;
    });
    validate(s.team2GameScore, ({value}) => {
      if (value() > this.targetScore())
        return {kind: 'overScore', message: 'Poengsummen kan ikke overstige 10'};
      return null;
    });
    validate(s, () => {
      if (!this.hasSubmitted()) return null;
      if(!this.winner())
        return {kind: 'dnf', message: 'Det må være nøyaktig én vinner'};
      return null;
    });
    validate(s, ({valueOf}) => {
      const allPlayers = [valueOf(s.team1.offense), valueOf(s.team1.defense), valueOf(s.team2.offense), valueOf(s.team2.defense)]
      const uniquePlayers = new Set(allPlayers)
      if (allPlayers.length !== uniquePlayers.size && !uniquePlayers.has(''))
        return {kind: 'duplicate', message: 'En spiller kan bare ha én plass'};
      return null;
    });
  });

  m = this.matchForm.team1GameScore



  onSubmit(event: Event) {
    event.preventDefault();
    this.hasSubmitted.set(true);
    if (!this.matchForm().valid()) return;
    this.isSubmitting.set(true);
    this.apiError.set(null);

    submit(this.matchForm, async () => {
      try {
        const v = this.matchModel();
        const matchRequest: CreateMatchRequest = {
          team1: {
            offense: Number(v.team1.offense),
            defense: Number(v.team1.defense),
            teamColor: 'BLUE' as TeamColor
          },
          team2: {
            offense: Number(v.team2.offense),
            defense: Number(v.team2.defense),
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
            team1: { offense: '', defense: '', teamColor: 'BLUE' as TeamColor },
            team2: { offense: '', defense: '', teamColor: 'RED' as TeamColor },
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
          'Kunne ikke sende inn kampen. Prøv igjen.'
        );
        await new Promise(resolve => setTimeout(resolve, 3000));
        this.apiError.set(null);
      }
    });
  }



}

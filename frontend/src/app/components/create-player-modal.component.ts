import { Component, inject, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FoosballApiService } from '../services/foosball-api.service';
import { Player } from '../models/foosball.models';

@Component({
  selector: 'create-player-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    @if (isOpen()) {
      <div class="modal-overlay" (click)="close()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Create New Player</h2>
            <button class="close-btn" (click)="close()">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label for="playerName">Player Name</label>
              <input
                id="playerName"
                type="text"
                [(ngModel)]="playerName"
                (keyup.enter)="createPlayer()"
                placeholder="Enter player name"
                class="input-field"
              />
            </div>
            @if (error()) {
              <div class="error-message">{{ error() }}</div>
            }
            @if (success()) {
              <div class="success-message">Player created successfully!</div>
            }
          </div>
          <div class="modal-footer">
            <button
              class="btn btn-cancel"
              (click)="close()"
              [disabled]="isLoading()"
            >
              Cancel
            </button>
            <button
              class="btn btn-primary"
              (click)="createPlayer()"
              [disabled]="!playerName.trim() || isLoading()"
            >
              {{ isLoading() ? 'Creating...' : 'Create Player' }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    .modal-content {
      background: white;
      border-radius: 8px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      width: 90%;
      max-width: 400px;
      animation: slideUp 0.3s ease-out;
    }

    @keyframes slideUp {
      from {
        transform: translateY(20px);
        opacity: 0;
      }
      to {
        transform: translateY(0);
        opacity: 1;
      }
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #eee;
    }

    .modal-header h2 {
      margin: 0;
      font-size: 1.3em;
      color: #333;
    }

    .close-btn {
      background: none;
      border: none;
      font-size: 1.5em;
      cursor: pointer;
      color: #999;
      padding: 0;
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .close-btn:hover {
      color: #333;
    }

    .modal-body {
      padding: 20px;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-bottom: 16px;
    }

    label {
      font-weight: 600;
      font-size: 0.9em;
      color: #666;
    }

    .input-field {
      padding: 10px 12px;
      border: 2px solid #ddd;
      border-radius: 6px;
      font-size: 1em;
      transition: border-color 0.2s ease;
    }

    .input-field:focus {
      outline: none;
      border-color: #2196f3;
    }

    .error-message {
      padding: 10px 12px;
      background-color: #ffebee;
      border-left: 4px solid #f44336;
      color: #c62828;
      border-radius: 4px;
      margin-bottom: 12px;
    }

    .success-message {
      padding: 10px 12px;
      background-color: #e8f5e9;
      border-left: 4px solid #4caf50;
      color: #2e7d32;
      border-radius: 4px;
      margin-bottom: 12px;
    }

    .modal-footer {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
      padding: 20px;
      border-top: 1px solid #eee;
    }

    .btn {
      padding: 10px 20px;
      border: none;
      border-radius: 6px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .btn-cancel {
      background-color: #f5f5f5;
      color: #333;
    }

    .btn-cancel:hover:not(:disabled) {
      background-color: #e0e0e0;
    }

    .btn-primary {
      background-color: #2196f3;
      color: white;
    }

    .btn-primary:hover:not(:disabled) {
      background-color: #1976d2;
    }
  `]
})
export class CreatePlayerModalComponent {
  private apiService = inject(FoosballApiService);

  playerCreated = output<Player>();

  isOpen = signal(false);
  playerName = '';
  isLoading = signal(false);
  error = signal<string | null>(null);
  success = signal(false);

  open(): void {
    this.isOpen.set(true);
    this.playerName = '';
    this.error.set(null);
    this.success.set(false);
  }

  close(): void {
    this.isOpen.set(false);
    this.playerName = '';
    this.error.set(null);
  }

  createPlayer(): void {
    const name = this.playerName.trim();
    if (!name) {
      this.error.set('Player name cannot be empty');
      return;
    }

    this.isLoading.set(true);
    this.error.set(null);

    this.apiService.createPlayer({name: name}).subscribe({
      next: (player: Player) => {
        this.success.set(true);
        this.playerCreated.emit(player);
        setTimeout(() => {
          this.close();
          this.isLoading.set(false);
        }, 800);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.error.set(err.error?.message || 'Failed to create player');
      }
    });
  }
}

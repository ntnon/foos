import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { DataLoaderService } from './services/data-loader.service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: 'app.html',
  styleUrl: 'app.css',
  standalone: true,
  imports: [CommonModule, HttpClientModule, RouterModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App implements OnInit {
  private dataLoaderService = inject(DataLoaderService);

  players = this.dataLoaderService.players;
  matches = this.dataLoaderService.matches;
  error = this.dataLoaderService.error;

  ngOnInit() {
    this.dataLoaderService.loadAllData();
  }

  dismissError() {
    this.dataLoaderService.error.set(null);
  }
}

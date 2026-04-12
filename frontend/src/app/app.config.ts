import { ApplicationConfig, provideAppInitializer, provideBrowserGlobalErrorListeners, inject, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app.routes';
import { FoosballApiService } from './services/foosball-api.service';
import { firstValueFrom } from 'rxjs';
import { Player } from './models/foosball.models';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(),
    provideAppInitializer(() => {
      const api = inject(FoosballApiService);
      return firstValueFrom(api.getAllPlayers()).then((players: Player[]) => {
        api.players.set([...players].sort((a, b) => a.name.localeCompare(b.name)));
      });
    })
  ]
};

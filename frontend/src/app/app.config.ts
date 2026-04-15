import { ApplicationConfig, provideAppInitializer, provideBrowserGlobalErrorListeners, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { FoosballApiService } from './services/foosball-api.service';
import { backendErrorInterceptor } from './interceptors/backend-error.interceptor';
import { firstValueFrom } from 'rxjs';
import { Player } from './models/foosball.models';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([backendErrorInterceptor])),
    provideAppInitializer(() => {
      const api = inject(FoosballApiService);
      return firstValueFrom(api.getAllPlayers())
        .then((players: Player[]) => {
          api.players.set([...players].sort((a, b) => a.name.localeCompare(b.name)));
        })
        .catch(() => {}); // backend down — interceptor handles the banner
    })
  ]
};

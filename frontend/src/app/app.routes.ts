import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'leaderboard' },
  { path: 'leaderboard', title: 'Рейтинг · quotelab', loadComponent: () => import('./features/leaderboard/leaderboard.page').then((m) => m.LeaderboardPage) },
  { path: 'instruments', pathMatch: 'full', redirectTo: 'instruments/IMOEX' },
  { path: 'instruments/:secid', title: 'Инструмент · quotelab', loadComponent: () => import('./features/instrument/instrument.page').then((m) => m.InstrumentPage) },
  { path: 'strategies/:id', title: 'Стратегия · quotelab', loadComponent: () => import('./features/strategy/strategy.page').then((m) => m.StrategyPage) },
  { path: 'forecasts', title: 'Журнал · quotelab', loadComponent: () => import('./features/forecasts/forecasts.page').then((m) => m.ForecastsPage) },
  { path: 'guide', title: 'Справка · quotelab', loadComponent: () => import('./features/guide/guide.page').then((m) => m.GuidePage) },
  { path: '**', redirectTo: 'leaderboard' },
];

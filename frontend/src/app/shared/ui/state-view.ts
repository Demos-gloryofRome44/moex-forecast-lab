import { Component, computed, input, output } from '@angular/core';
import { toErrorResponse } from '../../core/api/api-error';

@Component({
  selector: 'app-state',
  template: `
    @if (error()) {
      <div class="state" [class.compact]="compact()" role="alert">
        <h2>{{ errorTitle() }}</h2>
        <p>{{ problem().error }}</p>
        @if (problem().details?.length) {
          <div class="details">
            @for (d of problem().details; track $index) {
              <div>{{ d }}</div>
            }
          </div>
        }
        <button type="button" class="btn btn-secondary" (click)="retry.emit()">Повторить</button>
      </div>
    } @else if (loading() && empty()) {
      <div class="skeleton" [class.short]="compact()" aria-busy="true"></div>
    } @else if (empty()) {
      <div class="state" [class.compact]="compact()">
        <h2>{{ emptyTitle() }}</h2>
        <p>{{ emptyText() }}</p>
      </div>
    } @else {
      <ng-content />
    }
  `,
})
export class StateView {
  readonly loading = input(false);
  readonly error = input<unknown>(undefined);
  readonly empty = input(false);
  readonly compact = input(false);
  readonly errorTitle = input('Не удалось загрузить данные');
  readonly emptyTitle = input('Пока пусто');
  readonly emptyText = input('Данные появятся, когда сервис их соберёт.');
  readonly retry = output<void>();

  protected readonly problem = computed(() => toErrorResponse(this.error()));
}

import { HttpErrorResponse } from '@angular/common/http';
import { ErrorResponse } from './models';

export function toErrorResponse(error: unknown): ErrorResponse {
  if (error instanceof HttpErrorResponse) {
    const body = error.error as Partial<ErrorResponse> | null;
    if (body && typeof body.error === 'string') {
      return { error: body.error, details: body.details ?? [] };
    }
    if (error.status === 0) {
      return { error: 'Backend недоступен', details: ['Проверьте, что сервис запущен на localhost:8080'] };
    }
    return { error: `HTTP ${error.status}`, details: [error.message] };
  }
  return { error: error instanceof Error ? error.message : 'Неизвестная ошибка', details: [] };
}

import { Pipe, PipeTransform } from '@angular/core';
import { int, num, pct, pp, shortDate, signedPct } from './format';

@Pipe({ name: 'pct' })
export class PctPipe implements PipeTransform {
  transform = pct;
}

@Pipe({ name: 'signedPct' })
export class SignedPctPipe implements PipeTransform {
  transform = signedPct;
}

@Pipe({ name: 'num' })
export class NumPipe implements PipeTransform {
  transform(value: number | null | undefined, digits: 2 | 3 = 2): string {
    return num(value, digits);
  }
}

@Pipe({ name: 'int' })
export class IntPipe implements PipeTransform {
  transform = int;
}

@Pipe({ name: 'pp' })
export class PpPipe implements PipeTransform {
  transform = pp;
}

@Pipe({ name: 'ruDate' })
export class RuDatePipe implements PipeTransform {
  transform = shortDate;
}

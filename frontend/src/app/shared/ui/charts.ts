import {
  CategoryScale,
  Chart,
  Filler,
  LineController,
  LineElement,
  LinearScale,
  Plugin,
  PointElement,
  ScatterController,
  Tooltip,
} from 'chart.js';

Chart.register(LineController, ScatterController, LineElement, PointElement, LinearScale, CategoryScale, Tooltip, Filler);

export { Chart };
export type { Plugin };

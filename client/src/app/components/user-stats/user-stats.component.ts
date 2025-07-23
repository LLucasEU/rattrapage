import { Component, OnInit } from '@angular/core';
import { ChartOptions } from 'chart.js';
import { MovieService, UserMovieList } from '../../services/movie.service';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

@Component({
  selector: 'app-user-stats',
  templateUrl: './user-stats.component.html',
  styleUrls: ['./user-stats.component.scss']
})
export class UserStatsComponent implements OnInit {
  selectedPeriod: string = 'all';
  selectedGenre: string = 'all';
  selectedRating: string = 'all';

  periods = [
    { value: 'all', label: 'Tout le temps' },
    { value: 'year', label: 'Cette année' },
    { value: 'month', label: 'Ce mois-ci' },
  ];

  genres: string[] = [];
  ratings = [
    { value: 'all', label: 'Toutes notes' },
    { value: '8+', label: '8 et plus' },
    { value: '5-7', label: '5 à 7' },
    { value: '1-4', label: '1 à 4' },
  ];

  genreLabels: string[] = [];
  genreData: number[] = [];

  moisLabels: string[] = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin', 'Juil', 'Août', 'Sep', 'Oct', 'Nov', 'Déc'];
  moisData: number[] = [2, 1, 3, 2, 4, 5, 3, 2, 1, 2, 1, 1];

  pieChartData = {
    labels: this.genreLabels,
    datasets: [{ data: this.genreData }]
  };

  barChartData = {
    labels: this.moisLabels,
    datasets: [{ data: this.moisData, label: 'Films vus' }]
  };

  pieChartOptions: ChartOptions<'pie'> = {
    responsive: true,
    plugins: {
      legend: { position: 'top' },
      title: { display: true, text: 'Répartition par genre' }
    }
  };
  pieChartType = 'pie';

  barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    plugins: {
      legend: { display: false },
      title: { display: true, text: 'Films vus par mois' }
    }
  };
  barChartType = 'bar';

  allWatched: UserMovieList[] = [];

  pieColors: string[] = [
    '#1976d2', '#388e3c', '#fbc02d', '#d32f2f', '#7b1fa2', '#0288d1', '#c2185b', '#ffa000', '#388e3c', '#f57c00',
    '#455a64', '#0097a7', '#cddc39', '#8bc34a', '#e91e63', '#ff5722', '#607d8b', '#9c27b0', '#00bcd4', '#ff9800',
    '#3f51b5', '#4caf50', '#b71c1c', '#f44336', '#ffeb3b', '#00e676', '#bdbdbd', '#5d4037', '#ffb300', '#1de9b6',
    '#d500f9', '#c51162', '#00bfae', '#aeea00', '#ffd600', '#ff6d00', '#263238', '#b0bec5', '#ff1744', '#00e5ff'
  ];

  colorblindPieColors: string[] = [
    '#0066cc', '#ff6600', '#9900cc', '#009900', '#cc0000', '#ffcc00', '#0099cc', '#ff9900', '#6600cc', '#00cc66',
    '#cc6600', '#0066ff', '#cc0066', '#66cc00', '#cc00cc', '#00cc99', '#cc9900', '#0066cc', '#cc6600', '#66cc66',
    '#cc0066', '#00cc66', '#cc6600', '#0066cc', '#cc0066', '#66cc00', '#cc00cc', '#00cc99', '#cc9900', '#0066cc',
    '#cc6600', '#66cc66', '#cc0066', '#00cc66', '#cc6600', '#0066cc', '#cc0066', '#66cc00', '#cc00cc', '#00cc99'
  ];

  exportingPDF = false;

  constructor(private movieService: MovieService) {}

  ngOnInit(): void {
    this.loadUserWatchedMovies();
    this.updateChartColors();
  }

  isColorBlindMode(): boolean {
    return document.body.classList.contains('color-blind');
  }

  getPieColors(): string[] {
    return this.isColorBlindMode() ? this.colorblindPieColors : this.pieColors;
  }

  updateChartColors(): void {
    if (this.genreLabels.length > 0) {
      this.pieChartData = {
        labels: this.genreLabels,
        datasets: [{
          data: this.genreData,
          // @ts-ignore: Chart.js accepte backgroundColor dans le dataset
          backgroundColor: this.genreLabels.map((_, i) => this.getPieColors()[i % this.getPieColors().length])
        }]
      };
    }

    if (this.moisData.length > 0) {
      const barColor = this.isColorBlindMode() ? '#0066cc' : '#1976d2';
      this.barChartData = {
        labels: this.moisLabels,
        datasets: [{
          data: this.moisData,
          label: 'Films vus',
          // @ts-ignore: Chart.js accepte backgroundColor dans le dataset
          backgroundColor: barColor,
          borderColor: barColor,
          borderWidth: 1
        }]
      };
    }
  }

  loadUserWatchedMovies(): void {
    this.movieService.getUserWatchedMovies().subscribe({
      next: (res) => {
        this.allWatched = res.watched;
        this.updateStats();
        const genreSet = new Set<string>();
        this.allWatched.forEach(entry => (entry.movie.genres || []).forEach(g => genreSet.add(g)));
        this.genres = Array.from(genreSet).sort();
      }
    });
  }

  updateStats(): void {
    let filtered = this.allWatched;
    const now = new Date();
    if (this.selectedPeriod === 'year') {
      filtered = filtered.filter(entry => entry.watchedDate && new Date(entry.watchedDate).getFullYear() === now.getFullYear());
    } else if (this.selectedPeriod === 'month') {
      filtered = filtered.filter(entry => {
        if (!entry.watchedDate) return false;
        const d = new Date(entry.watchedDate);
        return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth();
      });
    }
    if (this.selectedGenre !== 'all') {
      filtered = filtered.filter(entry => (entry.movie.genres || []).includes(this.selectedGenre));
    }
    if (this.selectedRating === '8+') {
      filtered = filtered.filter(entry => (entry.userRating || 0) >= 8);
    } else if (this.selectedRating === '5-7') {
      filtered = filtered.filter(entry => (entry.userRating || 0) >= 5 && (entry.userRating || 0) <= 7);
    } else if (this.selectedRating === '1-4') {
      filtered = filtered.filter(entry => (entry.userRating || 0) >= 1 && (entry.userRating || 0) <= 4);
    }
    const genreCount: { [genre: string]: number } = {};
    filtered.forEach(entry => {
      (entry.movie.genres || []).forEach(genre => {
        genreCount[genre] = (genreCount[genre] || 0) + 1;
      });
    });
    this.genreLabels = Object.keys(genreCount);
    this.genreData = Object.values(genreCount);
    const moisCount: number[] = Array(12).fill(0);
    filtered.forEach(entry => {
      if (entry.watchedDate) {
        const date = new Date(entry.watchedDate);
        const month = date.getMonth();
        moisCount[month]++;
      }
    });

    this.moisData = moisCount;

    this.updateChartColors();
  }

  onFilterChange(): void {
    this.updateStats();
  }

  clearFilters(): void {
    this.selectedPeriod = 'all';
    this.selectedGenre = 'all';
    this.selectedRating = 'all';
    this.updateStats();
  }

  exportPDF(): void {
    this.exportingPDF = true;
    const chartsSection = document.querySelector('.charts-section') as HTMLElement;
    if (!chartsSection) { this.exportingPDF = false; return; }
    html2canvas(chartsSection, { scale: 2 }).then((canvas: HTMLCanvasElement) => {
      const imgData = canvas.toDataURL('image/png');
      const pdf = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' });
      const pageWidth = pdf.internal.pageSize.getWidth();
      const imgWidth = pageWidth - 40;
      const imgHeight = canvas.height * (imgWidth / canvas.width);
      pdf.text('Statistiques de mes films', 40, 40);
      pdf.addImage(imgData, 'PNG', 20, 60, imgWidth, imgHeight);
      pdf.save('statistiques-films.pdf');
      this.exportingPDF = false;
    }).catch(() => { this.exportingPDF = false; });
  }
}

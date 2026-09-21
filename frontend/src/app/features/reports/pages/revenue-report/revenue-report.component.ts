import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ReportService } from '../../services/report.service';
import { WeeklyRevenueReport } from '../../models/report.model';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-revenue-report',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './revenue-report.component.html',
  styleUrls: ['./revenue-report.component.css']
})
export class RevenueReportComponent implements OnInit {
  private reportService = inject(ReportService);

  report = signal<WeeklyRevenueReport | null>(null);
  isLoading = signal(false);
  errorMessage = signal('');

  // Date range (default 7 ngày gần nhất)
  toDate   = signal(this.formatDate(new Date()));
  fromDate = signal(this.formatDate(this.subtractDays(new Date(), 6)));

  presets = [
    { label: '7 ngày', days: 6 },
    { label: '14 ngày', days: 13 },
    { label: '30 ngày', days: 29 },
  ];
  activePreset = signal(6);

  maxBarAmount = computed(() => {
    const r = this.report();
    if (!r || !r.dailyBreakdown.length) return 1;
    return Math.max(...r.dailyBreakdown.map(d => d.collectedAmount), 1);
  });

  ngOnInit(): void {
    this.loadReport();
  }

  applyPreset(days: number): void {
    this.activePreset.set(days);
    const to   = new Date();
    const from = this.subtractDays(new Date(), days);
    this.toDate.set(this.formatDate(to));
    this.fromDate.set(this.formatDate(from));
    this.loadReport();
  }

  loadReport(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.reportService.getRevenueReport(this.fromDate(), this.toDate()).subscribe({
      next: (data) => {
        this.report.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error(err);
        this.errorMessage.set('Không thể tải báo cáo. Vui lòng thử lại.');
        this.isLoading.set(false);
      }
    });
  }

  isExporting = signal(false);

  exportPdf(): void {
    if (!this.report()) return;
    this.isExporting.set(true);
    this.reportService.exportRevenuePdf(this.fromDate(), this.toDate()).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `revenue_report_${this.fromDate()}_${this.toDate()}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        this.isExporting.set(false);
      },
      error: (err) => {
        console.error('Error exporting PDF', err);
        this.errorMessage.set('Lỗi khi xuất PDF. Vui lòng thử lại.');
        this.isExporting.set(false);
      }
    });
  }

  barHeightPercent(amount: number): number {
    const max = this.maxBarAmount();
    return max > 0 ? Math.round((amount / max) * 100) : 0;
  }

  growthLabel(pct: number): string {
    if (pct > 0)  return `▲ +${pct.toFixed(1)}%`;
    if (pct < 0)  return `▼ ${pct.toFixed(1)}%`;
    return '— 0%';
  }

  growthClass(pct: number): string {
    if (pct > 0) return 'growth-up';
    if (pct < 0) return 'growth-down';
    return 'growth-flat';
  }

  private formatDate(d: Date): string {
    return d.toISOString().split('T')[0];
  }

  private subtractDays(d: Date, days: number): Date {
    const result = new Date(d);
    result.setDate(result.getDate() - days);
    return result;
  }
}

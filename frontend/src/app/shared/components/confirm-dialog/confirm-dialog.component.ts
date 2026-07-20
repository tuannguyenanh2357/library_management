import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmService } from '../../services/confirm.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (confirmService.config(); as config) {
      <div class="modal-backdrop">
        <div class="modal-container">
          <div class="modal-header">
            <h3>{{ config.title }}</h3>
            <button class="btn-close" (click)="confirmService.respond(false)">×</button>
          </div>
          <div class="modal-body">
            <p>{{ config.message }}</p>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" (click)="confirmService.respond(false)">{{ config.cancelText }}</button>
            <button class="btn btn-primary" (click)="confirmService.respond(true)">{{ config.confirmText }}</button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [`
    .modal-backdrop {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 10000;
      animation: fadeIn 0.2s ease-out;
    }
    .modal-container {
      background: white;
      border-radius: 12px;
      width: 90%;
      max-width: 400px;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
      animation: slideUp 0.3s ease-out;
    }
    .modal-header {
      padding: 16px 20px;
      border-bottom: 1px solid #e2e8f0;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .modal-header h3 {
      margin: 0;
      font-size: 1.125rem;
      color: #1e293b;
    }
    .btn-close {
      background: none; border: none; font-size: 1.5rem; color: #94a3b8; cursor: pointer;
    }
    .btn-close:hover { color: #475569; }
    
    .modal-body {
      padding: 20px;
      color: #475569;
      font-size: 1rem;
      line-height: 1.5;
    }
    .modal-footer {
      padding: 16px 20px;
      border-top: 1px solid #e2e8f0;
      display: flex;
      justify-content: flex-end;
      gap: 12px;
    }
    .btn {
      padding: 8px 16px;
      border-radius: 6px;
      font-weight: 500;
      cursor: pointer;
      border: none;
      transition: all 0.2s;
    }
    .btn-secondary {
      background: #f1f5f9; color: #475569;
    }
    .btn-secondary:hover { background: #e2e8f0; }
    .btn-primary {
      background: #3b82f6; color: white;
    }
    .btn-primary:hover { background: #2563eb; }

    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
    @keyframes slideUp {
      from { transform: translateY(20px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
  `]
})
export class ConfirmDialogComponent {
  confirmService = inject(ConfirmService);
}

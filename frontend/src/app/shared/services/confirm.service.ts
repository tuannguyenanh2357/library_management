import { Injectable, signal } from '@angular/core';

export interface ConfirmConfig {
  title?: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ConfirmService {
  config = signal<ConfirmConfig | null>(null);
  private resolveFn: ((value: boolean) => void) | null = null;

  confirm(config: ConfirmConfig | string): Promise<boolean> {
    if (typeof config === 'string') {
      config = { message: config };
    }
    this.config.set({
      title: config.title || 'Xác nhận',
      message: config.message,
      confirmText: config.confirmText || 'Đồng ý',
      cancelText: config.cancelText || 'Hủy'
    });

    return new Promise<boolean>((resolve) => {
      this.resolveFn = resolve;
    });
  }

  respond(result: boolean) {
    if (this.resolveFn) {
      this.resolveFn(result);
      this.resolveFn = null;
    }
    this.config.set(null);
  }
}

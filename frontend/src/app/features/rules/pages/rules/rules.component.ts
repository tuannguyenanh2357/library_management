import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface RuleCategory {
  id: string;
  title: string;
  icon: string;
  description: string;
}

interface FaqItem {
  question: string;
  answer: string;
  isOpen?: boolean;
}

@Component({
  selector: 'app-rules',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './rules.component.html',
  styleUrl: './rules.component.css'
})
export class RulesComponent {
  activeTab = signal<string>('general');
  searchQuery = signal<string>('');

  categories: RuleCategory[] = [
    {
      id: 'general',
      title: 'Quy Định Chung',
      icon: '📜',
      description: 'Các yêu cầu cơ bản về thẻ thành viên, trang phục và ứng xử khi đến thư viện.'
    },
    {
      id: 'borrowing',
      title: 'Mượn & Trả Sách',
      icon: '📖',
      description: 'Hạn mức mượn, quy trình gia hạn và thời gian trả sách tiêu chuẩn.'
    },
    {
      id: 'fines',
      title: 'Xử Lý Vi Phạm & Phạt',
      icon: '⚠️',
      description: 'Chính sách xử lý trễ hạn, hư hỏng hoặc làm mất tài liệu thư viện.'
    },
    {
      id: 'space',
      title: 'Không Gian & Văn Hóa Đọc',
      icon: '🤫',
      description: 'Giữ gìn vệ sinh, không gian yên tĩnh và bảo vệ tài sản dùng chung.'
    }
  ];

  faqs: FaqItem[] = [
    {
      question: 'Tôi có thể mượn tối đa bao nhiêu cuốn sách cùng lúc?',
      answer: 'Mỗi tài khoản độc giả được mượn tối đa 05 cuốn sách cùng lúc đối với tài khoản tiêu chuẩn. Với độc giả đặc biệt (giảng viên, nghiên cứu sinh), hạn mức là 10 cuốn.',
      isOpen: true
    },
    {
      question: 'Thời hạn mượn sách là bao nhiêu ngày và có được gia hạn không?',
      answer: 'Thời hạn mượn sách tiêu chuẩn là 14 ngày. Độc giả có thể gia hạn 01 lần thêm 07 ngày qua website trực tuyến nếu sách đó chưa có người khác đặt mượn.',
      isOpen: false
    },
    {
      question: 'Nếu trả sách quá hạn thì phí phạt được tính như thế nào?',
      answer: 'Phí phạt trả trễ hạn là 5.000 VNĐ / ngày / cuốn. Nếu quá hạn quá 30 ngày mà không trả, hệ thống sẽ tạm khóa tài khoản mượn sách và chuyển sang hình thức đền bù tài sản.',
      isOpen: false
    },
    {
      question: 'Tôi bị mất sách hoặc làm rách sách thì phải làm gì?',
      answer: 'Trường hợp làm mất hoặc hư hỏng sách, độc giả cần báo ngay cho thủ thư. Độc giả có thể mua lại đúng cuốn sách mới (cùng NXB/tái bản) hoặc đền bù 200% giá trị cuốn sách niêm yết cộng với phí xử lý nghiệp vụ (20.000 VNĐ).',
      isOpen: false
    },
    {
      question: 'Người ngoài (chưa đăng ký thẻ) có được vào phòng đọc không?',
      answer: 'Khách tham quan có thể đọc sách tại chỗ sau khi đăng ký thông tin tại bàn thủ thư. Tuy nhiên, để mượn sách về nhà, quý khách cần đăng ký tài khoản thành viên chính thức.',
      isOpen: false
    }
  ];

  selectTab(tabId: string) {
    this.activeTab.set(tabId);
  }

  toggleFaq(index: number) {
    this.faqs[index].isOpen = !this.faqs[index].isOpen;
  }
}

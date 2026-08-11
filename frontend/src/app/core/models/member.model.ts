/** Dữ liệu member dùng chung cho cả admin CRUD lẫn current-user profile */
export interface Member {
  id: number;
  name: string;
  username: string;
  memberCode: string;
  email: string;
  phone: string;
  address: string;
  isActive: boolean;
  age?: number;
  role: string;
}

/** Request cập nhật thông tin cá nhân của user đang đăng nhập */
export interface ProfileUpdateRequest {
  name?: string;
  email?: string;
  phone?: string;
  address?: string;
}

/** Request đổi mật khẩu */
export interface ChangePasswordRequest {
  oldPassword?: string;
  newPassword?: string;
}

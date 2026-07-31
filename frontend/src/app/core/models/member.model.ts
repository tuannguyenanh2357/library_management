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
  avatar?: string;
  age?: number;
  role: string;
}

/** Request cập nhật thông tin cá nhân của user đang đăng nhập */
export interface ProfileUpdateRequest {
  name?: string;
  email?: string;
  phone?: string;
  address?: string;
  avatar?: string;
}

/** Request đổi mật khẩu */
export interface ChangePasswordRequest {
  oldPassword?: string;
  newPassword?: string;
}

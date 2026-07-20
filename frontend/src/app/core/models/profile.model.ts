export interface MemberResponse {
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

export interface MemberUpdateRequest {
  name?: string;
  email?: string;
  phone?: string;
  address?: string;
  avatar?: string;
}

export interface ChangePasswordRequest {
  oldPassword?: string;
  newPassword?: string;
}

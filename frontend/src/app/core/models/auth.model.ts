export interface AuthenticationRequest {
  username: string;
  password: string;
}

export interface AuthenticationResponse {
  token: string;
  authenticated: boolean;
  username: string;
  role: string;
  memberId: number;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  name: string;
  phone: string;
  address: string;
}

export interface UserResponse {
  id: number;
  username: string;
  name: string;
  email: string;
  role: string;
}


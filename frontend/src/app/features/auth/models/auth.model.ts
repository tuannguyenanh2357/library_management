export interface AuthenticationRequest {
  username: string;
  password: string;
}

export interface AuthenticationResponse {
  token: string;
  authenticated: boolean;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  name: string;
  phone: string;
  address: string;
}

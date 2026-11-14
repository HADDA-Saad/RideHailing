export type Role = 'RIDER' | 'DRIVER';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  role: Role;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  phone: string;
}

export interface RegisterResponse {
  userId: number;
  email: string;
}

export interface RiderProfile {
  id: number;
  name: string;
  email: string;
  phone: string;
}

export interface DriverProfile {
  id: number;
  name: string;
  email: string;
  phone: string;
  available: boolean;
}

export type Profile = RiderProfile | DriverProfile;

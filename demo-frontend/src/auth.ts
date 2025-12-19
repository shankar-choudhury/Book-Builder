import { apiFetch } from "./api";
import type { LoginRequest, RegisterRequest, UserDto, UserUpdateRequest } from "./types";

export async function register(req: RegisterRequest) {
  return apiFetch("/api/auth/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });
}

export async function login(req: LoginRequest) {
  return apiFetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });
}

export async function me(): Promise<UserDto> {
  // First authenticated GET mints CSRF cookie in your backend
  return apiFetch("/api/users/me", { method: "GET" });
}

export async function updateUser(req: UserUpdateRequest): Promise<UserDto> {
  return apiFetch("/api/users/update", {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
  });
}

export async function refresh() {
  return apiFetch("/api/auth/refresh", { method: "POST" });
}

export async function logout() {
  return apiFetch("/api/logout", { method: "POST" });
}

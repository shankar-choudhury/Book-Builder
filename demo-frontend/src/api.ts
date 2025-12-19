const API_BASE = (import.meta.env.VITE_API_BASE ?? "").replace(/\/$/, "");

function getCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`));
  return match ? decodeURIComponent(match[1]) : null;
}

type ApiFetchOptions = Omit<RequestInit, "headers"> & {
  headers?: Record<string, string>;
};

export async function apiFetch(path: string, options: ApiFetchOptions = {}) {
  const url = path.startsWith("http") ? path : `${API_BASE}${path}`;
  const method = (options.method ?? "GET").toUpperCase();

  const headers: Record<string, string> = {
    ...(options.headers ?? {}),
  };

  const isUnsafe = !["GET", "HEAD", "OPTIONS", "TRACE"].includes(method);
  if (isUnsafe) {
    const csrf = getCookie("XSRF-TOKEN");
    if (csrf) headers["X-XSRF-TOKEN"] = csrf;
  }

  const res = await fetch(url, {
    ...options,
    method,
    headers,
    credentials: "include",
  });

  const contentType = res.headers.get("content-type") ?? "";
  const isJson = contentType.includes("application/json");

  if (!res.ok) {
    const body = isJson ? await res.json().catch(() => null) : await res.text().catch(() => "");
    const msg = typeof body === "string" ? body : body?.message ?? res.statusText;
    throw new Error(`${res.status} ${msg}`);
  }

  if (res.status === 204) return null;
  return isJson ? res.json() : res.text();
}

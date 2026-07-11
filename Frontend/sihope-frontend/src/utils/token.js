const STORAGE_KEY = "sihope_token";

export function getToken() {
    return localStorage.getItem(STORAGE_KEY);
}

export function saveToken(token) {
    if (token) localStorage.setItem(STORAGE_KEY, token);
}

export function clearToken() {
    localStorage.removeItem(STORAGE_KEY);
}

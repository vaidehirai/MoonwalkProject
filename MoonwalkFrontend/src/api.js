const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

export async function request(path, options) {
  const response = await fetch(`${API_BASE_URL}${path}`, options);
  const body = await response.json().catch(() => ({}));

  if (!response.ok) {
    const error = new Error(body.msg || `Request failed (${response.status})`);
    error.status = response.status;
    throw error;
  }

  return body;
}

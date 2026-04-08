const parseJson = async (response) => response.json().catch(() => ({}));

const request = async (url, options = {}) => {
  const response = await fetch(url, options);
  const body = await parseJson(response);
  if (!response.ok) {
    const message = body.message || `Request failed (${response.status})`;
    throw new Error(message);
  }
  return body;
};

export const adminApi = {
  me: () => request('/api/admin/me'),
  login: (payload) => request('/api/admin/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  logout: () => request('/api/admin/logout', { method: 'POST' }),
  list: (entity, query = '') => request(`/api/admin/${entity}${query ? `?${query}` : ''}`),
  create: (entity, payload) => request(`/api/admin/${entity}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  update: (entity, key, payload) => request(`/api/admin/${entity}/${encodeURIComponent(key)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  }),
  remove: async (entity, key) => {
    const response = await fetch(`/api/admin/${entity}/${encodeURIComponent(key)}`, { method: 'DELETE' });
    if (!response.ok && response.status !== 204) {
      const body = await parseJson(response);
      throw new Error(body.message || 'Delete failed.');
    }
  },
  upload: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return request('/api/admin/upload', { method: 'POST', body: formData });
  }
};

export const uiState = {
  render(el, message, isError = false) {
    if (!el) return;
    if (!message) {
      el.innerHTML = '';
      return;
    }
    el.innerHTML = `<div class="state ${isError ? 'error' : ''}">${message}</div>`;
  }
};

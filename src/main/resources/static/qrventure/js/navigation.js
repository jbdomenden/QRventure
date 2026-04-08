document.addEventListener('DOMContentLoaded', async () => {
  const out = document.querySelector('#navResults');
  const q = new URLSearchParams(location.search).get('q') || '';
  document.querySelector('#navQuery').value = q;

  async function runSearch(term) {
    if (!term || term.length < 2) {
      renderState(out, 'Type at least 2 characters to search destinations.');
      return;
    }
    out.innerHTML = '<div class="card skeleton"></div>';
    try {
      const data = await apiGet(`/api/search?q=${encodeURIComponent(term)}`);
      const blocks = [
        ['Attractions', data.attractions, 'attractions'],
        ['Dining', data.dining, 'dining'],
        ['Services', data.services, 'services']
      ].map(([title, items, type]) => `<section><h3>${title}</h3>${items.length ? `<div class="grid cards">${items.map(i => cardHtml(i, type)).join('')}</div>` : '<p class="meta">No matches</p>'}</section>`);
      out.innerHTML = blocks.join('');
    } catch (e) { renderState(out, e.message, true); }
  }

  if (q) runSearch(q);
  document.querySelector('#navigationForm').addEventListener('submit', (e) => {
    e.preventDefault();
    runSearch(document.querySelector('#navQuery').value.trim());
  });
});

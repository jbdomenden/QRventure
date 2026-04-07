document.addEventListener('DOMContentLoaded', async () => {
  const featuredWrap = document.querySelector('#featuredWrap');
  featuredWrap.innerHTML = '<div class="card skeleton"></div><div class="card skeleton"></div><div class="card skeleton"></div>';
  try {
    const data = await apiGet('/api/featured');
    featuredWrap.innerHTML = `
      <section><h3>Featured Attractions</h3><div class="grid cards">${data.attractions.map(a => cardHtml(a, 'attractions')).join('')}</div></section>
      <section><h3>Featured Dining</h3><div class="grid cards">${data.dining.map(a => cardHtml(a, 'dining')).join('')}</div></section>
      <section><h3>Featured Services</h3><div class="grid cards">${data.services.map(a => cardHtml(a, 'services')).join('')}</div></section>`;
  } catch (e) {
    renderState(featuredWrap, e.message, true);
  }

  document.querySelector('#globalSearchForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const q = document.querySelector('#homeSearch').value.trim();
    if (q.length < 2) return;
    location.href = `/qrventure/navigation.html?q=${encodeURIComponent(q)}`;
  });
});

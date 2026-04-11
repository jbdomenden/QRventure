document.addEventListener('DOMContentLoaded', async () => {
  const featuredWrap = document.querySelector('#featuredWrap');
  featuredWrap.innerHTML = '<div class="card skeleton"></div><div class="card skeleton"></div><div class="card skeleton"></div>';
  try {
    const [featured, routes] = await Promise.all([
      apiGet('/api/featured'),
      apiGet('/api/routes')
    ]);

    featuredWrap.innerHTML = `
      <section>
        <div class="section-header"><h2>Featured Landmarks</h2><p>Historic anchors visitors prioritize.</p></div>
        <div class="grid cards">${featured.attractions.map(a => cardHtml(a, 'attractions')).join('')}</div>
      </section>
      <section>
        <div class="section-header"><h2>Walking Routes</h2><p>Start with these curated itineraries.</p></div>
        <div class="grid cards">${(featured.routes || routes).slice(0, 3).map(r => cardHtml(r, 'routes')).join('')}</div>
      </section>
      <section>
        <div class="section-header"><h2>Dining Highlights</h2><p>Notable stops for breaks between sites.</p></div>
        <div class="grid cards">${featured.dining.map(a => cardHtml(a, 'dining')).join('')}</div>
      </section>`;
  } catch (e) {
    renderState(featuredWrap, e.message, true);
  }

  document.querySelector('#globalSearchForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const jumpTo = document.querySelector('#homeJump').value;
    if (jumpTo) {
      location.href = jumpTo;
      return;
    }
    const q = document.querySelector('#homeSearch').value.trim();
    if (q.length < 2) return;
    location.href = `/qrventure/attractions.html?q=${encodeURIComponent(q)}`;
  });
});

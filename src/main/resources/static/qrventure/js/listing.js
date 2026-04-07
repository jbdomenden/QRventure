async function loadListing(config) {
  const target = document.querySelector(config.target);
  target.innerHTML = '<div class="card skeleton"></div><div class="card skeleton"></div>';
  try {
    const query = new URLSearchParams(location.search);
    const apiQuery = new URLSearchParams();
    config.filters.forEach(f => {
      const value = query.get(f.url);
      if (value) apiQuery.set(f.api, value);
    });
    const data = await apiGet(`${config.endpoint}?${apiQuery.toString()}`);
    if (!data.length) return renderState(target, config.empty);
    target.innerHTML = `<div class="grid cards">${data.map(i => cardHtml(i, config.type)).join('')}</div>`;
  } catch (e) {
    renderState(target, e.message, true);
  }
}

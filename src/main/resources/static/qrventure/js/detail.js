async function loadDetail(endpoint, typeLabel) {
  const target = document.querySelector('#detail');
  target.innerHTML = '<div class="card skeleton"></div>';
  const key = new URLSearchParams(location.search).get('key');
  if (!key) return renderState(target, `${typeLabel} key is missing.`, true);
  try {
    const item = await apiGet(`${endpoint}/${encodeURIComponent(key)}`);
    const extra = item.fullDescription || item.description;
    target.innerHTML = `<div class="detail-layout">
      <article class="card"><img src="${item.imagePath}" alt="${item.name}"><h1>${item.name}</h1><p>${extra}</p></article>
      <aside class="card">
        <p><strong>Location:</strong> ${item.locationText}</p>
        <p><strong>Hours:</strong> ${item.openingHours || item.operatingHours}</p>
        ${item.entranceFee ? `<p><strong>Entrance Fee:</strong> ${item.entranceFee}</p>` : ''}
        ${item.priceRange ? `<p><strong>Price Range:</strong> ${item.priceRange}</p>` : ''}
        <p><strong>Contact:</strong> ${item.contactDetails}</p>
        <a class="btn btn-primary" target="_blank" href="https://www.google.com/maps?q=${item.latitude},${item.longitude}">Open in Maps</a>
      </aside></div>`;
  } catch (e) {
    renderState(target, e.message, true);
  }
}

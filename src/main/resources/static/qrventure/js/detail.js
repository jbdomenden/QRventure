async function loadDetail(endpoint, typeLabel) {
  const target = document.querySelector('#detail');
  target.innerHTML = '<div class="card skeleton"></div>';
  const key = new URLSearchParams(location.search).get('key');
  if (!key) return renderState(target, `${typeLabel} key is missing.`, true);

  try {
    const item = await apiGet(`${endpoint}/${encodeURIComponent(key)}`);
    const summaryMeta = [
      item.category || item.diningType || item.serviceType || item.routeType,
      item.historicalPeriod || item.cuisine || item.estimatedDuration || item.hours
    ].filter(Boolean).join(' · ');

    const primaryAction = item.mapLink
      ? `<a class="btn btn-primary" target="_blank" href="${item.mapLink}">Open Route Map</a>`
      : `<a class="btn btn-primary" target="_blank" href="https://www.google.com/maps?q=${item.latitude || 14.5906},${item.longitude || 120.9734}">Open in Maps</a>`;

    target.innerHTML = `<div class="detail-layout">
      <article class="card">
        <img src="${item.imagePath || '/qrventure/images/fort-santiago.svg'}" alt="${item.name}">
        <h1>${item.name}</h1>
        ${summaryMeta ? `<p class="meta">${summaryMeta}</p>` : ''}
        <p>${item.fullDescription || item.shortDescription}</p>
      </article>
      <aside class="card">
        ${item.locationText ? `<p><strong>Location:</strong> ${item.locationText}</p>` : ''}
        ${item.startingPoint ? `<p><strong>Starting point:</strong> ${item.startingPoint}</p>` : ''}
        ${item.openingHours || item.hours ? `<p><strong>Hours:</strong> ${item.openingHours || item.hours}</p>` : ''}
        ${item.estimatedDuration ? `<p><strong>Estimated duration:</strong> ${item.estimatedDuration}</p>` : ''}
        ${item.entranceFee ? `<p><strong>Entrance fee:</strong> ${item.entranceFee}</p>` : ''}
        ${item.priceRange ? `<p><strong>Price range:</strong> ${item.priceRange}</p>` : ''}
        ${item.distanceText ? `<p><strong>Distance:</strong> ${item.distanceText}</p>` : ''}
        ${item.bestTimeToVisit ? `<p><strong>Best time to visit:</strong> ${item.bestTimeToVisit}</p>` : ''}
        ${item.visitorTips || item.visitorNotes || item.travelTips ? `<p><strong>Visitor notes:</strong> ${item.visitorTips || item.visitorNotes || item.travelTips}</p>` : ''}
        ${item.contactDetails ? `<p><strong>Contact:</strong> ${item.contactDetails}</p>` : ''}
        ${primaryAction}
      </aside>
    </div>`;
  } catch (e) {
    renderState(target, e.message, true);
  }
}

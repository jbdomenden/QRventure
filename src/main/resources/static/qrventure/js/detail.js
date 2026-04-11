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

    const images = detailImages(item);
    const gallery = renderGallery(images, item.name);

    target.innerHTML = `<div class="detail-layout">
      <article class="card">
        ${gallery}
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
    initGallery(target);
  } catch (e) {
    renderState(target, e.message, true);
  }
}

function detailImages(item) {
  const fromList = Array.isArray(item.imageUrls) ? item.imageUrls : [];
  const merged = [...fromList];
  if (typeof item.imagePath === 'string' && item.imagePath) merged.unshift(item.imagePath);

  const clean = merged.filter((url, idx) => {
    if (typeof url !== 'string') return false;
    const trimmed = url.trim();
    if (!trimmed) return false;
    if (!/^https?:\/\//i.test(trimmed)) return false;
    return merged.indexOf(url) === idx;
  });
  return clean;
}

function renderGallery(images, name) {
  const placeholder = 'https://placehold.co/1200x675/e8dcc7/5f564d?text=No+Image+Available';
  if (!images.length) {
    return `<div class="detail-no-image">No gallery images available for this location.</div>`;
  }

  return `<section class="detail-gallery" data-gallery>
    <div class="detail-gallery-main">
      <img src="${images[0]}" alt="${name}" data-gallery-main onerror="this.onerror=null;this.src='${placeholder}'">
      ${images.length > 1 ? `<button class="gallery-nav prev" type="button" data-gallery-prev aria-label="Previous image">‹</button>
      <button class="gallery-nav next" type="button" data-gallery-next aria-label="Next image">›</button>` : ''}
    </div>
    ${images.length > 1 ? `<div class="detail-thumbs">
      ${images.map((url, index) => `<button class="detail-thumb ${index === 0 ? 'active' : ''}" type="button" data-gallery-thumb="${index}">
        <img src="${url}" alt="${name} thumbnail ${index + 1}" onerror="this.style.visibility='hidden'">
      </button>`).join('')}
    </div>` : ''}
  </section>`;
}

function initGallery(root) {
  const gallery = root.querySelector('[data-gallery]');
  if (!gallery) return;
  const main = gallery.querySelector('[data-gallery-main]');
  const thumbs = Array.from(gallery.querySelectorAll('[data-gallery-thumb]'));
  if (!main || !thumbs.length) return;

  const sources = thumbs.map(t => t.querySelector('img')?.getAttribute('src')).filter(Boolean);
  let current = 0;

  const setIndex = (nextIndex) => {
    current = (nextIndex + sources.length) % sources.length;
    main.src = sources[current];
    thumbs.forEach((thumb, idx) => thumb.classList.toggle('active', idx === current));
  };

  thumbs.forEach((thumb, idx) => thumb.addEventListener('click', () => setIndex(idx)));
  gallery.querySelector('[data-gallery-prev]')?.addEventListener('click', () => setIndex(current - 1));
  gallery.querySelector('[data-gallery-next]')?.addEventListener('click', () => setIndex(current + 1));
}

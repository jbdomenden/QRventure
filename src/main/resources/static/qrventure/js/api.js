const apiGet = async (path) => {
  const res = await fetch(path);
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || 'Request failed');
  return data;
};

const renderState = (el, message, isError = false) => {
  el.innerHTML = `<div class="state ${isError ? 'error' : ''}">${message}</div>`;
};

const detailHref = (type, slug) => {
  if (type === 'attractions') return `/qrventure/attraction-detail.html?key=${encodeURIComponent(slug)}`;
  if (type === 'dining') return `/qrventure/dining-detail.html?key=${encodeURIComponent(slug)}`;
  if (type === 'services') return `/qrventure/service-detail.html?key=${encodeURIComponent(slug)}`;
  return `/qrventure/route-detail.html?key=${encodeURIComponent(slug)}`;
};

const cardHtml = (item, type) => {
  const meta = type === 'attractions'
    ? `${item.category} · ${item.historicalPeriod}`
    : type === 'dining'
      ? `${item.diningType} · ${item.cuisine}`
      : type === 'services'
        ? `${item.serviceType} · ${item.hours}`
        : `${item.routeType} · ${item.estimatedDuration}`;

  const desc = item.shortDescription || item.fullDescription || '';
  const image = (item.imageUrl || '').trim();
  const visual = image
    ? `<img src="${image}" alt="${item.name}" loading="lazy" onerror="this.outerHTML='<div class=&quot;card-image-empty&quot;>Image unavailable</div>'">`
    : '<div class="card-image-empty">Image unavailable</div>';

  return `<article class="card visual-card">
    ${visual}
    <div class="card-visual-body">
      <h3 class="card-title">${item.name}</h3>
      <p class="meta">${meta}</p>
      <p>${desc}</p>
      <a class="btn btn-secondary" href="${detailHref(type, item.slug || item.id)}">View details</a>
    </div>
  </article>`;
};

const apiGet = async (path) => {
  const res = await fetch(path);
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || 'Request failed');
  return data;
};

const renderState = (el, message, isError = false) => {
  el.innerHTML = `<div class="state ${isError ? 'error' : ''}">${message}</div>`;
};

const cardHtml = (item, type) => {
  if (type === 'routes') {
    return `<article class="card">
      <h3 class="card-title">${item.name}</h3>
      <p class="meta">${item.estimatedDuration} · ${item.difficultyLevel || 'Moderate'}</p>
      <p>${item.description}</p>
      <p class="meta">Stops: ${item.stopsSummary || item.mainStops || 'Fort Santiago, plazas, and church area'}</p>
    </article>`;
  }

  const meta = type === 'attractions' ? item.category : (item.cuisineOrType || item.serviceType);
  const desc = item.shortDescription || item.description;
  const detailPath = type === 'attractions' ? 'attraction' : type === 'dining' ? 'dining' : 'service';

  return `<article class="card visual-card">
    <img src="${item.imagePath}" alt="${item.name}">
    <div class="card-visual-body">
      <h3 class="card-title">${item.name}</h3>
      <p class="meta">${meta}</p>
      <p>${desc}</p>
      <a class="btn btn-secondary" href="/qrventure/${detailPath}-detail.html?key=${item.slug}">View details</a>
    </div>
  </article>`;
};

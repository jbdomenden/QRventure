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
  const meta = type === 'attractions' ? item.category : (item.cuisineOrType || item.serviceType);
  const desc = item.shortDescription || item.description;
  return `<article class="card">
    <img src="${item.imagePath}" alt="${item.name}">
    <h3 class="card-title">${item.name}</h3>
    <p class="meta">${meta}</p>
    <p>${desc}</p>
    <a class="btn btn-secondary" href="/qrventure/${type === 'attractions' ? 'attraction' : type === 'dining' ? 'dining' : 'service'}-detail.html?key=${item.slug}">View details</a>
  </article>`;
};

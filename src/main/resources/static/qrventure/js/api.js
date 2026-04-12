const firebaseConfig = {
  apiKey: "AIzaSyBJyIcD8z_4qfl3sYOGfDkVXGuBWI-KhwA",
  authDomain: "qrventure-b4e52.firebaseapp.com",
  projectId: "qrventure-b4e52",
  storageBucket: "qrventure-b4e52.firebasestorage.app",
  messagingSenderId: "603551416491",
  appId: "1:603551416491:web:42f9fd0eaa757f476837ee",
  databaseURL: "https://qrventure-b4e52-default-rtdb.firebaseio.com"
};

let dbPromise;

async function getDb() {
  if (!dbPromise) {
    dbPromise = Promise.all([
      import('https://www.gstatic.com/firebasejs/12.0.0/firebase-app.js'),
      import('https://www.gstatic.com/firebasejs/12.0.0/firebase-database.js')
    ]).then(([appModule, dbModule]) => {
      const app = appModule.initializeApp(firebaseConfig);
      return {
        db: dbModule.getDatabase(app),
        ref: dbModule.ref,
        get: dbModule.get,
        child: dbModule.child
      };
    });
  }
  return dbPromise;
}

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
    ? `${item.category || ''} · ${item.historicalPeriod || ''}`
    : type === 'dining'
      ? `${item.diningType || ''} · ${item.cuisine || ''}`
      : type === 'services'
        ? `${item.serviceType || ''} · ${item.hours || ''}`
        : `${item.routeType || ''} · ${item.estimatedDuration || ''}`;

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

const collectionAliases = {
  attractions: 'attractions',
  dining: 'dining',
  services: 'services',
  routes: 'routes'
};

function normalizeList(raw) {
  if (!raw) return [];
  const items = Array.isArray(raw) ? raw : Object.entries(raw).map(([id, item]) => ({ id, ...item }));
  return items
    .map((item) => ({ ...item, id: item.id || item.slug }))
    .filter((item) => (item.status || 'open').toLowerCase() === 'open')
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0) || (a.name || '').localeCompare(b.name || ''));
}

function includesTerm(item, term, keys) {
  return keys.some((key) => (item[key] || '').toString().toLowerCase().includes(term));
}

async function readNode(path) {
  const { db, ref, get, child } = await getDb();
  const snapshot = await get(child(ref(db), path));
  return snapshot.exists() ? snapshot.val() : null;
}

async function loadCollection(type) {
  return normalizeList(await readNode(`qrventure/${type}`));
}

async function loadFeatured() {
  const featured = await readNode('qrventure/featured');
  if (!featured) {
    const [attractions, dining, routes, services] = await Promise.all([
      loadCollection('attractions'),
      loadCollection('dining'),
      loadCollection('routes'),
      loadCollection('services')
    ]);
    return {
      attractions: attractions.filter((x) => x.isFeatured).slice(0, 6),
      dining: dining.filter((x) => x.isFeatured).slice(0, 6),
      routes: routes.filter((x) => x.isFeatured).slice(0, 6),
      services: services.slice(0, 6)
    };
  }

  const response = {};
  for (const type of ['attractions', 'dining', 'routes', 'services']) {
    const ids = Array.isArray(featured[type]) ? featured[type] : [];
    if (!ids.length) {
      response[type] = [];
      continue;
    }
    const all = await loadCollection(type);
    const byKey = new Map(all.flatMap((item) => [[item.id, item], [item.slug, item]]));
    response[type] = ids.map((id) => byKey.get(id)).filter(Boolean);
  }
  return response;
}

async function apiGet(path) {
  const url = new URL(path, window.location.origin);
  const endpoint = url.pathname.replace(/^\/api\//, '');
  const [resource, key] = endpoint.split('/');

  if (resource === 'featured') return loadFeatured();

  if (resource === 'search') {
    const term = (url.searchParams.get('q') || '').trim().toLowerCase();
    if (term.length < 2) throw new Error('Query must be at least 2 characters');
    const [attractions, dining, services, routes] = await Promise.all([
      loadCollection('attractions'),
      loadCollection('dining'),
      loadCollection('services'),
      loadCollection('routes')
    ]);
    return {
      query: term,
      attractions: attractions.filter((item) => includesTerm(item, term, ['name', 'shortDescription', 'fullDescription', 'category', 'historicalPeriod'])),
      dining: dining.filter((item) => includesTerm(item, term, ['name', 'shortDescription', 'fullDescription', 'diningType', 'cuisine'])),
      services: services.filter((item) => includesTerm(item, term, ['name', 'shortDescription', 'fullDescription', 'serviceType', 'visitorNotes'])),
      routes: routes.filter((item) => includesTerm(item, term, ['name', 'shortDescription', 'fullDescription', 'routeType', 'startingPoint']))
    };
  }

  const type = collectionAliases[resource];
  if (!type) throw new Error('Unsupported API endpoint');

  const items = await loadCollection(type);
  if (key) {
    const item = items.find((x) => `${x.id}` === key || `${x.slug}` === key);
    if (!item) throw new Error(`${resource.slice(0, -1)} not found`);
    return item;
  }

  if (resource === 'attractions') {
    const q = (url.searchParams.get('q') || '').trim().toLowerCase();
    const category = (url.searchParams.get('category') || '').trim().toLowerCase();
    return items.filter((item) => {
      if (category && (item.category || '').toLowerCase() !== category) return false;
      if (!q) return true;
      return includesTerm(item, q, ['name', 'shortDescription', 'fullDescription']);
    });
  }

  if (resource === 'dining') {
    const typeFilter = (url.searchParams.get('type') || '').trim().toLowerCase();
    return typeFilter ? items.filter((item) => (item.diningType || '').toLowerCase().includes(typeFilter)) : items;
  }

  if (resource === 'services') {
    const typeFilter = (url.searchParams.get('type') || '').trim().toLowerCase();
    return typeFilter ? items.filter((item) => (item.serviceType || '').toLowerCase().includes(typeFilter)) : items;
  }

  if (resource === 'routes') {
    const q = (url.searchParams.get('q') || '').trim().toLowerCase();
    return q ? items.filter((item) => includesTerm(item, q, ['name', 'shortDescription', 'fullDescription'])) : items;
  }

  return items;
}

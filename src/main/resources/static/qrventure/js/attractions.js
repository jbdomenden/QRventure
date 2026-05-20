(() => {
  const state = { q: '', category: '' };
  const cache = { items: null };

  const els = {
    items: document.getElementById('items'),
    searchInput: document.getElementById('searchInput'),
    categorySelect: document.getElementById('categorySelect'),
    searchForm: document.getElementById('attractionsSearchForm'),
    pillNav: document.querySelector('.pill-nav'),
    resultsSummary: document.getElementById('resultsSummary')
  };

  const escapeHtml = (value = '') => String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');

  const detailHref = (slug) => `/qrventure/attraction-detail.html?key=${encodeURIComponent(slug)}`;

  const categoryMatches = (item, categoryKey) => {
    if (!categoryKey) return true;

    const category = (item.category || '').toLowerCase();
    switch (categoryKey) {
      case 'museum':
        return category.includes('museum') || category.includes('shrine');
      case 'church':
        return category.includes('church');
      case 'fortification':
        return category.includes('fort') || category.includes('fortress') || category.includes('fortification') || category.includes('gate');
      case 'civic':
        return category.includes('plaza') || category.includes('garden') || category.includes('government') || category.includes('heritage complex') || category.includes('memorial') || category.includes('tourist center');
      default:
        return category === categoryKey;
    }
  };

  const searchMatches = (item, query) => {
    if (!query) return true;

    const haystack = [
      item.name,
      item.shortDescription,
      item.fullDescription,
      item.category,
      item.historicalPeriod,
      item.locationText,
      item.bestTimeToVisit
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();

    return haystack.includes(query.toLowerCase());
  };

  const readQuery = () => {
    const params = new URLSearchParams(window.location.search);
    state.q = (params.get('q') || '').trim();
    state.category = (params.get('category') || '').trim();
  };

  const writeQuery = () => {
    const params = new URLSearchParams();
    if (state.q) params.set('q', state.q);
    if (state.category) params.set('category', state.category);
    const next = `${window.location.pathname}${params.toString() ? `?${params}` : ''}`;
    window.history.replaceState({}, '', next);
  };

  const syncControls = () => {
    els.searchInput.value = state.q;
    els.categorySelect.value = state.category;

    [...els.pillNav.querySelectorAll('.pill')].forEach((pill) => {
      const matches = pill.dataset.category === state.category || (!pill.dataset.category && !state.category);
      pill.classList.toggle('is-active', matches);
    });
  };

  const preloadTopImages = (items) => {
    document.querySelectorAll('link[data-preload-attraction]').forEach((node) => node.remove());

    items.slice(0, 3).forEach((item) => {
      if (!item.imageUrl) return;
      const link = document.createElement('link');
      link.rel = 'preload';
      link.as = 'image';
      link.href = item.imageUrl;
      link.setAttribute('data-preload-attraction', 'true');
      document.head.appendChild(link);
    });
  };

  const renderSkeletons = () => {
    els.items.innerHTML = `
      <div class="grid cards attractions-grid">
        ${Array.from({ length: 6 }).map(() => '<div class="card skeleton"></div>').join('')}
      </div>
    `;
  };

  const imageHtml = (item, index) => {
    const image = (item.imageUrl || '').trim();
    if (!image) return '<div class="card-image-empty">Image unavailable</div>';

    const loading = index < 3 ? 'eager' : 'lazy';
    const fallback = '&lt;div class=&quot;card-image-empty&quot;&gt;Image unavailable&lt;/div&gt;';
    return `<img src="${escapeHtml(image)}" alt="${escapeHtml(item.name)}" loading="${loading}" onerror="this.outerHTML='${fallback}'">`;
  };

  const renderEmpty = () => {
    els.resultsSummary.textContent = 'No attractions matched the current filters.';
    els.items.innerHTML = `
      <div class="empty-state">
        <p>No attractions found.</p>
        <button type="button" class="btn btn-primary" data-reset-filters>Reset filters</button>
      </div>
    `;
  };

  const attractionCardHtml = (item, index) => {
    const meta = item.historicalPeriod
      ? `${escapeHtml(item.category || 'Attraction')} &middot; ${escapeHtml(item.historicalPeriod)}`
      : escapeHtml(item.category || 'Attraction');
    const location = item.locationText
      ? `<p class="meta attraction-location">${escapeHtml(item.locationText)}</p>`
      : '';
    const note = item.bestTimeToVisit
      ? `<span class="attraction-tag">Best time: ${escapeHtml(item.bestTimeToVisit)}</span>`
      : '';
    const desc = item.shortDescription || item.fullDescription || 'Discover this Intramuros destination.';

    return `
      <article class="card visual-card attraction-list-card">
        ${imageHtml(item, index)}
        <div class="card-visual-body attraction-card-body">
          <h3>${escapeHtml(item.name)}</h3>
          <p class="meta">${meta}</p>
          ${location}
          <p>${escapeHtml(desc)}</p>
          <div class="attraction-card-actions">
            ${note}
            <a class="btn btn-secondary" href="${detailHref(item.slug || item.id)}">View details</a>
          </div>
        </div>
      </article>
    `;
  };

  const renderCards = (items) => {
    if (!items.length) return renderEmpty();

    els.resultsSummary.textContent = `Showing ${items.length} attraction${items.length === 1 ? '' : 's'}.`;
    els.items.innerHTML = `
      <div class="grid cards attractions-grid">
        ${items.map((item, index) => attractionCardHtml(item, index)).join('')}
      </div>
    `;
  };

  const filteredItems = (items) => items.filter((item) => searchMatches(item, state.q) && categoryMatches(item, state.category));

  const fetchAttractions = async () => {
    renderSkeletons();

    try {
      if (!Array.isArray(cache.items)) {
        const data = await apiGet('/api/attractions');
        cache.items = Array.isArray(data) ? data : [];
      }

      const items = filteredItems(cache.items);
      preloadTopImages(items);
      renderCards(items);
    } catch (error) {
      els.resultsSummary.textContent = 'Unable to load attractions.';
      els.items.innerHTML = `<div class="state error">${escapeHtml(error.message || 'Unable to load attractions.')}</div>`;
    }
  };

  const applyFilters = () => {
    state.q = els.searchInput.value.trim();
    state.category = els.categorySelect.value.trim();
    writeQuery();
    syncControls();
    fetchAttractions();
  };

  const initEvents = () => {
    let debounceTimer;

    els.searchInput.addEventListener('input', () => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(applyFilters, 180);
    });

    els.categorySelect.addEventListener('change', applyFilters);

    els.searchForm.addEventListener('submit', (event) => {
      event.preventDefault();
      applyFilters();
    });

    els.pillNav.addEventListener('click', (event) => {
      const pill = event.target.closest('.pill');
      if (!pill) return;
      els.categorySelect.value = pill.dataset.category || '';
      applyFilters();
    });

    document.addEventListener('click', (event) => {
      if (!event.target.closest('[data-reset-filters]')) return;
      state.q = '';
      state.category = '';
      writeQuery();
      syncControls();
      fetchAttractions();
    });
  };

  const boot = () => {
    readQuery();
    syncControls();
    initEvents();
    fetchAttractions();
  };

  boot();
})();

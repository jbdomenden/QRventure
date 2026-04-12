(() => {
  const state = { q: '', category: '' };

  const els = {
    items: document.getElementById('items'),
    searchInput: document.getElementById('searchInput'),
    categorySelect: document.getElementById('categorySelect'),
    clearSearch: document.getElementById('clearSearch'),
    filtersPanel: document.getElementById('filters-panel'),
    filterToggle: document.querySelector('.filter-toggle'),
    pillNav: document.querySelector('.pill-nav')
  };

  const escapeHtml = (value = '') => value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');

  const detailHref = (slug) => `/qrventure/attraction-detail.html?key=${encodeURIComponent(slug)}`;
  const descriptionText = (item) => item.shortDescription || item.fullDescription || 'Discover this Intramuros destination.';

  const toggleClear = () => {
    els.clearSearch.classList.toggle('is-visible', Boolean(els.searchInput.value.trim()));
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
    toggleClear();
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
    els.items.innerHTML = `<div class="skeleton-grid">${Array.from({ length: 6 }).map(() => `
      <article class="skeleton-card" aria-hidden="true">
        <div class="skeleton-media"></div>
        <div class="skeleton-body">
          <div class="skeleton-line"></div>
          <div class="skeleton-line short"></div>
          <div class="skeleton-line mid"></div>
        </div>
      </article>
    `).join('')}</div>`;
  };

  const cardMediaHtml = (item, index) => {
    if (!item.imageUrl) return '<div class="card-media-fallback" aria-hidden="true">No image available</div>';
    const loading = index < 3 ? 'eager' : 'lazy';
    const fetchpriority = index < 3 ? 'high' : 'auto';
    return `<img src="${escapeHtml(item.imageUrl)}" alt="${escapeHtml(item.name)}" width="1600" height="900" loading="${loading}" fetchpriority="${fetchpriority}">`;
  };

  const attachImageFallbackHandlers = () => {
    els.items.querySelectorAll('.card-media img').forEach((img) => {
      img.addEventListener('error', () => {
        const fallback = document.createElement('div');
        fallback.className = 'card-media-fallback';
        fallback.textContent = 'No image available';
        img.replaceWith(fallback);
      }, { once: true });
    });
  };

  const renderEmpty = () => {
    els.items.innerHTML = `
      <div class="empty-state">
        <p>No attractions found</p>
        <button type="button" class="btn btn-primary" data-reset-filters>Reset filters</button>
      </div>
    `;
  };

  const renderCards = (items) => {
    if (!items.length) return renderEmpty();

    els.items.innerHTML = `<div class="attractions-grid">${items.map((item, index) => `
      <a class="attraction-card-link" href="${detailHref(item.slug || item.id)}" aria-label="View details for ${escapeHtml(item.name)}">
        <article class="attraction-card">
          <div class="card-media">${cardMediaHtml(item, index)}</div>
          <div class="card-content">
            <h2 class="card-title">${escapeHtml(item.name)}</h2>
            <p class="card-category">${escapeHtml(item.category || 'Attraction')}</p>
            <p class="card-description">${escapeHtml(descriptionText(item))}</p>
            <span class="card-cta">View details</span>
          </div>
        </article>
      </a>
    `).join('')}</div>`;

    attachImageFallbackHandlers();
  };

  const fetchAttractions = async () => {
    renderSkeletons();
    const params = new URLSearchParams();
    if (state.q) params.set('q', state.q);
    if (state.category) params.set('category', state.category);

    try {
      const data = await apiGet(`/api/attractions${params.toString() ? `?${params}` : ''}`);
      const items = Array.isArray(data) ? data : [];
      preloadTopImages(items);
      renderCards(items);
    } catch (error) {
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
      debounceTimer = setTimeout(applyFilters, 150);
      toggleClear();
    });

    els.categorySelect.addEventListener('change', applyFilters);

    els.clearSearch.addEventListener('click', () => {
      els.searchInput.value = '';
      applyFilters();
      els.searchInput.focus();
    });

    els.filterToggle.addEventListener('click', () => {
      const willOpen = !els.filtersPanel.classList.contains('is-open');
      els.filtersPanel.classList.toggle('is-open', willOpen);
      els.filterToggle.setAttribute('aria-expanded', String(willOpen));
    });

    els.pillNav.addEventListener('click', (event) => {
      const pill = event.target.closest('.pill');
      if (!pill) return;
      const nextCategory = pill.dataset.category || '';
      if (els.categorySelect.querySelector(`option[value="${CSS.escape(nextCategory)}"]`)) {
        els.categorySelect.value = nextCategory;
      }
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

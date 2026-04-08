import { adminApi, uiState } from '/qrventure/js/admin-api.js';

const schemas = {
  attractions: {
    title: 'Attraction',
    listMeta: (item) => `${item.category} · ${item.status}`,
    fields: [
      ['slug', 'text'], ['name', 'text'], ['shortDescription', 'textarea'], ['fullDescription', 'textarea'],
      ['category', 'text'], ['locationText', 'text'], ['openingHours', 'text'], ['entranceFee', 'text'],
      ['contactDetails', 'text'], ['latitude', 'number'], ['longitude', 'number'], ['imagePath', 'text'],
      ['status', 'text'], ['isFeatured', 'checkbox']
    ],
    defaults: { status: 'open', isFeatured: false, latitude: 14.0, longitude: 120.0 }
  },
  dining: {
    title: 'Dining',
    listMeta: (item) => `${item.cuisineOrType} · ${item.priceRange}`,
    fields: [
      ['slug', 'text'], ['name', 'text'], ['description', 'textarea'], ['cuisineOrType', 'text'],
      ['locationText', 'text'], ['openingHours', 'text'], ['priceRange', 'text'], ['contactDetails', 'text'],
      ['latitude', 'number'], ['longitude', 'number'], ['imagePath', 'text'], ['isFeatured', 'checkbox']
    ],
    defaults: { isFeatured: false, latitude: 14.0, longitude: 120.0 }
  },
  services: {
    title: 'Service',
    listMeta: (item) => `${item.serviceType} · ${item.operatingHours}`,
    fields: [
      ['slug', 'text'], ['name', 'text'], ['description', 'textarea'], ['serviceType', 'text'],
      ['locationText', 'text'], ['operatingHours', 'text'], ['contactDetails', 'text'],
      ['latitude', 'number'], ['longitude', 'number'], ['nearbyLandmarkNotes', 'textarea'], ['travelTips', 'textarea'],
      ['imagePath', 'text'], ['isFeatured', 'checkbox']
    ],
    defaults: { isFeatured: false, latitude: 14.0, longitude: 120.0 }
  },
  routes: {
    title: 'Route',
    listMeta: (item) => `${item.durationText} · ${item.startPoint}`,
    fields: [
      ['slug', 'text'], ['name', 'text'], ['durationText', 'text'], ['startPoint', 'text'],
      ['routeDescription', 'textarea'], ['distanceKm', 'number'], ['highlights', 'textarea'], ['isFeatured', 'checkbox']
    ],
    defaults: { isFeatured: false, distanceKm: 1.0 }
  }
};

const state = { entity: 'attractions', records: [], selected: null };

const refs = {
  adminStatus: document.getElementById('adminStatus'),
  logoutBtn: document.getElementById('logoutBtn'),
  entitySelect: document.getElementById('entitySelect'),
  searchInput: document.getElementById('searchInput'),
  refreshBtn: document.getElementById('refreshBtn'),
  createBtn: document.getElementById('createBtn'),
  listState: document.getElementById('listState'),
  records: document.getElementById('records'),
  editorTitle: document.getElementById('editorTitle'),
  editorForm: document.getElementById('editorForm'),
  fileInput: document.getElementById('fileInput'),
  uploadBtn: document.getElementById('uploadBtn'),
  saveBtn: document.getElementById('saveBtn'),
  deleteBtn: document.getElementById('deleteBtn'),
  cancelBtn: document.getElementById('cancelBtn'),
  formState: document.getElementById('formState')
};

const asLabel = (field) => field.replace(/([A-Z])/g, ' $1').replace(/^./, (s) => s.toUpperCase());
const selectedKey = (item) => item.slug || item.id;

const normalize = (field, type, value) => {
  if (type === 'checkbox') return Boolean(value);
  if (type === 'number') return Number(value);
  return String(value).trim();
};

const renderRecordList = () => {
  refs.records.innerHTML = '';
  if (!state.records.length) {
    uiState.render(refs.records, 'No records found for this type.');
    return;
  }

  refs.records.innerHTML = state.records.map((item) => {
    const isActive = state.selected && selectedKey(state.selected) === selectedKey(item);
    return `<article class="admin-item ${isActive ? 'selected' : ''}" data-key="${selectedKey(item)}">
      <p><strong>${item.name}</strong></p>
      <p class="meta">${schemas[state.entity].listMeta(item)}</p>
      <p class="meta">slug: ${item.slug}</p>
    </article>`;
  }).join('');
};

const renderForm = (item = null) => {
  const schema = schemas[state.entity];
  refs.editorTitle.textContent = item ? `Edit ${schema.title}` : `Create ${schema.title}`;
  refs.deleteBtn.disabled = !item;

  const model = item || { ...schema.defaults };
  refs.editorForm.innerHTML = schema.fields.map(([field, type]) => {
    const value = model[field];
    if (type === 'textarea') {
      return `<label class="field">${asLabel(field)}<textarea class="input" name="${field}" required>${value ?? ''}</textarea></label>`;
    }
    if (type === 'checkbox') {
      return `<label class="field"><span>${asLabel(field)}</span><input type="checkbox" name="${field}" ${value ? 'checked' : ''}></label>`;
    }
    return `<label class="field">${asLabel(field)}<input class="input" type="${type}" step="${type === 'number' ? 'any' : ''}" name="${field}" value="${value ?? ''}" required></label>`;
  }).join('');
};

const collectPayload = () => {
  const schema = schemas[state.entity];
  const payload = {};
  schema.fields.forEach(([field, type]) => {
    const input = refs.editorForm.elements.namedItem(field);
    const raw = type === 'checkbox' ? input.checked : input.value;
    payload[field] = normalize(field, type, raw);
  });
  return payload;
};

const loadRecords = async () => {
  const search = refs.searchInput.value.trim();
  const query = new URLSearchParams();
  if (search.length >= 2) query.set('q', search);

  refs.records.innerHTML = '<div class="card skeleton"></div><div class="card skeleton"></div>';
  uiState.render(refs.listState, 'Loading records…');
  try {
    state.records = await adminApi.list(state.entity, query.toString());
    uiState.render(refs.listState, '');
    if (state.selected) {
      const current = state.records.find((item) => selectedKey(item) === selectedKey(state.selected));
      state.selected = current || null;
    }
    renderRecordList();
    if (state.selected) renderForm(state.selected);
  } catch (error) {
    state.records = [];
    uiState.render(refs.records, error.message || 'Failed to load records.', true);
    uiState.render(refs.listState, '', false);
  }
};

const resetEditor = () => {
  state.selected = null;
  renderForm();
  renderRecordList();
  uiState.render(refs.formState, '');
};

refs.records.addEventListener('click', (event) => {
  const target = event.target.closest('[data-key]');
  if (!target) return;
  const key = target.getAttribute('data-key');
  const item = state.records.find((record) => String(selectedKey(record)) === key);
  if (!item) return;
  state.selected = item;
  renderRecordList();
  renderForm(item);
  uiState.render(refs.formState, '');
});

refs.entitySelect.addEventListener('change', async () => {
  state.entity = refs.entitySelect.value;
  state.selected = null;
  renderForm();
  await loadRecords();
});

refs.refreshBtn.addEventListener('click', loadRecords);
refs.createBtn.addEventListener('click', resetEditor);
refs.cancelBtn.addEventListener('click', resetEditor);

refs.editorForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const payload = collectPayload();
  refs.saveBtn.disabled = true;
  uiState.render(refs.formState, 'Saving…');

  try {
    if (state.selected) {
      await adminApi.update(state.entity, selectedKey(state.selected), payload);
      uiState.render(refs.formState, 'Record updated successfully.');
    } else {
      await adminApi.create(state.entity, payload);
      uiState.render(refs.formState, 'Record created successfully.');
    }
    await loadRecords();
    resetEditor();
  } catch (error) {
    uiState.render(refs.formState, error.message || 'Save failed.', true);
  } finally {
    refs.saveBtn.disabled = false;
  }
});

refs.deleteBtn.addEventListener('click', async () => {
  if (!state.selected) return;
  refs.deleteBtn.disabled = true;
  uiState.render(refs.formState, 'Deleting…');
  try {
    await adminApi.remove(state.entity, selectedKey(state.selected));
    uiState.render(refs.formState, 'Record deleted successfully.');
    state.selected = null;
    await loadRecords();
    renderForm();
  } catch (error) {
    uiState.render(refs.formState, error.message || 'Delete failed.', true);
  } finally {
    refs.deleteBtn.disabled = !state.selected;
  }
});

refs.uploadBtn.addEventListener('click', async () => {
  const file = refs.fileInput.files?.[0];
  if (!file) {
    uiState.render(refs.formState, 'Please select an image file to upload.', true);
    return;
  }
  refs.uploadBtn.disabled = true;
  uiState.render(refs.formState, 'Uploading image…');
  try {
    const upload = await adminApi.upload(file);
    const imageInput = refs.editorForm.elements.namedItem('imagePath');
    if (imageInput) imageInput.value = upload.imagePath;
    uiState.render(refs.formState, `Image uploaded: ${upload.imagePath}`);
  } catch (error) {
    uiState.render(refs.formState, error.message || 'Upload failed.', true);
  } finally {
    refs.uploadBtn.disabled = false;
  }
});

refs.logoutBtn.addEventListener('click', async () => {
  await adminApi.logout().catch(() => {});
  window.location.href = '/admin/login';
});

const initialize = async () => {
  try {
    const session = await adminApi.me();
    refs.adminStatus.textContent = `Signed in as ${session.username}.`;
    renderForm();
    await loadRecords();
  } catch (_) {
    window.location.href = '/admin/login';
  }
};

initialize();

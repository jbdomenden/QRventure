import { adminApi, uiState } from '/qrventure/js/admin-api.js';

const schemas = {
  attractions: {
    title: 'Attraction',
    listMeta: (i) => `${i.category} · ${i.status}`,
    fields: [
      ['slug', 'text'], ['name', 'text'], ['shortDescription', 'textarea'], ['fullDescription', 'textarea'],
      ['category', 'text'], ['historicalPeriod', 'text'], ['locationText', 'text'], ['openingHours', 'text'],
      ['entranceFee', 'text'], ['contactDetails', 'text'], ['visitorTips', 'textarea'], ['bestTimeToVisit', 'text'],
      ['latitude', 'number'], ['longitude', 'number'], ['imagePath', 'text'], ['isFeatured', 'checkbox'], ['status', 'text'], ['sortOrder', 'number']
    ],
    defaults: { isFeatured: false, status: 'open', sortOrder: 0, latitude: 14.59, longitude: 120.97 }
  },
  dining: {
    title: 'Dining',
    listMeta: (i) => `${i.diningType} · ${i.status}`,
    fields: [
      ['slug', 'text'], ['name', 'text'], ['shortDescription', 'textarea'], ['fullDescription', 'textarea'],
      ['diningType', 'text'], ['cuisine', 'text'], ['locationText', 'text'], ['openingHours', 'text'],
      ['priceRange', 'text'], ['contactDetails', 'text'], ['visitorNotes', 'textarea'], ['latitude', 'number'],
      ['longitude', 'number'], ['imagePath', 'text'], ['isFeatured', 'checkbox'], ['status', 'text'], ['sortOrder', 'number']
    ],
    defaults: { isFeatured: false, status: 'open', sortOrder: 0, latitude: 14.59, longitude: 120.97 }
  },
  services: {
    title: 'Service',
    listMeta: (i) => `${i.serviceType} · ${i.status}`,
    fields: [
      ['slug', 'text'], ['name', 'text'], ['shortDescription', 'textarea'], ['fullDescription', 'textarea'],
      ['serviceType', 'text'], ['locationText', 'text'], ['hours', 'text'], ['contactDetails', 'text'],
      ['visitorNotes', 'textarea'], ['latitude', 'number'], ['longitude', 'number'], ['imagePath', 'text'], ['status', 'text'], ['sortOrder', 'number']
    ],
    defaults: { status: 'open', sortOrder: 0, latitude: 14.59, longitude: 120.97 }
  },
  routes: {
    title: 'Route',
    listMeta: (i) => `${i.routeType} · ${i.estimatedDuration}`,
    fields: [
      ['slug', 'text'], ['name', 'text'], ['shortDescription', 'textarea'], ['fullDescription', 'textarea'],
      ['routeType', 'text'], ['startingPoint', 'text'], ['estimatedDuration', 'text'], ['travelTips', 'textarea'],
      ['distanceText', 'text'], ['mapLink', 'text'], ['isFeatured', 'checkbox'], ['status', 'text'], ['sortOrder', 'number']
    ],
    defaults: { isFeatured: false, status: 'open', sortOrder: 0 }
  }
};

const refs = {
  logoutBtn: document.getElementById('logoutBtn'),
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

const state = { entity: window.ADMIN_ENTITY || 'attractions', records: [], selected: null };
const asLabel = (f) => f.replace(/([A-Z])/g, ' $1').replace(/^./, s => s.toUpperCase());
const selectedKey = (item) => item.slug || item.id;

const renderForm = (item = null) => {
  const schema = schemas[state.entity];
  refs.editorTitle.textContent = item ? `Edit ${schema.title}` : `Create ${schema.title}`;
  refs.deleteBtn.disabled = !item;
  const model = item || { ...schema.defaults };
  refs.editorForm.innerHTML = schema.fields.map(([field, type]) => {
    const value = model[field];
    if (type === 'textarea') return `<label class="field">${asLabel(field)}<textarea class="input" name="${field}" required>${value ?? ''}</textarea></label>`;
    if (type === 'checkbox') return `<label class="field"><span>${asLabel(field)}</span><input type="checkbox" name="${field}" ${value ? 'checked' : ''}></label>`;
    return `<label class="field">${asLabel(field)}<input class="input" type="${type}" step="${type==='number'?'any':''}" name="${field}" value="${value ?? ''}" required></label>`;
  }).join('');
};

const collectPayload = () => {
  const payload = {};
  schemas[state.entity].fields.forEach(([field, type]) => {
    const input = refs.editorForm.elements.namedItem(field);
    payload[field] = type === 'checkbox' ? input.checked : type === 'number' ? Number(input.value) : String(input.value).trim();
  });
  return payload;
};

const renderRecordList = () => {
  if (!state.records.length) return uiState.render(refs.records, 'No records found.');
  refs.records.innerHTML = state.records.map((item) => {
    const active = state.selected && selectedKey(state.selected) === selectedKey(item);
    return `<article class="admin-item ${active ? 'selected' : ''}" data-key="${selectedKey(item)}"><p><strong>${item.name}</strong></p><p class="meta">${schemas[state.entity].listMeta(item)}</p><p class="meta">slug: ${item.slug}</p></article>`;
  }).join('');
};

const loadRecords = async () => {
  const search = refs.searchInput.value.trim();
  const q = new URLSearchParams();
  if (search.length >= 2) q.set('q', search);
  uiState.render(refs.listState, 'Loading…');
  try {
    state.records = await adminApi.list(state.entity, q.toString());
    uiState.render(refs.listState, '');
    renderRecordList();
  } catch (e) {
    uiState.render(refs.records, e.message, true);
  }
};

refs.records?.addEventListener('click', (e) => {
  const node = e.target.closest('[data-key]');
  if (!node) return;
  state.selected = state.records.find(r => String(selectedKey(r)) === node.dataset.key) || null;
  renderRecordList();
  renderForm(state.selected);
});

refs.editorForm?.addEventListener('submit', async (e) => {
  e.preventDefault();
  try {
    const payload = collectPayload();
    if (state.selected) await adminApi.update(state.entity, selectedKey(state.selected), payload);
    else await adminApi.create(state.entity, payload);
    uiState.render(refs.formState, 'Saved successfully.');
    state.selected = null;
    renderForm();
    await loadRecords();
  } catch (err) { uiState.render(refs.formState, err.message, true); }
});

refs.deleteBtn?.addEventListener('click', async () => {
  if (!state.selected) return;
  try {
    await adminApi.remove(state.entity, selectedKey(state.selected));
    uiState.render(refs.formState, 'Deleted successfully.');
    state.selected = null;
    renderForm();
    await loadRecords();
  } catch (err) { uiState.render(refs.formState, err.message, true); }
});

refs.refreshBtn?.addEventListener('click', loadRecords);
refs.createBtn?.addEventListener('click', () => { state.selected = null; renderForm(); });
refs.cancelBtn?.addEventListener('click', () => { state.selected = null; renderForm(); });

refs.uploadBtn?.addEventListener('click', async () => {
  const file = refs.fileInput.files?.[0];
  if (!file) return uiState.render(refs.formState, 'Select an image file first.', true);
  try {
    const up = await adminApi.upload(file);
    const image = refs.editorForm.elements.namedItem('imagePath');
    if (image) image.value = up.imagePath;
    uiState.render(refs.formState, `Uploaded: ${up.imagePath}`);
  } catch (err) { uiState.render(refs.formState, err.message, true); }
});

refs.logoutBtn?.addEventListener('click', async () => { await adminApi.logout().catch(() => {}); location.href = '/admin/login'; });

adminApi.me().then(async () => { renderForm(); await loadRecords(); }).catch(() => { location.href = '/admin/login'; });

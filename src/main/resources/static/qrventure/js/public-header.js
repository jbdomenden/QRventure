function buildHeaderLinks() {
  return `
    <nav class="public-header-links" aria-label="Primary navigation">
      <a href="/qrventure/attractions">Attractions</a>
      <a href="/qrventure/dining">Dining</a>
      <a href="/qrventure/services">Services</a>
      <a href="/qrventure/routes">Routes</a>
    </nav>
  `;
}

function mountPublicHeader() {
  const target = document.querySelector('[data-public-header]');
  if (!target) return;

  const title = target.dataset.headerTitle || 'QRventure Intramuros';
  const showLinks = target.dataset.showLinks !== 'false';
  const rightActions = target.dataset.headerActions || '';

  target.innerHTML = `
    <header class="public-header">
      <div class="container public-header-inner">
        <button class="public-back-button" type="button" aria-label="Go back">← Back</button>
        <a class="public-header-title" href="/qrventure/">${title}</a>
        <div class="public-header-actions">
          ${rightActions || (showLinks ? buildHeaderLinks() : '')}
        </div>
      </div>
    </header>
  `;

  target.querySelector('.public-back-button')?.addEventListener('click', () => {
    window.location.href = '/qrventure/';
  });
}

mountPublicHeader();

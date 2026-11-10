(function (action, selectedUrl) {
 const videos = [];
 const seen = new Set();
 for (const link of document.querySelectorAll('a[href]')) {
  const url = new URL(link.href, location.href);
  if (url.origin !== location.origin || !link.getClientRects().length) continue;
  const id = url.pathname === '/watch' ? url.searchParams.get('v')
    : url.pathname.match(/^\/shorts\/([^/]+)$/)?.[1];
  if (!id || !/^[A-Za-z0-9_-]{11}$/.test(id) || seen.has(id)) continue;
  seen.add(id);
  videos.push({link, url: 'https://youtu.be/' + id});
 }
 if (action === 'queue') {
  return {urls: videos.slice(0, 50).map(video => video.url)};
 }
 const focused = document.activeElement;
 if (focused && focused.matches('input, textarea, [contenteditable="true"]')) return null;
 if (!videos.length) return null;

 let index = videos.findIndex(video => video.link === focused);
 if (index < 0) index = videos.findIndex(video => video.url === window.iyoSelected);
 if (action === 'open') {
  const selected = videos[Math.max(0, index)];
  const start = Math.max(0, Math.max(0, index) - 10);
  return {selected: selected.url, urls: videos.slice(start, start + 50).map(video => video.url)};
 }
 const offset = action === 'next' ? 1 : -1;
 index = index < 0 ? 0 : Math.min(videos.length - 1, Math.max(0, index + offset));
 const selected = videos[index];
 const old = document.querySelector('[data-iyo-focus]');
 if (old) {
  old.style.outline = '';
  old.removeAttribute('data-iyo-focus');
 }
 selected.link.dataset.iyoFocus = '';
 selected.link.style.outline = '3px solid #b7ffbf';
 selected.link.focus({preventScroll: true});
 selected.link.scrollIntoView({block: 'center'});
 window.iyoSelected = selected.url;
 return null;
})

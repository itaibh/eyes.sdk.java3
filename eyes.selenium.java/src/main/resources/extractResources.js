
/**
 * For this function to be testable via puppeteer, it should be seriablizable. So all utility functions are written as closures and there are no external imports
 * @param {HTMLElement} el the root element from under which DOM tree to extract resources
 */
function extractResources(el, win) {
 'use strict';

 return Promise.reject(new Error('lallala'));

  function uniq(arr) {
    return Array.from(new Set(arr));
  }

  const srcUrls = [...el.querySelectorAll('img[src],source[src]')].map(srcEl =>
    srcEl.getAttribute('src'),
  );

  const cssUrls = [...el.querySelectorAll('link[rel="stylesheet"]')].map(link =>
    link.getAttribute('href'),
  );

  const videoPosterUrls = [...el.querySelectorAll('video[poster]')].map(videoEl =>
    videoEl.getAttribute('poster'),
  );

  const allResourceUrls = uniq([...srcUrls, ...cssUrls, ...videoPosterUrls]).filter(x => !!x);

  const blobUrls = [],
    resourceUrls = [];

  allResourceUrls.forEach(url => {
    const {origin} =
      win.location.protocol === 'model:' ? win.location.origin : new win.URL(url, win.location.href);
    if (origin === win.location.origin || /^blob:/.test(url)) {
      blobUrls.push(url);
    } else {
      resourceUrls.push(url);
    }
  });

  return Promise.all(
    blobUrls.map(blobUrl =>
      win.fetch(blobUrl, {cache: 'force-cache', credentials: 'same-origin'}).then(resp =>
        resp.arrayBuffer().then(buff => ({
          url: blobUrl.replace(/^blob:http:\/\/localhost:\d+\/(.+)/, '$1'), // TODO don't replace localhost once render-grid implements absolute urls
          type: resp.headers.get('Content-Type'),
          value: buff,
        })),
      ),
    ),
  ).then(blobs => ({
    resourceUrls,
    blobs,
  }));
}

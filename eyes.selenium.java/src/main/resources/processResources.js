
function __processPageAndSerialize() {
  var processPageAndSerialize = (function () {
  'use strict';

  // This code was copied and modified from https://github.com/beatgammit/base64-js/blob/bf68aaa277/index.js
  // License: https://github.com/beatgammit/base64-js/blob/bf68aaa277d9de7007cc0c58279c411bb10670ac/LICENSE

  function arrayBufferToBase64(ab) {
    const lookup = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'.split('');

    const uint8 = new Uint8Array(ab);
    const len = uint8.length;
    const extraBytes = len % 3; // if we have 1 byte left, pad 2 bytes
    const parts = [];
    const maxChunkLength = 16383; // must be multiple of 3

    let tmp;

    // go through the array every three bytes, we'll deal with trailing stuff later
    for (let i = 0, len2 = len - extraBytes; i < len2; i += maxChunkLength) {
      parts.push(encodeChunk(i, i + maxChunkLength > len2 ? len2 : i + maxChunkLength));
    }

    // pad the end with zeros, but make sure to not forget the extra bytes
    if (extraBytes === 1) {
      tmp = uint8[len - 1];
      parts.push(lookup[tmp >> 2] + lookup[(tmp << 4) & 0x3f] + '==');
    } else if (extraBytes === 2) {
      tmp = (uint8[len - 2] << 8) + uint8[len - 1];
      parts.push(lookup[tmp >> 10] + lookup[(tmp >> 4) & 0x3f] + lookup[(tmp << 2) & 0x3f] + '=');
    }

    return parts.join('');

    function tripletToBase64(num) {
      return (
        lookup[(num >> 18) & 0x3f] +
        lookup[(num >> 12) & 0x3f] +
        lookup[(num >> 6) & 0x3f] +
        lookup[num & 0x3f]
      );
    }

    function encodeChunk(start, end) {
      let tmp;
      const output = [];
      for (let i = start; i < end; i += 3) {
        tmp = ((uint8[i] << 16) & 0xff0000) + ((uint8[i + 1] << 8) & 0xff00) + (uint8[i + 2] & 0xff);
        output.push(tripletToBase64(tmp));
      }
      return output.join('');
    }
  }

  var arrayBufferToBase64_1 = arrayBufferToBase64;

  function extractLinks(doc = document) {
    const srcsetUrls = [...doc.querySelectorAll('img[srcset],source[srcset]')]
      .map(srcsetEl =>
        srcsetEl
          .getAttribute('srcset')
          .split(',')
          .map(str => str.trim().split(/\s+/)[0]),
      )
      .reduce((acc, urls) => acc.concat(urls), []);

    const srcUrls = [...doc.querySelectorAll('img[src],source[src]')].map(srcEl =>
      srcEl.getAttribute('src'),
    );

    const cssUrls = [...doc.querySelectorAll('link[rel="stylesheet"]')].map(link =>
      link.getAttribute('href'),
    );

    const videoPosterUrls = [...doc.querySelectorAll('video[poster]')].map(videoEl =>
      videoEl.getAttribute('poster'),
    );

    return [...srcsetUrls, ...srcUrls, ...cssUrls, ...videoPosterUrls];
  }

  var extractLinks_1 = extractLinks;

  /* eslint-disable no-use-before-define */

  function domNodesToCdt(docNode) {
    const NODE_TYPES = {
      ELEMENT: 1,
      TEXT: 3,
      DOCUMENT: 9,
      DOCUMENT_TYPE: 10,
    };

    const domNodes = [
      {
        nodeType: NODE_TYPES.DOCUMENT,
      },
    ];
    domNodes[0].childNodeIndexes = childrenFactory(domNodes, docNode.childNodes);
    return domNodes;

    function childrenFactory(domNodes, elementNodes) {
      if (!elementNodes || elementNodes.length === 0) return null;

      const childIndexes = [];
      elementNodes.forEach(elementNode => {
        const index = elementNodeFactory(domNodes, elementNode);
        if (index !== null) {
          childIndexes.push(index);
        }
      });

      return childIndexes;
    }

    function elementNodeFactory(domNodes, elementNode) {
      let node;
      const {nodeType} = elementNode;
      if (nodeType === NODE_TYPES.ELEMENT) {
        if (elementNode.nodeName !== 'SCRIPT') {
          if (
            elementNode.nodeName === 'STYLE' &&
            !elementNode.textContent &&
            elementNode.sheet &&
            elementNode.sheet.cssRules.length
          ) {
            elementNode.appendChild(
              docNode.createTextNode(
                [...elementNode.sheet.cssRules].map(rule => rule.cssText).join(''),
              ),
            );
          }

          node = {
            nodeType: NODE_TYPES.ELEMENT,
            nodeName: elementNode.nodeName,
            attributes: Object.keys(elementNode.attributes).map(key => {
              let value = elementNode.attributes[key].value;
              const name = elementNode.attributes[key].localName;

              if (/^blob:/.test(value)) {
                value = value.replace(/^blob:http:\/\/localhost:\d+\/(.+)/, '$1'); // TODO don't replace localhost once render-grid implements absolute urls
              }

              return {
                name,
                value,
              };
            }),
            childNodeIndexes: elementNode.childNodes.length
              ? childrenFactory(domNodes, elementNode.childNodes)
              : [],
          };
        }
      } else if (nodeType === NODE_TYPES.TEXT) {
        node = {
          nodeType: NODE_TYPES.TEXT,
          nodeValue: elementNode.nodeValue,
        };
      } else if (nodeType === NODE_TYPES.DOCUMENT_TYPE) {
        node = {
          nodeType: NODE_TYPES.DOCUMENT_TYPE,
          nodeName: elementNode.nodeName,
        };
      }

      if (node) {
        domNodes.push(node);
        return domNodes.length - 1;
      } else {
        // console.log(`Unknown nodeType: ${nodeType}`);
        return null;
      }
    }
  }

  var domNodesToCdt_1 = domNodesToCdt;
  var NODE_TYPES = {
    ELEMENT: 1,
    TEXT: 3,
    DOCUMENT: 9,
    DOCUMENT_TYPE: 10,
  };
  domNodesToCdt_1.NODE_TYPES = NODE_TYPES;

  function extractFrames(doc = document) {
    return [...doc.querySelectorAll('iframe[src]:not([src=""]):not([src^="about:"]')]
      .map(srcEl => {
        try {
          const contentDoc = srcEl.contentDocument;
          return (
            contentDoc && contentDoc.defaultView && contentDoc.defaultView.frameElement && contentDoc
          );
        } catch (err) {
          //for CORS frames
        }
      })
      .filter(x => !!x);
  }

  var extractFrames_1 = extractFrames;

  function uniq(arr) {
    return Array.from(new Set(arr)).filter(x => !!x);
  }

  var uniq_1 = uniq;

  function aggregateResourceUrlsAndBlobs(resourceUrlsAndBlobsArr, initialValue) {
    return resourceUrlsAndBlobsArr.reduce(
      ({resourceUrls: allResourceUrls, blobsObj: allBlobsObj}, {resourceUrls, blobsObj}) => ({
        resourceUrls: uniq_1(allResourceUrls.concat(resourceUrls)),
        blobsObj: Object.assign(allBlobsObj, blobsObj),
      }),
      initialValue,
    );
  }

  var aggregateResourceUrlsAndBlobs_1 = aggregateResourceUrlsAndBlobs;

  function isSameOrigin(url, baseUrl) {
    const blobOrData = /^(blob|data):/;
    if (blobOrData.test(url)) return true;
    if (blobOrData.test(baseUrl)) return false;

    const {origin} = new URL(url, baseUrl);
    const {origin: baseOrigin} = new URL(baseUrl);
    return origin === baseOrigin;
  }

  var isSameOrigin_1 = isSameOrigin;

  function splitOnOrigin(urls, baseUrl) {
    const result = urls.reduce(
      ({internalUrls, externalUrls}, url) => {
        if (isSameOrigin_1(url, baseUrl)) {
          internalUrls.push(url);
        } else {
          externalUrls.push(url);
        }
        return {internalUrls, externalUrls};
      },
      {externalUrls: [], internalUrls: []},
    );
    return result;
  }

  var splitOnOrigin_1 = splitOnOrigin;

  function makeGetResourceUrlsAndBlobs({processResource, aggregateResourceUrlsAndBlobs}) {
    return function getResourceUrlsAndBlobs(doc, baseUrl, absoluteUrls) {
      const {externalUrls, internalUrls} = splitOnOrigin_1(absoluteUrls, baseUrl);
      return Promise.all(
        internalUrls.map(url =>
          processResource(url, doc, getResourceUrlsAndBlobs.bind(null, doc, baseUrl)),
        ),
      ).then(resourceUrlsAndBlobsArr =>
        aggregateResourceUrlsAndBlobs(resourceUrlsAndBlobsArr, {
          resourceUrls: externalUrls,
          blobsObj: {},
        }),
      );
    };
  }

  var getResourceUrlsAndBlobs = makeGetResourceUrlsAndBlobs;

  function filterDataUrl(url) {
    return !/^data:/.test(url);
  }

  var filterDataUrl_1 = filterDataUrl;

  function absolutizeUrl(url, absoluteUrl) {
    return new URL(url, absoluteUrl).href;
  }

  var absolutizeUrl_1 = absolutizeUrl;

  function makeProcessResource({
    fetchUrl,
    findStyleSheetByUrl,
    extractResourcesFromStyleSheet,
    cache = {},
  }) {
    return function processResource(absoluteUrl, doc, getResourceUrlsAndBlobs) {
      return cache[absoluteUrl] || (cache[absoluteUrl] = doProcessResource(absoluteUrl));

      function doProcessResource(url) {
        return fetchUrl(url)
          .then(({url, type, value}) => {
            const result = {blobsObj: {[url]: {type, value}}};
            if (/text\/css/.test(type)) {
              const styleSheet = findStyleSheetByUrl(url, doc);
              if (!styleSheet) {
                return result;
              }
              const resourceUrls = extractResourcesFromStyleSheet(styleSheet, doc.defaultView)
                .filter(filterDataUrl_1)
                .map(resourceUrl => absolutizeUrl_1(resourceUrl, url.replace(/^blob:/, '')));
              return getResourceUrlsAndBlobs(resourceUrls).then(({resourceUrls, blobsObj}) => ({
                resourceUrls,
                blobsObj: Object.assign(blobsObj, {[url]: {type, value}}),
              }));
            } else {
              return result;
            }
          })
          .catch(err => {
            console.log('[dom-capture] error while fetching', url, err);
            return {};
          });
      }
    };
  }

  var processResource = makeProcessResource;

  /* global window */

  function fetchUrl(url, fetch = window.fetch) {
    return fetch(url, {cache: 'force-cache', credentials: 'same-origin'}).then(resp =>
      resp.arrayBuffer().then(buff => ({
        url,
        type: resp.headers.get('Content-Type'),
        value: buff,
      })),
    );
  }

  var fetchUrl_1 = fetchUrl;

  function findStyleSheetByUrl(url, doc) {
    return [...doc.styleSheets].find(styleSheet => styleSheet.href === url);
  }

  var findStyleSheetByUrl_1 = findStyleSheetByUrl;

  function getUrlFromCssText(cssText) {
    const re = /url\((?!['"]?:)['"]?([^'")]*)['"]?\)/g;
    const ret = [];
    let result;
    while ((result = re.exec(cssText)) !== null) {
      ret.push(result[1]);
    }
    return ret;
  }

  var getUrlFromCssText_1 = getUrlFromCssText;

  // TODO share this code with the node part of visual-grid-client
  function extractResourcesFromStyleSheet(styleSheet, win = window) {
    return uniq_1(
      [...styleSheet.cssRules].reduce((acc, rule) => {
        if (rule instanceof win.CSSImportRule) {
          return acc.concat(rule.href);
        } else if (rule instanceof win.CSSFontFaceRule) {
          return acc.concat(getUrlFromCssText_1(rule.style.getPropertyValue('src')));
        } else if (rule instanceof win.CSSSupportsRule || rule instanceof win.CSSMediaRule) {
          return acc.concat(extractResourcesFromStyleSheet(rule));
        } else if (rule instanceof win.CSSStyleRule) {
          for (let i = 0, ii = rule.style.length; i < ii; i++) {
            const urls = getUrlFromCssText_1(rule.style.getPropertyValue(rule.style[i]));
            urls.length && (acc = acc.concat(urls));
          }
        }
        return acc;
      }, []),
    );
  }

  var extractResourcesFromStyleSheet_1 = extractResourcesFromStyleSheet;

  function extractResourceUrlsFromStyleAttrs(cdt) {
    return cdt.reduce((acc, node) => {
      if (node.nodeType === 1) {
        const styleAttr =
          node.attributes && node.attributes.find(attr => attr.name.toUpperCase() === 'STYLE');

        if (styleAttr) acc = acc.concat(getUrlFromCssText_1(styleAttr.value));
      }
      return acc;
    }, []);
  }

  var extractResourceUrlsFromStyleAttrs_1 = extractResourceUrlsFromStyleAttrs;

  function makeExtractResourceUrlsFromStyleTags(extractResourcesFromStyleSheet) {
    return function extractResourceUrlsFromStyleTags(doc) {
      return uniq_1(
        [...doc.getElementsByTagName('style')].reduce((resourceUrls, styleEl) => {
          const styleSheet = [...doc.styleSheets].find(
            styleSheet => styleSheet.ownerNode === styleEl,
          );
          return resourceUrls.concat(extractResourcesFromStyleSheet(styleSheet, doc.defaultView));
        }, []),
      );
    };
  }

  var extractResourceUrlsFromStyleTags = makeExtractResourceUrlsFromStyleTags;

  function processPage(doc = document) {
    const processResource$$1 = processResource({
      fetchUrl: fetchUrl_1,
      findStyleSheetByUrl: findStyleSheetByUrl_1,
      extractResourcesFromStyleSheet: extractResourcesFromStyleSheet_1,
      absolutizeUrl: absolutizeUrl_1,
    });

    const getResourceUrlsAndBlobs$$1 = getResourceUrlsAndBlobs({
      processResource: processResource$$1,
      aggregateResourceUrlsAndBlobs: aggregateResourceUrlsAndBlobs_1,
    });

    const extractResourceUrlsFromStyleTags$$1 = extractResourceUrlsFromStyleTags(
      extractResourcesFromStyleSheet_1,
    );

    return doProcessPage(doc);

    function doProcessPage(doc) {
      const frameElement = doc.defaultView && doc.defaultView.frameElement;
      const url = frameElement ? frameElement.src : doc.location.href;

      const cdt = domNodesToCdt_1(doc);

      const links = uniq_1(
        extractLinks_1(doc)
          .concat(extractResourceUrlsFromStyleAttrs_1(cdt))
          .concat(extractResourceUrlsFromStyleTags$$1(doc))
          .filter(filterDataUrl_1),
      )
        .map(absolutizeThisUrl)
        .filter(x => !!x);
      const resourceUrlsAndBlobsPromise = getResourceUrlsAndBlobs$$1(doc, url, links);

      const frameDocs = extractFrames_1(doc);
      const processFramesPromise = frameDocs.map(doProcessPage);

      return Promise.all([resourceUrlsAndBlobsPromise, ...processFramesPromise]).then(
        ([{resourceUrls, blobsObj}, ...framesResults]) => ({
          cdt,
          url,
          resourceUrls,
          blobs: blobsObjToArray(blobsObj),
          frames: framesResults,
          srcAttr: frameElement ? frameElement.getAttribute('src') : undefined,
        }),
      );

      function absolutizeThisUrl(someUrl) {
        try {
          return absolutizeUrl_1(someUrl, url);
        } catch (err) {
          // can't do anything with a non-absolute url
        }
      }
    }
  }

  function blobsObjToArray(blobsObj) {
    return Object.keys(blobsObj).map(blobUrl =>
      Object.assign(
        {
          url: blobUrl.replace(/^blob:/, ''),
        },
        blobsObj[blobUrl],
      ),
    );
  }

  var processPage_1 = processPage;

  function processPageAndSerialize(doc) {
    return processPage_1(doc).then(serializeFrame);
  }

  function serializeFrame(frame) {
    frame.blobs = frame.blobs.map(({url, type, value}) => ({
      url,
      type,
      value: arrayBufferToBase64_1(value),
    }));
    frame.frames.forEach(serializeFrame);
    return frame;
  }

  var processPageAndSerialize_1 = processPageAndSerialize;

  return processPageAndSerialize_1;

}());

  return processPageAndSerialize.apply(this, arguments);
}
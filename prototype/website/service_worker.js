/*
 * Service worker.
 *
 * - prefer network.
 * - fallback on fail.
 */
let CACHE_NAME = 'bunny-cache-v22';
let urlsToCache = ['/', './index.html'];

let failures = 0;
let offline = false;

console.log('using service worker cache ' + CACHE_NAME);

self.addEventListener('install', (event) => {
    // remove all old caches, because reasons.
    for (let i = 0; i < 1000; i++) {
        caches.delete(`bunny-cache-v${i}`);
    }

    self.skipWaiting();
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then((cache) => {
                return cache.addAll(urlsToCache);
            })
    );
});

self.addEventListener('activate', () => {
    console.log('service worker activated on ' + CACHE_NAME);
});

self.addEventListener('message', (event) => {
    console.log("service worker query: " + event.data);
    event.ports[0].postMessage({offline: offline});
});

self.addEventListener('fetch', (event) => {
    if (offline) {
        event.respondWith(caches.open(CACHE_NAME)
            .then(cache => {
                return caches.match(event.request)
            })
        );
    } else {
        event.respondWith(
            caches.open(CACHE_NAME)
                .then(cache => {
                    return fetch(event.request)
                        .then(response => {
                            let clone = response.clone();

                            cache.match(event.request)
                                .then(() => {
                                    cache.put(event.request, clone);
                                });
                            return response;
                        })
                        .catch(() => {
                            failures++;
                            if (!offline && failures > 8) {
                                console.log('offline detected: defaulting to cache.');
                                offline = true;
                            }
                            return caches.match(event.request);
                        })

                })
        );
    }
});
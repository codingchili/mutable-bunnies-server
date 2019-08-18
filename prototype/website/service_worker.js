/*
 * Service worker.
 *
 * - prefer network.
 * - fallback on fail.
 */
const CACHE_NAME = 'bunny-cache-v22';
const urlsToCache = ['/', './index.html'];
const OFFLINE_LATCH_COUNT = 8;

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

self.addEventListener('activate', event => {
    console.log('service worker activated on ' + CACHE_NAME);
});

self.addEventListener('message', (event) => {
    console.log("service worker query: " + event.data);
    event.ports[0].postMessage({offline: offline});
});

self.addEventListener('fetch', (event) => {

    if (event.request.method !== "GET") {
        return false;
    }

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
                            if (!offline && failures > OFFLINE_LATCH_COUNT) {
                                console.log('offline detected: defaulting to cache.');
                                offline = true;

                                setTimeout(() => {
                                    // reset circuit breaker because page reload isn't enough to reset vars.
                                    offline = false;
                                    failures = 0;
                                }, 10000);
                            }
                            return caches.match(event.request);
                        })

                })
        );
    }
});
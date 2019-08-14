// may load multiple resources at once: but resources added after the load call starts
window.AssetLoader = class AssetLoader {

    constructor() {
        this.callbacks = [];
        this.completers = [];
        this.loading = false;
        this.queue = [];
        this.resources = PIXI.Loader.shared.resources;
    }

    load(callback, assetName) {
        this.queue.push({'assetName': assetName, "callback": callback});
        return this;
    }

    begin(callback) {
        if (callback) {
            this.callbacks.push(callback);
        }

        if (!this.loading) {
            this.completers = this.callbacks;
            this.callbacks = [];
            this.processQueue();
        }
    }

    processQueue() {
        this.loading = true;
        this.processing = this.queue;
        this.queue = [];

        let loader = PIXI.Loader.shared.pre((res, next) => {
            // load files that has been loaded by the patcher.
            if (patch.files[res.url] !== undefined) {
                res.xhr = patch.files[res.url].xhr;
                res.xhrType = 'blob';
                res.data = patch.files[res.url].data;
                res.complete();
            } else {
                // if not loaded by patcher perform on-demand xhr load.
                if (!res.url.startsWith(application.realm.resources)) {
                    res.url = application.realm.resources + res.url;
                }
            }
            next();
        });

        let isAllLoaded = true;

        this.processing.forEach((asset) => {
            let resource = PIXI.Loader.shared.resources[asset.assetName];
            if (!resource) {
                loader.add(asset.assetName);
                isAllLoaded = false;
            }
        });

        // if all the assets are loaded - make sure to mark as complete.
        if (isAllLoaded) {
            this.completed();
        }

        loader.load(() => {
            for (let i in this.processing) {
                let current = this.processing[i];
                let resource;

                if (current.assetName.endsWith('.json')) {
                    resource = PIXI.Loader.shared.resources[current.assetName].data;
                } else {
                    resource = new PIXI.Sprite(PIXI.Loader.shared.resources[current.assetName].texture);
                }
                current.callback(resource);
            }
            this.loading = false;
            if (this.queue.length > 0) {
                this.completed();
                this.processQueue();
            }
        });
    }

    completed() {
        this.loading = false;
        this.completers.forEach((completer) => {
            completer();
        });
    }
};

var Loader = new AssetLoader();
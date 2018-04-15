
// may load multiple resources at once: but resources added after the load call starts
class AssetLoader {

    constructor() {
        this.loading = false;
        this.queue = [];
    }

    load(callback, assetName) {
        this.queue.push({'assetName': assetName, "callback": callback});
        return this;
    }

    begin() {
        if (!this.loading) {
            this.processQueue();
        }
    }

    processQueue() {
        this.loading = true;
        this.processing = this.queue;
        this.queue = [];

        let loader = PIXI.loader.pre((res, next) => {
            // load files that has been loaded by the patcher.
            if (patch.files[res.url] != undefined) {
                res.xhr = patch.files[res.url].xhr;
                res.xhrType = 'blob';
                res.data = patch.files[res.url].data;
                res.complete();
            } else {
                // if not loaded by patcher perform on-demand xhr load.
                res.url = "/resources/" + res.url;
            }
            next();
        });

        let isAllLoaded = true;
        for (let i in this.processing) {
            let asset = this.processing[i];
            if (PIXI.loader.resources[asset.assetName]) {
                // already loaded: call the callback instantly..
                let sprite = new PIXI.Sprite(PIXI.loader.resources[asset.assetName].texture);
                asset.callback(sprite);
            } else {
                loader.add(asset.assetName);
                isAllLoaded = false;
            }
        }
        // if all the assets are loaded - make sure to mark as complete.
        if (isAllLoaded) {
            this.loading = false;
        }

        loader.load(() => {
            for (let i in this.processing) {
                let current = this.processing[i];
                let sprite = new PIXI.Sprite(PIXI.loader.resources[current.assetName].texture);
                current.callback(sprite);
            }
            this.loading = false;
            if (this.queue.length > 0) {
                this.processQueue();
            }
        });
    }
}

var assetLoader = new AssetLoader();
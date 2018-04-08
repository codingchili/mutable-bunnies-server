/**
 * @author Robin Duda
 *
 * Handles the loading of game resources.
 */
class Patcher {

    check(callback) {
        this.callback = callback;
        this.reset();
    }

    load(done, patch, url) {
        this.patch = patch;
        this.patch.count = Object.keys(patch.files).length;
        this.index = 0;
        this.transferred = 0;
        this.downloaded = 0;
        this.chunks = 0;
        this.delta = performance.now() - 1000;
        this.url = url;

        this.patchSize(done);
    }

    update(worker) {
        const patch = this.patch;
        this.worker = worker;
        worker.started(patch.name, patch.version, patch.size, patch.files);

        if (this.patch.count > 0) {
            this.download(Object.keys(patch.files)[this.index]);
        } else {
            this.worker.completed();
        }
    }

    patchSize(done) {
        let latch = this.patch.count;
        let patch = this.patch;
        patch.size = 0;

        function countdown(file) {
            patch.size += file.size;
            latch--;
            if (latch == 0) {
                done();
            }
        }

        Object.keys(patch.files).forEach((key, index) => {
           let file = patch.files[key];

           if (file.size == undefined) {
               let xhr = new XMLHttpRequest();
               xhr.open("HEAD", this.url + key, true);
               xhr.onreadystatechange = () => {
                   if (xhr.readyState == 2) {
                       file.size = parseInt(xhr.getResponseHeader("Content-Length"));
                       countdown(file);
                   }
               };
               xhr.send();
           } else {
               countdown(file);
           }
        });
    }

    download(fileName) {
        const xhr = new XMLHttpRequest();
        this.patch.files[fileName].xhr = xhr;
        xhr.open('GET', this.url + fileName, true);
        xhr.responseType = 'blob';

        this.downloaded = 0;
        xhr.onload = (event) => this.completeHandler(event);
        xhr.addEventListener('progress', (event) => this.progressHandler(event));
        xhr.onreadystatechange = (event) => this.errorHandler(event);
        xhr.send();
    }

    progressHandler(event) {
        this.chunks += (event.loaded - this.downloaded);
        this.transferred += (event.loaded - this.downloaded);
        this.downloaded = event.loaded;

        if ((performance.now() - this.delta) >= 1000) {
            this.bandwidth = this.chunks * 1000;
            this.delta = performance.now();
            this.chunks = 0;
        }

        this.worker.progress(
            parseFloat(this.bandwidth).toFixed(2),
            this.transferred,
            this.downloaded,
            this.index
        );
    }

    completeHandler(event) {
        if (event.target.status === 200) {
            let file = this.patch.files[Object.keys(this.patch.files)[this.index]];
            file.data = event.target.response;

            this.index += 1;

            if (this.index < this.patch.count) {
                this.download(Object.keys(this.patch.files)[this.index]);
            } else {
                this.worker.completed(file);
            }
        }
    }

    errorHandler(event) {
        if (event.target.status === 409) {
            this.reset();
        } else if (event.target.status === 404) {
            application.error("Failed to retrieve file.");
        }
    }
}

var patcher = new Patcher();
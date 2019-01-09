/**
 * @author Robin Duda
 *
 * Handles the loading of game resources.
 */
class Patcher {

    load(callback, patch, url) {
        this.patch = patch;
        this.index = 0;
        this.transferred = 0;
        this.downloaded = 0;
        this.chunks = 0;
        this.delta = performance.now() - 1000;
        this.url = url;

        // copy files from the executables into the download section.
        for (let i = 0; i < patch.executable.length; i++) {
            if (!patch.files[patch.executable[i]]) {
                patch.files[patch.executable[i]] = {};
            }
        }

        this.patch.count = Object.keys(patch.files).length;

        if (this.isUpToDate(patch.version)) {
            callback.upToDate();
        } else {
            this.patchSize(callback.beginUpdate);
        }
    }

    update(worker) {
        this.worker = worker;

        if (this.patch.version === this.getVersion()) {
            this.patch.files = this.getFiles(worker);
            this.worker.completed();
        } else {
            localStorage.clear();
            worker.started(this.patch.name, this.patch.version, this.patch.size, this.patch.files);

            if (this.patch.count > 0) {
                this.download(Object.keys(this.patch.files)[this.index]);
            } else {
                this.worker.completed();
            }
        }
    }

    isUpToDate(version) {
        return this.getVersion() === version;
    }

    getVersion() {
        return localStorage.getItem("version@"  + this.url);
    }

    setVersion(version) {
        localStorage.setItem("version@" + this.url, version);
    }

    getFiles(worker) {
        let files = JSON.parse(localStorage.getItem("files@" + this.url));
        this.patch.files = files;
        this.patch.size = 0;
        let transferred = 0;

        for (let key in files) {
            this.patch.size += files[key].data.length;
        }

        worker.started(this.patch.name, this.patch.version, this.patch.size, this.patch.files);

        let i = 0;
        for (let key in files) {
            i++;
            let size = files[key].data.length;
            files[key].data = this.dataURIToBlob(files[key].data);
            transferred += size;
            // does not emit bandwidth or downloaded per file.
            worker.progress(0, transferred, 0, Object.keys(files).indexOf(files[key].name));
        }
        return files;
    }

    setFiles(files) {
        let i = 0;
        let save = {};
        for (let key in files) {
            let file = {};
            file.name = files[key].name;
            file.xhr = {};
            file.xhrType = 'application/blob';

            let reader = new FileReader();
             reader.readAsDataURL(files[key].data);
             reader.onloadend = () => {
                 let base64data = reader.result;
                 file.data = base64data;
                 save[key] = file;
                 i++;

                 if (i === Object.keys(patch.files).length) {
                    localStorage.setItem("files@" + this.url, JSON.stringify(save));
                 }
             };
        }
    }

    // see: https://stackoverflow.com/a/12300351
    dataURIToBlob(dataURI) {
          var byteString = atob(dataURI.split(',')[1]);
          var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0]
          var ab = new ArrayBuffer(byteString.length);
          var ia = new Uint8Array(ab);

          for (var i = 0; i < byteString.length; i++) {
              ia[i] = byteString.charCodeAt(i);
          }

          var blob = new Blob([ab], {type: mimeString});
          return blob;
    }

    patchSize(done) {
        let latch = this.patch.count;
        let patch = this.patch;
        patch.size = 0;

        function countdown(file) {
            patch.size += file.size;
            latch--;
            if (latch === 0) {
                done();
            }
        }

        Object.keys(patch.files).forEach((key, index) => {
           let file = patch.files[key];

           if (file.size === undefined) {
               let xhr = new XMLHttpRequest();
               xhr.open("HEAD", this.url + key, true);
               xhr.onreadystatechange = () => {
                   if (xhr.readyState === 2) {
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
                this.setVersion(this.patch.version);
                this.setFiles(this.patch.files);
                this.worker.completed(file);
            }
        }
    }

    errorHandler(event) {
        if (event.target.status === 409) {
            this.reset();
        } else if (event.target.status === 404) {
            application.error("Failed to retrieve file " + Object.keys(this.patch.files)[this.index]);
        }
    }
}

var patcher = new Patcher();
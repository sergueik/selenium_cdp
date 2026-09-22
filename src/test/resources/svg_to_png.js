var selector = arguments[0];
var filename = arguments[1];
var done = arguments[arguments.length - 1];

var trace = [];

function log(message) {
    trace.push(message);
}

try {
    log("START");
    log("selector=" + selector);
    log("filename=" + filename);

    var element = document.querySelector(selector);

    if (!element) {
        done("FAIL: element not found\n" + trace.join("\n"));
        return;
    }

    log("element found");
    log("tag=" + element.tagName);
    log("id=" + element.id);

    var rect = element.getBoundingClientRect();

    log("width=" + rect.width);
    log("height=" + rect.height);

    var width = rect.width || 100;
    var height = rect.height || 100;

    log("serializing");

    var serializer = new XMLSerializer();
    var svgString = serializer.serializeToString(element);

    log("serialized, length=" + svgString.length);

    var svgBlob = new Blob(
        [svgString],
        { type: "image/svg+xml;charset=utf-8" }
    );

    log("blob created, size=" + svgBlob.size);

    var url = URL.createObjectURL(svgBlob);

    log("object URL created");

    var img = new Image();

    img.onload = function() {
        try {
            log("IMAGE ONLOAD");
            log("naturalWidth=" + img.naturalWidth);
            log("naturalHeight=" + img.naturalHeight);

            var canvas = document.createElement("canvas");

            canvas.width = Math.round(width);
            canvas.height = Math.round(height);

            log("canvas created");

            var ctx = canvas.getContext("2d");

            if (!ctx) {
                URL.revokeObjectURL(url);
                done("FAIL: no canvas context\n" + trace.join("\n"));
                return;
            }

            log("canvas context created");

            ctx.drawImage(img, 0, 0, width, height);

            log("drawImage completed");

            var pngDataUrl = canvas.toDataURL("image/png");

            log("toDataURL completed");
            log("PNG length=" + pngDataUrl.length);

            URL.revokeObjectURL(url);

            var downloadLink = document.createElement("a");

            downloadLink.href = pngDataUrl;
            downloadLink.download =
                filename || "converted-image.png";

            log("download link created");
            log("download filename=" + downloadLink.download);

            document.body.appendChild(downloadLink);

            downloadLink.click();

            document.body.removeChild(downloadLink);

            log("download click completed");

            done("SUCCESS\n" + trace.join("\n"));

        } catch (e) {
            URL.revokeObjectURL(url);

            log("ONLOAD ERROR");
            log(e.name);
            log(e.message);

            done("FAIL\n" + trace.join("\n"));
        }
    };

    img.onerror = function() {
        URL.revokeObjectURL(url);

        log("IMAGE ONERROR");

        done("FAIL\n" + trace.join("\n"));
    };

    log("assigning img.src");

    img.src = url;

    log("img.src assigned");

} catch (e) {
    log("TOP LEVEL ERROR");
    log(e.name);
    log(e.message);

    done("FAIL\n" + trace.join("\n"));
}


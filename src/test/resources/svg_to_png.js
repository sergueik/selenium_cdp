var selector = arguments[0];
var filename = arguments[1];
var noop = arguments[2];
var done = arguments[arguments.length - 1];
var done = arguments[arguments.length - 1];

/*
 * The Java caller supplies selector, filename, and noop.
 * The WebDriver remote end supplies `done` as an additional
 * final argument when invoking this asynchronous script.
 *
 * Calling done(value) completes executeAsyncScript().
 * `value` becomes its return value.
 */
 
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



            if (noop||false){
              var downloadLink = document.createElement("a");
              downloadLink.href = "data:text/plain;charset=utf-8,HELLO_FROM_SELENIUM";
              downloadLink.download = "selenium_test.txt";

              document.body.appendChild(downloadLink);
downloadLink.addEventListener("click", (event) => {
              // var downloadLink = document.createElement("a");
  log("Click received, propagating and navigating normally!");
  
});


              downloadLink.click();

              document.body.removeChild(downloadLink);

              log("download click completed");
            } else {

            var downloadLink = document.createElement("a");

            downloadLink.href = pngDataUrl;
            downloadLink.download = filename;

            log("download link created");
            log("download filename=" + downloadLink.download);

            document.body.appendChild(downloadLink);

            downloadLink.click();

            document.body.removeChild(downloadLink);

            log("download click completed");
            }

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


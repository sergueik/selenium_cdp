function convertSvgToPng(svgElement, callback) {
  // 1. Get the width and height of the SVG
  const rect = svgElement.getBoundingClientRect();
  const width = rect.width || 100;
  const height = rect.height || 100;

  // 2. Serialize the SVG element into a string
  const serializer = new XMLSerializer();
  const svgString = serializer.serializeToString(svgElement);

  // 3. Create a Blob from the SVG string and generate an Object URL
  const svgBlob = new Blob([svgString], { type: 'image/svg+xml;charset=utf-8' });
  const url = URL.createObjectURL(svgBlob);

  // 4. Load the SVG string into an Image element
  const img = new Image();
  img.onload = function () {
    // 5. Initialize a hidden Canvas element
    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;
    const ctx = canvas.getContext('2d');

    // 6. Draw the image onto the canvas
    ctx.drawImage(img, 0, 0, width, height);

    // 7. Extract the PNG Data URL
    const pngDataUrl = canvas.toDataURL('image/png');

    // 8. Clean up memory and trigger callback
    URL.revokeObjectURL(url);
    callback(pngDataUrl);
  };

  img.src = url;
}

// === Usage Example ===
var selector = arguments[0];
var filename = arguments[1];
var done = arguments[arguments.length - 1];

const element = document.querySelector(selector);
convertSvgToPng(element, (pngUrl) => {
  // Option A: Display it in an <img> tag
  // document.getElementById('my-image').src = pngUrl;

  // Option B: Automatically trigger a download
  const downloadLink = document.createElement('a');
  downloadLink.href = pngUrl;
  downloadLink.download = filename || 'converted-image.png';
  downloadLink.click();
  setTimeout(function() { element.style.class = 'done';   done();  },100);
});



// based on: https://github.com/sunnylost/navigation-timing
get_data = function(options = {}) {
  var phases = [{
    name: 'Redirect',
    start: 'redirectStart',
    end: 'redirectEnd',
    index: 0
  }, {
    name: 'App cache',
    start: 'fetchStart',
    end: 'domainLookupStart',
    index: 1
  }, {
    name: 'DNS',
    start: 'domainLookupStart',
    end: 'domainLookupEnd',
    index: 2
  }, {
    name: 'TCP',
    start: 'connectStart',
    end: 'connectEnd',
    index: 3
  }, {
    name: 'Request',
    start: 'requestStart',
    end: 'responseStart',
    index: 4
  }, {
    name: 'Response',
    start: 'responseStart',
    end: 'responseEnd',
    index: 5
  }, {
    name: 'Processing',
    start: 'domLoading',
    end: 'domComplete',
    index: 6,
    value: 0
  }, {
    name: 'onLoad',
    start: 'loadEventStart',
    end: 'loadEventEnd',
    index: 7
  }];

  var totalCost = 0;
  var t = performance.timing;
  var content = [];
  // visualization code removed 

  phases.forEach(function(v) {
    var start = t[v.start],
      end = t[v.end];
    totalCost += (v.value = (start == 0 ? 0 : (end - start)));
  });
  if (options['ladder']) {
    phases.sort(function(a, b) {
      return b.value - a.value;
    });

    phases.forEach(function(v, i) {
      v.width = (100 * v.value / totalCost).toFixed(3);
    });

    phases.sort(function(a, b) {
      return a.index - b.index;
    });

    var content = [];
    var left = 0;
    phases.forEach(function(v) {
      v.left = left;
      left += +v.width;
    });
  }
  phases.forEach(function(v) {

    const parsed = parseInt(v.value, 10);
    if (isNaN(parsed) || parsed < 0) {
      console.log('fixing ' + v.name + ' = ' + v.value);

      v.value = 0;
    }
    // discard negative values
    // observed NumberFormatException on Linux
    // Expected an int but was -1612400986046 at line 1 column 604 path $[6].value
    // Java only supports signed longs.

  });
  return JSON.stringify(phases);
}
return get_data(arguments[0], arguments[1]);
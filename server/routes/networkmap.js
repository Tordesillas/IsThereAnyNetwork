var express = require('express');
var mongoose = require('mongoose');
var router = express.Router();

var researchparams = require('../middlewares/researchparams');

const signalMin = -150;
const signalMax = -40;

const l = 6; // max level, 0 is reserved to missing or invalid info
const resolution = 32; // number of columns and rows

Number.prototype.map = function (in_min, in_max, out_min, out_max) {
  return (this - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

function get_result(mongoose_object) {
  return new Promise((resolve, reject) =>
      mongoose_object.exec((err, networkstates) => 
          err ? reject(err) : resolve(networkstates)
      )
  );
}

function compute_matrix(res, networkstates, hour) {
  const minX = res.locals.researchparams.longitude['$gte'];
  const minY = res.locals.researchparams.latitude['$gte'];
  const maxX = res.locals.researchparams.longitude['$lte'];
  const maxY = res.locals.researchparams.latitude['$lte'];
  
  let signalsArray = new Array(resolution * resolution);

  for (i = 0; i < resolution; i++) {
    signalsArray[i] = new Array(resolution);
    for (j = 0; j < resolution; j++) {
      signalsArray[i][j] = new Array(0);
    }
  }

  networkstates.forEach(networkstate => {
    // There's a weird time zone offset applied to the network object's date.
    // The calculation brings the hours of the date back to the GMT value.
    networkstateHour = networkstate.date.getHours() + (networkstate.date.getTimezoneOffset() / 60);
    if (hour == -1 || networkstateHour == hour) {
      const x = Math.floor(networkstate.longitude.map(minX, maxX, 0, resolution));
      const y = resolution - Math.floor(networkstate.latitude.map(minY, maxY, 0, resolution)); // reverse y axis since latitude increases going north

      if (networkstate.signalStrength < signalMax && networkstate.signalStrength > signalMin) { // filter out invalid values
        signalsArray[y][x].push(Math.floor(Math.abs(networkstate.signalStrength))); // absolute value for subsequent operations
      }
    }
  });

  signalArray = signalsArray.map(row => {
    return row.map(cell => cell.length < 1 ? 0 : cell.reduce((a,b) => a + b, 0) / cell.length);
  });

  kernel = [
    [ 1, 2, 1 ],
    [ 2, 4, 2 ],
    [ 1, 2, 1 ]
  ];

  let filteredArray = new Array(resolution);
  for (i = 0; i < resolution; i++) {
    filteredArray[i] = new Array(resolution);
    for (j = 0; j < resolution; j++) {
      filteredArray[i][j] = 0;
    }
  }

  for (i = 0; i < resolution; i++) {
    for (j = 0; j < resolution; j++) {
      let computedValues = [];
      let computedFactors = [];

      for (m = -1; m <= 1; m++) {
        for (n = -1; n <= 1; n++) {
          if (i + m < 0 || i + m >= resolution || j + n < 0 || j + n >= resolution) {
            continue; // ignore out of bounds
          }
          const value = signalArray[i + m][j + n];
          if (value == 0) { // no or invalid value, ignore
            continue;
          }
          computedValues.push(kernel[m + 1][n + 1] * value);
          computedFactors.push(kernel[m + 1][n + 1]);
        }
      }

      if (computedValues.length < 1) { // no value could be computed
        filteredArray[i][j] = 0;
      } else {
        const factor = computedFactors.reduce((a,b) => a + b, 0);
        const finalValue = computedValues.reduce((a,b) => a + b, 0) / factor;
        //console.log(finalValue, computedValues.length);
        filteredArray[i][j] = finalValue;
      }
    }
  }

  return filteredArray.flatMap(t => t);
}

router.route('/')
  /* GET network results listing. */
  .get(researchparams, function(req, res, next) {
    var flatArray = [];
    var levelArray = [];

    var model = mongoose.model('networkstate');
    
    if (req.query.targetHour != undefined) {
      var aggregate = model.aggregate([{ $match: res.locals.researchparams }, res.locals.targetHour.redact]);
      const promise = get_result(aggregate);
      promise.then(result => {
        var matrix = [];
        for (k = 0; k < 5; k++) {  
          matrix.push(compute_matrix(res, result, res.locals.targetHour.hours[k]));
        }

        // Do the work
        timeKernel = [1, 2, 4, 2, 1];
        let filteredArray = new Array(resolution * resolution);
        for (i = 0; i < resolution * resolution; ++i) {
          filteredArray[i] = 0;
        }

        for (i = 0; i < resolution * resolution; i++) {
          let computedValues = [];
          let computedFactors = [];

          for (m = -1; m <= 3; m++) {
            const value = matrix[m + 1][i];
            if (value == 0) { // no or invalid value, ignore
              continue;
            }
            computedValues.push(timeKernel[m + 1] * value);
            computedFactors.push(timeKernel[m + 1]);
          }

          if (computedValues.length < 1) { // no value could be computed
            filteredArray[i][j] = 0;
          } else {
            const factor = computedFactors.reduce((a,b) => a + b, 0);
            const finalValue = computedValues.reduce((a,b) => a + b, 0) / factor;
            //console.log(finalValue, computedValues.length);
            filteredArray[i] = finalValue;
          }
        }

        flatArray = filteredArray.flatMap(t => t);

        // Filters
        levelArray = flatArray.map(s => s == 0 ? 0 : Math.floor(s.map(-signalMin, -signalMax, 1, l))); // we've taken absolute value before so use positive max and min
        res.send(levelArray);
      },
      err => {
        res.status(500);
        res.send({error: err});
        console.error(err);
      });
    } else {
      model = model.find(res.locals.researchparams);
      const promise = get_result(model);
      promise.then(result => {
        flatArray = compute_matrix(res, result, -1);
        // Filters
        levelArray = flatArray.map(s => s == 0 ? 0 : Math.floor(s.map(-signalMin, -signalMax, 1, l))); // we've taken absolute value before so use positive max and min
        res.send(levelArray);
      },
      err => {
        res.status(500);
        res.send({error: err});
        console.error(err);
      });
    }
  });

module.exports = router;

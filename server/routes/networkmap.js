var express = require('express');
var mongoose = require('mongoose');
var router = express.Router();

var researchparams = require('../middlewares/researchparams');


const signalMin = -150;
const signalMax = -40;

Number.prototype.map = function (in_min, in_max, out_min, out_max) {
  return (this - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

router.route('/')
  /* GET network results listing. */
  .get(researchparams, function(req, res, next) {
    const l = 6; // max level, 0 is reserved to missing or invalid info
    const resolution = 32; // number of columns and rows
    const minX = res.locals.researchparams.longitude['$gte'];
    const minY = res.locals.researchparams.latitude['$gte'];
    const maxX = res.locals.researchparams.longitude['$lte'];
    const maxY = res.locals.researchparams.latitude['$lte'];

    //console.log(minX, minY, maxX, maxY);
    //console.log(res.locals.researchparams);
    var model = mongoose.model('networkstate');
    if (req.query.targetHour) {
      model = model.aggregate([{ $match: res.locals.researchparams }, res.locals.targetHour.redact]);
    } else {
      model = model.fing(res.locals.researchparams);  
    }

    model.exec(function (err, networkstates) {
      if (err) {
        res.status(500);
        res.send({error: err});
        console.error(err);
      } else {
        let signalsArray = new Array(resolution * resolution);
        for (i = 0; i < resolution; i++) {
          signalsArray[i] = new Array(resolution);
          for (j = 0; j < resolution; j++) {
            signalsArray[i][j] = new Array(0);
          }
        }

        networkstates.forEach(networkstate => {
          const x = Math.floor(networkstate.longitude.map(minX, maxX, 0, resolution));
          const y = resolution - Math.floor(networkstate.latitude.map(minY, maxY, 0, resolution)); // reverse y axis since latitude increases going north

          if (networkstate.signalStrength < signalMax && networkstate.signalStrength > signalMin) { // filter out invalid values
            signalsArray[y][x].push(Math.floor(Math.abs(networkstate.signalStrength))); // absolute value for subsequent operations
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

        let filteredArray = new Array(resolution * resolution);
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

        //console.log("filteredArray");
        //console.log(filteredArray);
        const flatArray = filteredArray.flatMap(t => t);
        //console.log("flatArray");
        //console.log(flatArray);

        // Filters
        levelArray = flatArray.map(s => s == 0 ? 0 : Math.floor(s.map(-signalMin, -signalMax, 1, l))); // we've taken absolute value before so use positive max and min
        //console.log("levelArray");
        //console.log(levelArray);
        res.send(levelArray);
      }
    });

  });

module.exports = router;

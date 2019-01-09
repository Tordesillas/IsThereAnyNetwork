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
    const resolution = 10; // number of columns and rows
    const minX = res.locals.researchparams.longitude['$gte'];
    const minY = res.locals.researchparams.latitude['$gte'];
    const maxX = res.locals.researchparams.longitude['$lte'];
    const maxY = res.locals.researchparams.latitude['$lte'];

    console.log(minX, minY, maxX, maxY);
    console.log(res.locals.researchparams);

    mongoose.model('networkstate').find(res.locals.researchparams, function (err, networkstates) {
      if (err) {
        res.status(500);
        res.send({error: err});
        console.error(err);
      } else {
        let signalsArray = new Array(resolution * resolution);
        for (i = 0; i < resolution*resolution; i++) {
          signalsArray[i] = new Array(0);
        }

        networkstates.forEach(networkstate => {
          const x = Math.floor(networkstate.longitude.map(minX, maxX, 0, resolution));
          const y = Math.floor(networkstate.latitude.map(minY, maxY, 0, resolution));

          if (networkstate.signalStrength < signalMax && networkstate.signalStrength > signalMin) { // filter out invalid values
            signalsArray[y*resolution + x].push(networkstate.signalStrength);
          }

        });

        signalArray = signalsArray.map(t => t.length < 1 ? 0 : t.reduce((a,b) => a + b, 0) / t.length);
        levelArray = signalArray.map(s => s == 0 ? 0 : Math.floor(s.map(signalMax, signalMin, 1, l)));

        res.send(levelArray);
      }
    });

  });

module.exports = router;

var express = require('express');
var mongoose = require('mongoose');
var router = express.Router();

router.route('/')
  /* GET network results listing. */
  .get(function(req, res, next) {
    var researchparams = {};
    if (req.query.from)
      researchparams.date = Object.assign(researchparams.date || {}, { '$gte': req.query.from });
    if (req.query.to)
      researchparams.date = Object.assign(researchparams.date || {}, { '$lte': req.query.to });

    if (req.query.operatorName)
      researchparams.operatorName = req.query.operatorName;

    if (req.query.networkProtocol)
      researchparams.networkProtocol = req.query.networkProtocol;

    if (req.query.signalStrengthLowerThan)
      researchparams.signalStrength = Object.assign(researchparams.signalStrength || {}, { '$gte': req.query.signalStrengthLowerThan });
    if (req.query.signalStrengthGreaterThan)
      researchparams.signalStrength = Object.assign(researchparams.signalStrength || {}, { '$lte': req.query.signalStrengthGreaterThan });

    mongoose.model('networkstate').find(researchparams, function (err, networkstates) {
      if (err) {
        res.status(500);
        res.send({error: err});
        console.error(err);
      } else {
        res.send(networkstates);
      }
    });
  })
  /* POST new network result */
  .post(function(req, res, next) {
    console.log(req.body);
    mongoose.model('networkstate').create(req.body, function (err, networkstate) {
      if (err) {
        res.status(500);
        res.send({error: err});
        console.error(err);
      } else {
        console.log("Created: " + networkstate);
        res.send(networkstate);
      }
    });
  });

router.route('/:id')
  /* GET single networkstate by id */
  .get(function(req, res, next) {
    mongoose.model('networkstate').findOne({_id: mongoose.Types.ObjectId(req.params.id)}, function(err, networkstate) {
      if (err) {
        res.status(500);
        res.send({error: err});
        console.error(err);
      } else if (!networkstate) {
        res.status(404);
        res.send("Not found: " + req.params.id);
      } else {
        res.send(networkstate);
      }
    });
  });

module.exports = router;

var express = require('express');
var mongoose = require('mongoose');
var router = express.Router();

var researchparams = require('../middlewares/researchparams');

router.route('/')
  /* GET network results listing. */
  .get(researchparams, function(req, res, next) {
    mongoose.model('networkstate').find(res.locals.researchparams, function (err, networkstates) {
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

router.route('/average')
  .get(researchparams, function(req, res, next) {
    res.status(404);
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

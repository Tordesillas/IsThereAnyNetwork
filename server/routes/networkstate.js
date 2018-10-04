var express = require('express');
var mongoose = require('mongoose');
var router = express.Router();

router.route('/')
  /* GET network results listing. */
  .get(function(req, res, next) {
    mongoose.model('networkstate').find({}, function (err, networkstates) {
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
    console.log("miaou");
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


module.exports = router;

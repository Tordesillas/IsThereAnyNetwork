var express = require('express');
var mongoose = require('mongoose');
var router = express.Router();

router.route('/')
  /* GET network results listing. */
  .get(function(req, res, next) {
    const l = 5; // number of levels
    const resolution = 10; // number of columns and rows

    let levelArray = [];
    for (var i = 0; i < resolution; i++) { // rows
      for (var j = 0; j < resolution; j++) { // columns
        const v = Math.floor(Math.random() * l);
        levelArray[i*resolution + j] = v;
      }
    }
    res.send(levelArray);
  });

module.exports = router;

function researchparams(req, res, next) {

  var researchparams = {};
  var targetHour = {};
  if (req.query.from)
    researchparams.date = Object.assign(researchparams.date || {}, { '$gte': req.query.from });
  if (req.query.to)
    researchparams.date = Object.assign(researchparams.date || {}, { '$lte': req.query.to });
  if (req.query.targetHour) {
    req.query.targetHour = parseInt(req.query.targetHour);
    targetHour.hours = [];
    
    // Standard range assignment
    targetHour.lowerHour = req.query.targetHour - 2;
    targetHour.upperHour = req.query.targetHour + 2;

    for (i = targetHour.lowerHour; i <= targetHour.upperHour; ++i) {
      targetHour.hours.push(i);
    }
    
    // Special cases treatment
    switch (req.query.targetHour) {
      case 0:
        targetHour.lowerHour = 22;
        targetHour.hours[0] = targetHour.lowerHour;
        targetHour.hours[1] = targetHour.lowerHour + 1;
        break;
      case 1:
        targetHour.lowerHour = 23;
        targetHour.hours[0] = targetHour.lowerHour;
        targetHour.hours[1] = 0;
        break;
      case 22:
        targetHour.upperHour = 0;
        targetHour.hours[3] = 23;
        targetHour.hours[4] = targetHour.upperHour;
        break;
      case 23:
        targetHour.upperHour = 1;
        targetHour.hours[3] = targetHour.upperHour - 1;
        targetHour.hours[4] = targetHour.upperHour;
        break;
    }

    targetHour.redact = {
        "$redact": {
            "$cond": [
                {
                  "$and": [
                    { "$gte": [ { "$hour": "$date" }, targetHour.lowerHour ] },
                    { "$lte": [ { "$hour": "$date" }, targetHour.upperHour ] }
                  ]
                },
                "$$KEEP",
                "$$PRUNE"
            ]
        }    
    }
  } 
  if (req.query.operatorName)
    researchparams.operatorName = req.query.operatorName;

  if (req.query.networkProtocol)
    researchparams.networkProtocol = req.query.networkProtocol;

  if (req.query.signalStrengthGreaterThan)
    researchparams.signalStrength = Object.assign(researchparams.signalStrength || {}, { '$gte': parseFloat(req.query.signalStrengthGreaterThan) });
  if (req.query.signalStrengthLowerThan)
    researchparams.signalStrength = Object.assign(researchparams.signalStrength || {}, { '$lte': parseFloat(req.query.signalStrengthLowerThan) });

  if (req.query.latitudeGreaterThan)
    researchparams.latitude = Object.assign(researchparams.latitude || {}, { '$gte': parseFloat(req.query.latitudeGreaterThan) });
  if (req.query.latitudeLowerThan)
    researchparams.latitude = Object.assign(researchparams.latitude || {}, { '$lte': parseFloat(req.query.latitudeLowerThan) });

  if (req.query.longitudeGreaterThan)
    researchparams.longitude = Object.assign(researchparams.longitude || {}, { '$gte': parseFloat(req.query.longitudeGreaterThan) });
  if (req.query.longitudeLowerThan)
    researchparams.longitude = Object.assign(researchparams.longitude || {}, { '$lte': parseFloat(req.query.longitudeLowerThan) });

  res.locals.researchparams = researchparams;
  res.locals.targetHour = targetHour;

  next();
}

module.exports = researchparams;

function researchparams(req, res, next) {

  var researchparams = {};
  var targetHour = {};
  if (req.query.from)
    researchparams.date = Object.assign(researchparams.date || {}, { '$gte': req.query.from });
  if (req.query.to)
    researchparams.date = Object.assign(researchparams.date || {}, { '$lte': req.query.to });
  if (req.query.targetHour) {
    req.query.targetHour = parseInt(req.query.targetHour);
    console.log("targertHour" + req.query.targetHour);
    targetHour.redact = {
        "$redact": {
            "$cond": [
                { "$eq": [ { "$hour": "$date" }, req.query.targetHour ] },
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

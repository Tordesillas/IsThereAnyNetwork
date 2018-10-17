var mongoose = require('mongoose');

var networktypes = require('./networktypes.json');
var networkstateSchema = new mongoose.Schema({
  latitude: Number,
  longitude: Number,
  signalStrength: { type: Number, required: true },
  operatorName: String,
  networkProtocol: { type: String, enum: networktypes },
  date: { type: Date, default: Date.now }
});
mongoose.model('networkstate', networkstateSchema);

var mongoose = require('mongoose');
var networkstateSchema = new mongoose.Schema({
  latitude: Number,
  longitude: Number,
  signalStrength: {type: Number, required: true},
  operatorName: String,
  networkProtocol: { type: String, enum: ["2G", "GPRS", "Edge", "3G", "HSPA", "HSPA+", "LTE", "LTE Advanced", "5G"] },
  date: { type: Date, default: Date.now }
});
mongoose.model('networkstate', networkstateSchema);

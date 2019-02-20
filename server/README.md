# IsThereAnyNetwork Server

## Dependencies
```
 - docker
 - docker-compose >= 1.10.0
```

## Build
```
docker-compose build
```

## Run
```
docker-compose rm -f -s
docker-compose up -d
```

## Usage
### Base URL
```
http://localhost:3000/
```

### API endpoints
```
networkstate
networkmap
```

### API parameters
```
operatorName: String
networkProtocol: Enum NetworkType
signalStrengthGreaterThan: Signed Integer
signalStrengthLowerThan: Signed Integer
latitudeGreaterThan: Float
latitudeLowerThan: Float
longitudeGreaterThan: Float
longitudeLowerThan: Float
from: Date ISO-8601
to: Date ISO-8601
```

### Enum NetworkType
```
  GPRS
  EDGE
  UMTS
  CDMA
  EVDO rev. 0
  EVDO rev. A
  1xRTT
  HSDPA
  HSUPA
  HSPA
  iDen
  EVDO rev. B
  LTE
  eHRPD
  HSPA+
  GSM
  TD_SCDMA
  IWLAN
  Unknown
```

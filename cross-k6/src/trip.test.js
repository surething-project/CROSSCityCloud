import { group, check, sleep } from 'k6';
import http from 'k6/http';
import { uuidv4, randomIntBetween, randomItem } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';
import { Timestamp } from 'google-protobuf/google/protobuf/timestamp_pb';
import { Credentials,  CryptoIdentity, Authorization } from './proto/User_pb';
import { VisitEvidence, WiFiAPEvidence } from './proto/Evidence_pb';
import { Trip, Visit, GetTripsResponse, CreateOrUpdateTripResponse } from './proto/Trip_pb';

export const options = {
    stages: [
        { duration: '5m', target: 10 },
        { duration: '10m', target: 50 },
    ]
};

const BASE_URL = "https://cross-surething.eu/v2/";
const SLEEP_DURATION_SECONDS = 0.1;
const VISIT_DURATION_MILLIS = 25000;
const EVIDENCE_OFFSET_MILLIS = 1000;
const TEST_USERS = ["alice", "bob", "charlie"];
const TEST_PASSWORD = "123456";
const TEST_CONFIDENCE_THRESHOLD = 75;
const TEST_ROUTE_ID = "route_2";
const TEST_POI_ID = "poi_2_1";
const TEST_BSSIDS = new Set(['84:94:8c:ef:6e:78', '3c:a8:2a:c7:b5:ea', 'ca:02:10:5c:72:1a', 'a4:b1:e9:f3:ef:29', '86:b8:b8:34:af:00', 'a6:b1:e9:f3:ef:2a', '20:3d:b2:9a:d5:d0', 'f4:30:b9:4b:b3:78', '08:b0:55:11:22:b1', '04:9f:ca:ae:25:9c', '84:94:8c:ee:de:18', '54:13:10:a8:79:10', 'e8:82:5b:b0:56:19', '00:05:ca:b5:d2:28', 'cc:19:a8:01:b5:60', 'cc:19:a8:01:b5:62', 'f0:f2:49:d8:fb:c8', 'a8:4e:3f:33:a7:48']);

function getBinaryRequest(byteArray) {
    const offset = byteArray.byteOffset;
    const length = byteArray.byteLength;

    return byteArray.buffer.slice(offset, offset + length);
}

function buildTrip() {
    let testBssids = new Set([...TEST_BSSIDS]);
    const numOfEvidences = randomIntBetween(Math.ceil((TEST_CONFIDENCE_THRESHOLD * TEST_BSSIDS.size) / 100), TEST_BSSIDS.size);

    const trip = new Trip();
    trip.setId(uuidv4());
    trip.setRouteid(TEST_ROUTE_ID);

    const visit = new Visit();
    visit.setId(uuidv4());
    visit.setPoiid(TEST_POI_ID);
    const current_date = Date.now();
    const entryTs = new Timestamp();
    entryTs.fromDate(new Date(current_date - VISIT_DURATION_MILLIS));
    visit.setEntrytime(entryTs);
    const leaveTs = new Timestamp();
    leaveTs.fromDate(new Date(current_date));
    visit.setLeavetime(leaveTs);

    for (let i = 0; i < numOfEvidences; i++) {
        const evidence = new VisitEvidence();
        const wifiEvidence = new WiFiAPEvidence();
        const bssid = randomItem([...testBssids]);
        wifiEvidence.setBssid(bssid);
        wifiEvidence.setSsid("");
        wifiEvidence.setSightingmillis(current_date - VISIT_DURATION_MILLIS + EVIDENCE_OFFSET_MILLIS);
        evidence.setWifiapevidence(wifiEvidence);
        visit.addVisitevidences(evidence);
        testBssids.delete(bssid);
    }

    trip.addVisits(visit);

    return trip;
}

export default function () {
    const params = {
        headers: {
            'Content-Type': 'application/x-protobuf',
        },
        tags: {
            name: 'sign-in',
        },
        responseType: 'binary'
    };

    const identity = new CryptoIdentity();
    const credentials = new Credentials();
    credentials.setUsername(randomItem(TEST_USERS));
    credentials.setPassword(TEST_PASSWORD);
    identity.setSessionid(uuidv4());
    identity.setPublickey(new Uint8Array());
    credentials.setCryptoidentity(identity);

    const signin_request = getBinaryRequest(credentials.serializeBinary());

    group("trip user flow", (_) => {
        // User Signin
        const signin_response = http.post(`${BASE_URL}user/signin`, signin_request, params);
        check(signin_response, {
            'is signin status 200': (r) => r.status === 200,
          });
        const auth = Authorization.deserializeBinary(new Uint8Array(signin_response.body));
        params.headers['Authorization'] = auth.getJwt();
        sleep(SLEEP_DURATION_SECONDS);

        // Get Routes
        params.tags.name = 'get-routes';
        const get_routes_response = http.get(`${BASE_URL}route`, params);
        check(get_routes_response, {
            'is get routes status 200': (r) => r.status === 200,
        });
        sleep(SLEEP_DURATION_SECONDS);

        // Get Test Route
        params.tags.name = 'get-test-route';
        const get_route_response = http.get(`${BASE_URL}route/${TEST_ROUTE_ID}`, params);
        check(get_route_response, {
            'is get test route status 200': (r) => r.status === 200,
        });
        sleep(SLEEP_DURATION_SECONDS);

        // Create or Update Trip
        const trip = buildTrip();
        const create_or_update_trip_request = getBinaryRequest(trip.serializeBinary());

        params.tags.name = 'create-or-update-trip';
        const create_or_update_trip_response = http.post(`${BASE_URL}trip`, create_or_update_trip_request, params);
        check(create_or_update_trip_response, {
            'is create or update status 200': (r) => r.status === 200,
            'is trip not completed': (r) => CreateOrUpdateTripResponse.deserializeBinary(new Uint8Array(r.body)).getCompletedtrip() === false
        }); 
    });
}

import http from 'k6/http';
import { sleep, check } from 'k6';
export const options = {
    stages: [
        { duration: '30s', target: 50 }, // ramp to 50 virtual drivers
        { duration: '1m', target: 10000 }, // ramp to 200
        { duration: '2m', target: 10000 }, // hold at 200, sustained
        { duration: '30s', target: 0 }, // ramp down
],
};
export default function () {
    const driverId = Math.floor(Math.random() * 500) + 1;
    const payload = JSON.stringify({
        driverId: driverId,
        latitude: 33.97 + Math.random() * 0.05,
        longitude: -6.85 + Math.random() * 0.05,
    });
    const res = http.post(
        'http://localhost:8080/locations',
        payload,
        { headers: { 'Content-Type': 'application/json' } },
    );
    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(1); // one ping per second per virtual driver
}

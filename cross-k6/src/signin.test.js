import http from 'k6/http';
import { Credentials,  CryptoIdentity, Authorization } from './proto/User_pb';

export default function () {
    const identity = new CryptoIdentity()
    const credentials = new Credentials()
    credentials.setUsername("alice")
    credentials.setPassword("123456")
    identity.setSessionid("")
    identity.setPublickey(new Uint8Array())

    const arr = credentials.serializeBinary()
    const offset = arr.byteOffset;
    const length = arr.byteLength;

    const req = arr.buffer.slice(offset, offset + length)

    const res = http.post('http://localhost:8080/v2/user/signin', req, { headers: { 'Content-Type': 'application/x-protobuf' }, responseType: 'binary' });
    const resBody = Authorization.deserializeBinary(new Uint8Array(res.body))
    console.log(resBody.getJwt())
}

CREATE TABLE IF NOT EXISTS "dataset"
(
    id      VARCHAR(36) PRIMARY KEY,
    version TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS "cross_user"
(
    username      VARCHAR(36) PRIMARY KEY,
    password_hash BYTEA                    NOT NULL,
    password_salt BYTEA                    NOT NULL,
    joined        TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS "user_crypto_identity"
(
    username   VARCHAR(36) NOT NULL REFERENCES cross_user (username),
    session_id VARCHAR(36) NOT NULL,
    pubkey     BYTEA       NOT NULL,
    PRIMARY KEY (username, session_id)
);

CREATE TABLE IF NOT EXISTS "poi"
(
    id          VARCHAR(36) PRIMARY KEY,
    world_coord POINT      NOT NULL,
    web_url     TEXT,
    image_url   TEXT       NOT NULL,
    main_locale VARCHAR(5) NOT NULL
);

CREATE TABLE IF NOT EXISTS "poi_lang"
(
    poi_id      VARCHAR(36)  NOT NULL REFERENCES poi (id),
    lang        VARCHAR(5)   NOT NULL,
    name        VARCHAR(70)  NOT NULL,
    description VARCHAR(400) NOT NULL,
    PRIMARY KEY (poi_id, lang)
);

CREATE TABLE IF NOT EXISTS "waypoint"
(
    id                   VARCHAR(36) PRIMARY KEY,
    poi_id               VARCHAR(36) NOT NULL REFERENCES poi (id),
    stay_for             INTERVAL    NOT NULL,
    confidence_threshold INT         NOT NULL
);

CREATE TABLE IF NOT EXISTS "route"
(
    id          VARCHAR(36) PRIMARY KEY,
    image_url   TEXT       NOT NULL,
    main_locale VARCHAR(5) NOT NULL
);

CREATE TABLE IF NOT EXISTS "route_lang"
(
    route_id    VARCHAR(36)  NOT NULL REFERENCES route (id),
    lang        VARCHAR(5)   NOT NULL,
    name        VARCHAR(70)  NOT NULL,
    description VARCHAR(400) NOT NULL,
    PRIMARY KEY (route_id, lang)
);

CREATE TABLE IF NOT EXISTS "route_waypoint"
(
    route_id    VARCHAR(36) NOT NULL REFERENCES route (id),
    waypoint_id VARCHAR(36) NOT NULL REFERENCES waypoint (id),
    PRIMARY KEY (route_id, waypoint_id)
);

CREATE TABLE IF NOT EXISTS "trip"
(
    id       VARCHAR(36) PRIMARY KEY,
    route_id VARCHAR(36) NOT NULL REFERENCES route (id),
    traveler VARCHAR(36) NOT NULL REFERENCES cross_user (username)
);

CREATE TABLE IF NOT EXISTS "completed_trip"
(
    trip_id     VARCHAR(36)              NOT NULL REFERENCES trip (id) ON DELETE CASCADE,
    submit_time TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (trip_id)
);

CREATE TABLE IF NOT EXISTS "visit"
(
    id         VARCHAR(36) PRIMARY KEY,
    trip_id    VARCHAR(36)              NOT NULL REFERENCES trip (id) ON DELETE CASCADE,
    poi_id     VARCHAR(36)              NOT NULL REFERENCES poi (id),
    entry_time TIMESTAMP WITH TIME ZONE NOT NULL,
    leave_time TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS "wifiap"
(
    bssid       VARCHAR(17) PRIMARY KEY,
    type        VARCHAR(20) NOT NULL,
    trigger     BOOLEAN     NOT NULL,
    totp_secret TEXT,
    totp_regexp TEXT
);

CREATE TABLE IF NOT EXISTS "poi_wifiap"
(
    poi_id       VARCHAR(36) NOT NULL REFERENCES poi (id),
    wifiap_bssid VARCHAR(17) NOT NULL REFERENCES wifiap (bssid),
    PRIMARY KEY (poi_id, wifiap_bssid)
);

CREATE TABLE IF NOT EXISTS "wifiap_evidence"
(
    visit_id        VARCHAR(36) NOT NULL REFERENCES visit (id) ON DELETE CASCADE,
    bssid           VARCHAR(17) NOT NULL REFERENCES wifiap (bssid),
    ssid            VARCHAR(32) NOT NULL,
    sighting_millis BIGINT      NOT NULL,
    PRIMARY KEY (visit_id, bssid, ssid)
);

CREATE TABLE IF NOT EXISTS "unknown_wifiap_evidence"
(
    visit_id        VARCHAR(36) NOT NULL REFERENCES visit (id) ON DELETE CASCADE,
    bssid           VARCHAR(17) NOT NULL,
    ssid            VARCHAR(32) NOT NULL,
    sighting_millis BIGINT      NOT NULL,
    PRIMARY KEY (visit_id, bssid, ssid)
);

CREATE TABLE IF NOT EXISTS "peer_testimony"
(
    visit_id         VARCHAR(36) NOT NULL REFERENCES visit (id) ON DELETE CASCADE,
    witness          VARCHAR(36) NOT NULL REFERENCES cross_user (username),
    peer_endorsement BYTEA       NOT NULL,
    PRIMARY KEY (visit_id, witness)
);

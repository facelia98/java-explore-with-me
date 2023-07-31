DROP TABLE IF EXISTS categories, users, locations, events, requests, compilations, compilation_event CASCADE;

CREATE TABLE IF NOT EXISTS categories
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL,
    CONSTRAINT UQ_CATEGORY_NAME UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email VARCHAR NOT NULL,
    name  VARCHAR,
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS locations
(
    id  BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL
);

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    annotation         VARCHAR(2000),
    category_id        BIGINT,
    confirmed_requests BIGINT DEFAULT 0,
    created_on         TIMESTAMP NOT NULL,
    description        VARCHAR(7000)   NOT NULL,
    event_date         TIMESTAMP,
    initiator_id       BIGINT,
    location_id        BIGINT,
    paid               BOOLEAN   NOT NULL,
    participant_limit  INT DEFAULT 0,
    published_on       TIMESTAMP,
    request_moderation BOOLEAN   NOT NULL,
    state              VARCHAR(10),
    title              VARCHAR(1000)   NOT NULL,
    views              INT DEFAULT 0,
    CONSTRAINT fk_categories_to_event FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_location_to_event FOREIGN KEY (location_id) REFERENCES locations (id),
    CONSTRAINT fk_initiator_to_event FOREIGN KEY (initiator_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    created      TIMESTAMP,
    event_id     BIGINT,
    requester_id BIGINT,
    status       VARCHAR(10),
    CONSTRAINT fk_requestor_to_request FOREIGN KEY (requester_id) REFERENCES users (id),
    CONSTRAINT fk_event_to_request FOREIGN KEY (event_id) REFERENCES events (id),
    UNIQUE (event_id, requester_id)
);

CREATE TABLE IF NOT EXISTS compilations
(
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    pinned   BOOLEAN,
    title    VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS compilation_event
(
    compilation_id BIGINT NOT NULL,
    event_id       BIGINT NOT NULL,
    PRIMARY KEY (compilation_id, event_id),
    CONSTRAINT fk_compilations_to_compilation FOREIGN KEY (compilation_id) REFERENCES compilations (id),
    CONSTRAINT fk_event_to_compilation_event FOREIGN KEY (event_id) REFERENCES events (id)
);
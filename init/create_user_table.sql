CREATE SEQUENCE IF NOT EXISTS user_id_seq;

CREATE TABLE IF NOT EXISTS public."user"
(
    id bigint NOT NULL DEFAULT nextval('user_id_seq'::regclass),
    username character varying NOT NULL,
    password character varying NOT NULL,
    role character varying,
    CONSTRAINT user_pkey PRIMARY KEY (id),
    CONSTRAINT user_username_key UNIQUE (username)
    );

ALTER TABLE IF EXISTS public."user" OWNER to postgres;
ALTER SEQUENCE user_id_seq OWNER TO postgres;
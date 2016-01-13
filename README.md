__Please start at the [Top-Level Lens Repo][5].__

# Lens Auth Service

[![Build Status](https://travis-ci.org/alexanderkiel/lens-auth.svg?branch=master)](https://travis-ci.org/alexanderkiel/lens-auth)
[![Docker Pulls](https://img.shields.io/docker/pulls/akiel/lens-auth.svg)](https://hub.docker.com/r/akiel/lens-auth/)

The central OAuth 2.0 authorization service for all Lens backend services.

Lens auth currently supports only the Resource Owner Password Credentials Grant
described in section [4.3][1] of the spec. An OAuth 2.0 compatible
[token endpoint][2] is implemented under the `/token` path. An OAuth 2.0
compatible [introspection endpoint][3] is implemented under the `/introspect`
path.

## Client Identifiers

Client identifiers issued by this authorization service are opaque strings with a length not longer than 255 chars.

## Usage with Leiningen

To start the service with leiningen, run the following command

    lein with-profile production trampoline run

This starts the Lens Auth service on localhost port 8080.

## Usage on Heroku Compatible PaaS

This application uses the following environment vars:

* `PORT` - the port to listen on
* `CONTEXT_PATH` - an optional context path under which the workbook service runs
* `TOKEN_STORE` - how to store the generated tokens. Currently `atom` and `riak` are supported (defaults to `atom`)
* `RIAK_TOKEN_HOST` - **Must** be specified if token-store is `riak`
* `RIAK_TOKEN_PORT` - the Riak HTTP port (defaults to `8098`)
* `RIAK_TOKEN_BUCKET` - the name of the Riak bucket to store generated tokens in (defaults to `auth-tokens`)
* `EXPIRE` - the time in seconds after which a token expires
* `AUTH` - how to check user credentials, currently `noop` (all credentials are valid) and `ldap` are supported (defaults to `noop`)
* `LDAP_HOSTS` - the host or hosts (comma separated) to use for ldap connect, **must** be specified if auth is `ldap`
* `LDAP_USER_BASE_DN` - the ldap base [dn](https://www.ldap.com/ldap-dns-and-rdns) to locate users,**must** be specified if auth is `ldap`
* `CLIENT_STORE` - how to store the generated tokens. Currently `atom` and `riak` are supported (defaults to `atom`)
* `RIAK_CLIENT_HOST` - **Must** be specified if client-store is `riak`
* `RIAK_CLIENT_PORT` - the Riak HTTP port (defaults to `8098`)
* `RIAK_CLIENT_BUCKET` - the name of the Riak bucket to store generated clients in (defaults to `auth-clients`)

If you have [foreman][4] installed you can create an `.env` file listing the
environment vars specified above and just type `foreman start`.

## Usage through Docker Container

You have to start the auth container:

    docker run -d -p 8080:80 --name lens-auth akiel/lens-auth

After starting the container, a `curl http://localhost:8080/token` should show
`Method not allowed.` which is okay for the moment.

## Develop

Running a REPL will load the user namespace. Use `(startup)` to start the server
and `(reset)` to reload after code changes.

## License

Copyright © 2015 Alexander Kiel

Distributed under the Eclipse Public License, the same as Clojure.

[1]: <http://tools.ietf.org/html/rfc6749#section-4.3>
[2]: <http://tools.ietf.org/html/rfc6749#section-3.2>
[3]: <https://tools.ietf.org/html/draft-ietf-oauth-introspection-08#section-2>
[4]: <https://github.com/ddollar/foreman>
[5]: <https://github.com/alexanderkiel/lens>

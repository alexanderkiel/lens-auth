__This software is ALPHA, lacks documentation and has to be deployed in conjunction with other Lens modules.__

# Lens Auth Service

[![Build Status](https://travis-ci.org/alexanderkiel/lens-auth.svg?branch=master)](https://travis-ci.org/alexanderkiel/lens-auth)

Lens Auth is a central OAuth 2.0 authorization server for all Lens backend
services.

Lens auth currently supports only the Resource Owner Password Credentials Grant
described in section [4.3][1] of the spec. An OAuth 2.0 compatible
[token endpoint][2] is implemented under the `/token` path. An OAuth 2.0
compatible [introspection endpoint][3] is implemented under the `/introspect`
path.

## Usage with Leiningen

To start the service with leiningen, run the following command

    lein with-profile production trampoline run

This starts the Lens Auth service on localhost port 8080.

## Usage on Heroku Compatible PaaS

This application uses the following environment vars:

* `PORT` - the port to listen on
* `CONTEXT_PATH` - an optional context path under which the workbook service runs

If you have [foreman][4] installed you can create an `.env` file listing the
environment vars specified above and just type `foreman start`.

## Develop

Running a REPL will load the user namespace. Use `(startup)` to start the server
and `(reset)` to reload after code changes.

## License

Copyright © 2015 Alexander Kiel

Distributed under the Eclipse Public License, the same as Clojure.

[1]: <http://tools.ietf.org/html/rfc6749#section-4.3>
[2]: <http://tools.ietf.org/html/rfc6749#section-3.2>
[3]: <https://tools.ietf.org/html/draft-ietf-oauth-introspection-08#section-2>
[4]: https://github.com/ddollar/foreman

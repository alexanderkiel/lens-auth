# Lens Auth

Lens Auth is a central OAuth 2.0 authorization server for all Lens backend
services.

Lens auth currently supports only the Resource Owner Password Credentials Grant
described in section [4.3][1] of the spec. An OAuth 2.0 compatible
[token endpoint][2] is implemented under the `/token` path. An OAuth 2.0
compatible [introspection endpoint][3] is implemented under the `/introspect`
path.

## Usage

* lein with-profile production trampoline run

## Usage on Heroku Compatible PaaS

This application uses the following environment vars:

* `PORT` - the port to listen on

[1]: <http://tools.ietf.org/html/rfc6749#section-4.3>
[2]: <http://tools.ietf.org/html/rfc6749#section-3.2>
[3]: <https://tools.ietf.org/html/draft-ietf-oauth-introspection-08#section-2>

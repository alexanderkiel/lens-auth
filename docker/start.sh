#!/usr/bin/env bash

lein with-profile production trampoline run -p 8080 -c ${CONTEXT_PATH:-/}

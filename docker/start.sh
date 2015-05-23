#!/usr/bin/env bash

lein with-profile production trampoline run -p 80 -c ${CONTEXT_PATH:-/}

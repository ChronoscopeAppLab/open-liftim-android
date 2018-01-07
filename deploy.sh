#!/usr/bin/env bash

GIT_SHA=`git rev-parse --short HEAD`

curl \
  -F "token=${DEPLOY_GATE_TOKEN}" \
  -F "file=@app/build/outputs/apk/debug/app-debug.apk" \
  -F "message=${GIT_SHA}" \
  https://deploygate.com/api/users/KoFuk/apps

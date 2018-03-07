#!/usr/bin/env bash

GIT_SHA=`git rev-parse --short HEAD`

echo "Executing DeployGate APK deployment..."

curl \
  -F "token=${DEPLOY_GATE_TOKEN}" \
  -F "file=@app/build/outputs/apk/debug/app-debug.apk" \
  -F "message=${GIT_SHA}" \
  https://deploygate.com/api/users/KoFuk/apps

echo "Saving ProGuard mapping file to Dropbox"

curl -X POST https://content.dropboxapi.com/2/files/upload \
  -H "Authorization: Bearer $DROPBOX_TOKEN" \
  -H "Dropbox-API-Arg: {\"path\": \"/liftim_mapping.txt\",\"mode\": \"overwrite\",\"autorename\": false,\"mute\": false}" \
  -H "Content-Type: application/octet-stream" \
  --data-binary @app/build/outputs/mapping/debug/mapping.txt

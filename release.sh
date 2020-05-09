#!/usr/bin/env bash

set -eu

level=$1

if [[ "${level}" == "major" ]]; then
    echo "major release"
elif [[ "${level}" == "minor" ]]; then
    echo "minor release"
elif [[ "${level}" == "patch" ]]; then
    echo "patch release"
else
    echo "./release.sh [major | minor | patch]"
    exit 1
fi

git fetch
branch_name=$(instant -f unix)
git checkout -b "release/${branch_name}" origin/master

git commit --allow-empty -m "release/${level}"

gh pr create \
  --base master \
  --title "release/${branch_name}" \
  --body "release/${branch_name}" 

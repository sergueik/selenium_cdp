#!/bin/sh


VERSION=${1:-106}

CURRENT_VERSION=$(printf "v%s" ${VERSION})
NEW_VERSION=$(printf "v%s" $(expr 1 + ${VERSION} ))
grep -ilr "$CURRENT_VERSION" * --include '*java' | xargs -IX sed -i "s|\.$CURRENT_VERSION|.$NEW_VERSION|g" X
grep -ilr "${NEW_VERSION}" * --include '*java' |xargs -IX git add X

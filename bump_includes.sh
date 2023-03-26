#!/bin/sh
# NOTE: after editing on Windows, fix the line endings to prevent
# /bin/sh^M: bad interpreter: No such file or directory error

VERSION=${1:-106}
NEW_VERSION=$2
CURRENT_VERSION=$(printf "v%s" ${VERSION})
if [ -z "$NEW_VERSION" ] ; then
  NEW_VERSION=$(printf "v%s" $(expr 1 + ${VERSION} ))
else
  NEW_VERSION=$(printf "v%s" ${NEW_VERSION})
fi
grep -ilr "$CURRENT_VERSION" src/* --include '*java' | xargs -IX sed -i "s|\.$CURRENT_VERSION|.$NEW_VERSION|g" X
grep -ilr "${NEW_VERSION}" src/* --include '*java' |xargs -IX git add X

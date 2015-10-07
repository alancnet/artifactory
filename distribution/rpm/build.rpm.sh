#!/bin/bash -e
set -x
if [ -z "$4" ]; then

    echo
    echo "Error: Usage is $0 productName fullVersion releaseNumber outBuildDir"
    exit 1
fi
FILENAME_PREFIX="$1"
FULL_VERSION="$2"
RELEASE_NUMBER="$3"
OUT_BUILD_DIR="$4"

curDir="`dirname $0`"
curDir="`cd $curDir; pwd`"
RPM_SOURCES_DIR="$OUT_BUILD_DIR/SOURCES"

if [ -z "$OUT_BUILD_DIR" ] || [ ! -d "$OUT_BUILD_DIR" ]; then
    echo
    echo "Error: The output directory $OUT_BUILD_DIR does not exists!"
    exit 1
fi

echo
ARTIFACTORY_VERSION=`echo "$FULL_VERSION" | sed 's/SNAPSHOT/devel/g; s/-/./g;'`

cd $curDir && rpmbuild -bb \
--define="_tmppath $OUT_BUILD_DIR/tmp" \
--define="_topdir $PWD" \
--define="_rpmdir $OUT_BUILD_DIR" \
--define="buildroot $OUT_BUILD_DIR/BUILDROOT" \
--define="_sourcedir $RPM_SOURCES_DIR" \
--define="artifactory_version $ARTIFACTORY_VERSION" \
--define="artifactory_release $RELEASE_NUMBER" \
--define="filename_prefix $FILENAME_PREFIX" \
--define="full_version $FULL_VERSION" \
SPECS/artifactory-oss.spec

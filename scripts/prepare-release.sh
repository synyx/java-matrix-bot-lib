#!/bin/bash
set -e

usage() {
	echo "Usage: prepare-release.sh <VERSION>"
	echo "  VERSION must be in semver format (e.g. 1.0.0)"
}

# Go to root dir
cd "$(dirname "$0")/.." || exit 2

VERSION="$1"

if [[ ! ("${VERSION}" =~ ^[0-9]+\.[0-9]\.[0-9]$) ]]; then
	usage
	exit 1
fi

STAGING_FOLDER="build/staging-deploy"
OUTFILE="java-matrix-bot-lib-${VERSION}.zip"
./gradlew -Pversion="${VERSION}" clean build publishJavaMatrixBotLibPublicationToLocalMavenWithChecksumsRepository
pushd "${STAGING_FOLDER}"
[ -e "${OUTFILE}" ] && rm -- "${OUTFILE}"
zip -r "${OUTFILE}" ./org/synyx/java-matrix-bot-lib
popd
echo ""
echo "Successfully created release ${STAGING_FOLDER}/${OUTFILE}"
echo "Deployment Name: org.synyx:java-matrix-bot-lib:${VERSION}"

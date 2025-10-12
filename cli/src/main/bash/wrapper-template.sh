#!/usr/bin/env bash

VERSION={{VERSION}}
URL=https://github.com/InkApplications/Squawk/releases/download/${VERSION}/squawk-${VERSION}.zip
SHA256={{HASH}}
INSTALL_DIR="$HOME/.local/share/squawk"
SQUAWK_BIN="$HOME/.local/share/squawk/${VERSION}/bin/squawk"

if [ ! -f "$SQUAWK_BIN" ]; then
    echo "Downloading squawk..."
    TEMP_FOLDER=$(mktemp -d)
    ZIP_FILE="${TEMP_FOLDER}/squawk.zip"
    curl -L "$URL" -o "$ZIP_FILE"
    echo "$SHA256 $ZIP_FILE" | sha256sum -c -
    if [ $? -ne 0 ]; then
        echo "Checksum verification failed!"
        exit 1
    fi
    mkdir -p "$INSTALL_DIR"
    unzip "$ZIP_FILE" -d "${INSTALL_DIR}"
    mv "${INSTALL_DIR}/squawk-${VERSION}" "${INSTALL_DIR}/${VERSION}"
fi

"$SQUAWK_BIN" $@

#!/usr/bin/env bash
set -euo pipefail

ENV="${1:-dev}"
CONTROLLER="${WILDFLY_CONTROLLER:-remote+https://localhost:9993}"

if [[ -z "${JBOSS_HOME:-}" ]]; then
    echo "ERROR: JBOSS_HOME must point to your WildFly 42 installation."
    exit 1
fi

if [[ "$ENV" != "dev" && "$ENV" != "prod" ]]; then
    echo "Usage: $0 [dev|prod]"
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "==> [1/4] Building backend (Java 25 / Jakarta EE 11)..."
mvn -f "$PROJECT_ROOT/backend/pom.xml" clean package -q

echo "==> [2/4] Building frontend PWA (Vite)..."
npm --prefix "$PROJECT_ROOT/frontend" ci
npm --prefix "$PROJECT_ROOT/frontend" run build

TARGET_WWW=$(grep '^default.path=' "$SCRIPT_DIR/default-host-${ENV}.properties" | cut -d'=' -f2)
echo "==> [3/4] Deploying frontend bundle to ${TARGET_WWW}..."
mkdir -p "${TARGET_WWW}"
rm -rf "${TARGET_WWW:?}/"*
cp -r "$PROJECT_ROOT/frontend/dist/"* "${TARGET_WWW}/"

MERGED_PROPS=$(mktemp)
trap 'rm -f "$MERGED_PROPS"' EXIT
cat "$SCRIPT_DIR/default-host-${ENV}.properties" "$SCRIPT_DIR/api-${ENV}.properties" > "$MERGED_PROPS"

echo "==> [4/4] Configuring Undertow and deploying to WildFly 42 (${ENV})..."
"${JBOSS_HOME}/bin/jboss-cli.sh" \
    --connect \
    --controller="${CONTROLLER}" \
    --file="$SCRIPT_DIR/default-host.cli" \
    --properties="$MERGED_PROPS"

"${JBOSS_HOME}/bin/jboss-cli.sh" \
    --connect \
    --controller="${CONTROLLER}" \
    --file="$SCRIPT_DIR/add-api.cli" \
    --properties="$MERGED_PROPS"

echo "==> SecureProof (${ENV}) deployment complete."

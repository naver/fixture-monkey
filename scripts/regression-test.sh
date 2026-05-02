#!/usr/bin/env bash
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

# Usage: ./scripts/regression-test.sh [--repeat N] [--target-tests "module:pattern"]
#
# Options:
#   --repeat N                Number of times to run target tests (default: 1)
#   --target-tests SPEC       Target test spec in "module:pattern" format (repeatable)
#
# Examples:
#   ./scripts/regression-test.sh
#   ./scripts/regression-test.sh --repeat 10 --target-tests "fixture-monkey:com.navercorp.fixturemonkey.adapter.ContainerAdapterTest"
#   ./scripts/regression-test.sh --repeat 5 --target-tests "fixture-monkey:com.navercorp.fixturemonkey.test.FixtureMonkeyTest"

REPEAT=1
TARGET_TESTS=()

while [[ $# -gt 0 ]]; do
  case $1 in
    --repeat)
      REPEAT="$2"
      shift 2
      ;;
    --target-tests)
      TARGET_TESTS+=("$2")
      shift 2
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

echo "=== Adapter Regression Tests ==="
echo "Started at: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# Run all regression tests once
./gradlew clean \
  :fixture-monkey:test --tests "com.navercorp.fixturemonkey.adapter.*" \
  :fixture-monkey-tests:java-tests:test --tests "com.navercorp.fixturemonkey.tests.java.adapter.*" \
  :fixture-monkey-tests:java-17-tests:test --tests "com.navercorp.fixturemonkey.tests.java17.adapter.*" \
  :fixture-monkey-tests:kotlin-tests:test --tests "com.navercorp.fixturemonkey.tests.kotlin.adapter.*"

echo ""
echo "=== All regression tests passed ==="

# Run target tests N times if specified
if [[ ${#TARGET_TESTS[@]} -gt 0 && $REPEAT -gt 1 ]]; then
  echo ""
  echo "=== Running target tests ${REPEAT} times ==="

  for i in $(seq 2 "$REPEAT"); do
    echo ""
    echo "--- Run $i/$REPEAT ---"

    GRADLE_ARGS=("clean")
    for spec in "${TARGET_TESTS[@]}"; do
      MODULE="${spec%%:*}"
      PATTERN="${spec#*:}"
      GRADLE_ARGS+=(":${MODULE}:test" "--tests" "$PATTERN")
    done

    ./gradlew "${GRADLE_ARGS[@]}"
  done

  echo ""
  echo "=== All ${REPEAT} runs passed ==="
fi

echo "Finished at: $(date '+%Y-%m-%d %H:%M:%S')"

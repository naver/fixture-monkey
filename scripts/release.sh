#!/bin/bash
set -euo pipefail

# 사용법:
#   ./scripts/release.sh <VERSION> [RELEASE_NOTES_FILE]
#
# 예시:
#   ./scripts/release.sh 1.1.16
#   ./scripts/release.sh 1.1.16 /tmp/release-notes.txt
#
# RELEASE_NOTES_FILE이 없으면 대화형으로 입력받는다.

if [[ $# -lt 1 ]]; then
	echo "Usage: $0 <VERSION> [RELEASE_NOTES_FILE]"
	echo "  VERSION: e.g. 1.1.16"
	echo "  RELEASE_NOTES_FILE: optional, path to a file containing release notes"
	exit 1
fi

VERSION=$1
RELEASE_NOTES_FILE=${2:-}

# 버전 파싱
IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"
NEXT_PATCH=$((PATCH + 1))
NEXT_VERSION="${MAJOR}.${MINOR}.${NEXT_PATCH}-SNAPSHOT"
DOCS_VERSION_DIR="v${MAJOR}.${MINOR}.x"

# 현재 버전 확인
CURRENT_VERSION=$(grep -oE 'version = "[^"]+"' build.gradle.kts | head -1 | grep -oE '"[^"]+"' | tr -d '"')
if [[ "$CURRENT_VERSION" != "${VERSION}-SNAPSHOT" ]]; then
	echo "ERROR: 현재 버전이 ${VERSION}-SNAPSHOT이 아닙니다 (현재: $CURRENT_VERSION)"
	exit 1
fi

# 이전 릴리즈 버전 추출 (README에서)
PREV_VERSION=$(grep -oE 'fixture-monkey-starter:[0-9]+\.[0-9]+\.[0-9]+' README.md | head -1 | grep -oE '[0-9]+\.[0-9]+\.[0-9]+')
if [[ -z "$PREV_VERSION" ]]; then
	echo "ERROR: README.md에서 이전 버전을 찾을 수 없습니다"
	exit 1
fi

echo "=== Release $VERSION ==="
echo "  이전 버전: $PREV_VERSION"
echo "  다음 SNAPSHOT: $NEXT_VERSION"
echo ""

# 릴리즈 노트 준비
if [[ -n "$RELEASE_NOTES_FILE" && -f "$RELEASE_NOTES_FILE" ]]; then
	NOTES=$(cat "$RELEASE_NOTES_FILE")
	echo "릴리즈 노트 (파일):"
	echo "$NOTES"
	echo ""
else
	echo "릴리즈 노트를 입력하세요 (빈 줄에서 Ctrl+D로 종료):"
	NOTES=$(cat)
	echo ""
fi

if [[ -z "$NOTES" ]]; then
	echo "ERROR: 릴리즈 노트가 비어있습니다"
	exit 1
fi

# 확인
echo "--- 변경 예정 ---"
echo "1. build.gradle.kts: $CURRENT_VERSION -> $VERSION"
echo "2. README.md: $PREV_VERSION -> $VERSION"
echo "3. docs config: $PREV_VERSION -> $VERSION"
echo "4. 릴리즈 노트 추가 (영문 + 한글)"
echo "5. 태그: $VERSION"
echo "6. 다음 버전: $NEXT_VERSION"
echo ""
read -p "진행할까요? (y/N) " CONFIRM
if [[ "$CONFIRM" != "y" && "$CONFIRM" != "Y" ]]; then
	echo "취소됨"
	exit 0
fi

# === 커밋 1: Release ===

# 1. build.gradle.kts
sed -i '' "s/version = \"${VERSION}-SNAPSHOT\"/version = \"${VERSION}\"/" build.gradle.kts

# 2. README.md
sed -i '' "s/${PREV_VERSION}/${VERSION}/g" README.md

# 3. docs/config/_default/config.toml
sed -i '' "s/version = \"${PREV_VERSION}\"/version = \"${VERSION}\"/" docs/config/_default/config.toml

# 4. docs/config/_default/params.toml (2곳)
sed -i '' "s/fixtureMonkeyVersion = \"${PREV_VERSION}\"/fixtureMonkeyVersion = \"${VERSION}\"/" docs/config/_default/params.toml
sed -i '' "/^\[\[params\.versions\]\]/,/^version/ s/version = \"${PREV_VERSION}\"/version = \"${VERSION}\"/" docs/config/_default/params.toml

# 5. 릴리즈 노트 삽입
for lang_suffix in "" "-kor"; do
	RELEASE_FILE="docs/content/${DOCS_VERSION_DIR}${lang_suffix}/release-notes/_index.md"
	if [[ -f "$RELEASE_FILE" ]]; then
		# 임시 파일로 릴리즈 노트 블록 생성
		TEMP_FILE=$(mktemp)
		{
			echo "### v${VERSION}"
			echo "$NOTES"
			echo ""
		} > "$TEMP_FILE"

		# "## vX.Y.x" 뒤에 삽입
		sed -i '' "/^## v${MAJOR}\.${MINOR}\.x$/r ${TEMP_FILE}" "$RELEASE_FILE"
		rm -f "$TEMP_FILE"
	else
		echo "WARNING: $RELEASE_FILE 가 없습니다. 건너뜁니다."
	fi
done

# 커밋 + 태그
git add build.gradle.kts README.md \
	docs/config/_default/config.toml \
	docs/config/_default/params.toml \
	"docs/content/${DOCS_VERSION_DIR}/release-notes/_index.md" \
	"docs/content/${DOCS_VERSION_DIR}-kor/release-notes/_index.md"

git commit -m "Release ${VERSION} version"
git tag -a "${VERSION}" -m "Release ${VERSION} version"

echo ""
echo "=== Release commit + tag 생성 완료 ==="

# === 커밋 2: Prepare next iteration ===

sed -i '' "s/version = \"${VERSION}\"/version = \"${NEXT_VERSION}\"/" build.gradle.kts
git add build.gradle.kts
git commit -m "Prepare next iteration"

echo "=== Prepare next iteration 커밋 생성 완료 ==="

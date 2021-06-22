#!/bin/bash

target=$1

if [[ -z ${target} ]];
then
    target="snapshot"
fi

if [[ "$target" != "snapshot" ]] && [[ "$target" != "release" ]]
then
     echo "./deploy.sh [snapshot|release]"
     exit 1
fi

echo ====== gradle clean build ======

cd ${0%/*} 2>/dev/null
$PWD/gradlew clean build -x test

echo ====== maven deploy ======

if [[ "$target" == "snapshot" ]];
then
    $PWD/gradlew publishMavenJavaPublicationToNavercorp.snapshotRepository
elif [[ "$target" == "release" ]];
then
    $PWD/gradlew publishMavenJavaPublicationToNavercorp.releaseRepository
else
    echo "Unknown parameter: $target"
    exit 1
fi

echo "====== deploy done ======"

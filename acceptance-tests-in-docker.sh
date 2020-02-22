#!/bin/bash

docker-compose up

EXIT_CODE=`docker-compose ps -q | xargs docker inspect -f '{{ .State.ExitCode }}' | grep -v '^0' | wc -l | tr -d ' '`
docker-compose down

exit ${EXIT_CODE}

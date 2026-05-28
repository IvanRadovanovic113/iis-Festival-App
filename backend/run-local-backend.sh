#!/usr/bin/env zsh

set -a
if [ -f "$(dirname "$0")/mail.env.local" ]; then
  source "$(dirname "$0")/mail.env.local"
fi
set +a

export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"

if [ -z "${SPRING_DATASOURCE_URL:-}" ]; then
  export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:55432/festival_db
fi

cd "$(dirname "$0")"
mvn spring-boot:run

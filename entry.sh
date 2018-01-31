#!/bin/sh

set -x

DEFAULT_KAFKA_GROUP_ID="matchmaking"
DEFAULT_DISCOVERY_MODEL="DISCOVERY"
DEFAULT_KAFKA_RESPONSE_TIMEOUT=10000

env_required() {
  echo "EnvironmentVariable $1 is required."
  exit 1
}

env_set_default() {
  echo "EnvironmentVariable $1 is not set. Defaulting to $2."
}

validateMandatory() {

  if [ -z "$KAFKA_BOOTSTRAP_SERVERS" ]; then
	  env_required "KAFKA_BOOTSTRAP_SERVERS"
  fi

}

setDefaults() {

  if [ -z "$KAFKA_GROUP_ID" ]; then
    export KAFKA_GROUP_ID=${DEFAULT_KAFKA_GROUP_ID}
	  env_set_default "KAFKA_GROUP_ID" "$DEFAULT_KAFKA_GROUP_ID"
  fi

  if [ -z "$KAFKA_RESPONSE_TIMEOUT" ]; then
    export KAFKA_RESPONSE_TIMEOUT=${DEFAULT_KAFKA_RESPONSE_TIMEOUT}
	  env_set_default "KAFKA_RESPONSE_TIMEOUT" "$DEFAULT_KAFKA_RESPONSE_TIMEOUT"
  fi

  if [ -z "$DISCOVERY_MODEL" ]; then
    export DISCOVERY_MODEL=${DEFAULT_DISCOVERY_MODEL}
	  env_set_default "DISCOVERY_MODEL" "$DEFAULT_DISCOVERY_MODEL"
  fi
}

validateMandatory
setDefaults

# Run the service
java -Dkafka.groupId=${KAFKA_GROUP_ID} -Dkafka.responseTimeout=${KAFKA_RESPONSE_TIMEOUT} -Dkafka.bootstrapServers=${KAFKA_BOOTSTRAP_SERVERS} -Dmatchmaking.discoveryModel=${DISCOVERY_MODEL} -jar matchmaking-agent.jar

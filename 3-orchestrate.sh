#!/bin/bash
set -eo pipefail
JSON_ORCHESTRATOR=$(aws cloudformation describe-stack-resource --stack-name java-sqs-performance --logical-resource-id AwsJsonOrchestrator --query 'StackResourceDetail.PhysicalResourceId' --output text)
QUERY_ORCHESTRATOR=$(aws cloudformation describe-stack-resource --stack-name java-sqs-performance --logical-resource-id AwsQueryOrchestrator --query 'StackResourceDetail.PhysicalResourceId' --output text)

if [ "$1" != "SEND" -a "$1" != "PROCESS" ]
then
  echo "Error! Usage: <sript> <operation>(SEND|PROCESS) <client>"
  exit 1
fi

if [ "$2" != "json" -a "$2" != "query" ]
then
  echo "Error! Valid usage: <sript> <operation> <client-type>(json|query)"
  exit 1
fi
orchestrator=""
if [ $2 == "json" ]
then
  orchestrator=$JSON_ORCHESTRATOR
else
  orchestrator=$QUERY_ORCHESTRATOR
fi
ticks=10
echo "Assigning $ticks ticks per instance. Each tick will produce/process approximately 250 messages."

entries="["
for i in `seq 1 $ticks`
do
    entries="$entries {\"Id\":\"$i\",\"MessageBody\":\"$1\"}"
    if [ $i -lt $ticks ]
    then
        entries="$entries,"
    fi
done
entries="$entries ]"
echo "Created entries..."
echo "Orchestrating $1 for $2 client with $orchestrator..."
while true; do
  aws sqs send-message-batch \
      --queue-url "$orchestrator" \
      --entries "$entries"  2>&1 > /dev/null
done
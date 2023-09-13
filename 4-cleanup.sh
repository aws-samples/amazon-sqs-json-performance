
#!/bin/bash
set -eo pipefail
STACK=java-sqs-performance
if [[ $# -eq 1 ]] ; then
    STACK=$1
    echo "Deleting stack $STACK"
fi
echo "Deleting $STACK stack."
aws cloudformation delete-stack --stack-name $STACK
echo "Deleted $STACK stack."
if [ -f bucket-name.txt ]; then
    ARTIFACT_BUCKET=$(cat bucket-name.txt)
    if [[ ! $ARTIFACT_BUCKET =~ lambda-artifacts-[a-z0-9]{16} ]] ; then
        echo "Bucket was not created by this application. Skipping."
    else
        while true; do
            read -p "Delete deployment artifacts and bucket ($ARTIFACT_BUCKET)? (y/n)" response
            case $response in
                [Yy]* ) aws s3 rb --force s3://$ARTIFACT_BUCKET; rm bucket-name.txt; break;;
                [Nn]* ) break;;
                * ) echo "Response must start with y or n.";;
            esac
        done
    fi
fi
rm -f out.yml out.json
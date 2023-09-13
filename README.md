## Testing infrastructure to compare Lambda performance with AWS/QUERY and AWS JSON SQS clients

![Architecture](/images/perf_test_arch.png)

The project source includes function code and supporting resources:
- `src/main/java/example/handler` - Java handlers for sending/processing messages using AwsJson and AwsQuery clients.
- `src/test` - A unit test and helper classes.
- `template.yml` - An AWS CloudFormation template that creates an application.
- `pom.xml` - A maven build file.
- `1-create-bucket.sh`, `2-deploy.sh`, etc. - Shell scripts that use the AWS CLI to deploy and manage the application.

Each Lambda handler function can be invoked for either SEND or PROCESS operations. SEND sends messages to the target queue. PROCESS receives and deletes messages from the target queue.

Use the following instructions to deploy the sample application.

## Requirements
- [Java 8 runtime environment (SE JRE)](https://www.oracle.com/java/technologies/javase-downloads.html)
- [Maven 3](https://maven.apache.org/docs/history.html)
- The Bash shell. For Linux and macOS, this is included by default. In Windows 10, you can install the [Windows Subsystem for Linux](https://docs.microsoft.com/en-us/windows/wsl/install-win10) to get a Windows-integrated version of Ubuntu and Bash.
- [The AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html) v1.17 or newer.

If you use the AWS CLI v2, add the following to your [configuration file](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html) (`~/.aws/config`):

```
cli_binary_format=raw-in-base64-out
```

This setting enables the AWS CLI v2 to load JSON events from a file, matching the v1 behavior.

Ensure your AWS CLI is set to call us-east-1 region.

```
aws configure set region us-east-1
```

## Setup
This can be run in your local terminal or an AWS Cloud9 workspace.
Download or clone this repository.

    $ cd ~
    $ git clone https://github.com/eddy-aws/amazon-sqs-json-performance.git
    $ cd amazon-sqs-json-performance

If maven is not installed, follow the steps to install maven:

    sudo mkdir /usr/local/apache-maven
    cd /usr/local/apache-maven
    sudo wget https://dlcdn.apache.org/maven/maven-3/3.9.0/binaries/apache-maven-3.9.0-bin.tar.gz
    sudo tar xvf apache-maven-3.9.0-bin.tar.gz

    export M2_HOME=/usr/local/apache-maven/apache-maven-3.9.0
    export M2=$M2_HOME/bin
    export PATH=$M2:$PATH
    cd ~/amazon-sqs-json-performance


Install AWS JSON SQS client locally and build. This enables the application to use the AWS JSON SDK version along with AWSQuery SDK version.


    mvn install:install-file \
    -Dfile=src/main/resources/aws-java-json-sdk-sqs-1.0.jar \
    -DgroupId=com.amazonaws.sqs \
    -DartifactId=aws-java-json-sdk-sqs \
    -Dversion=1.0 \
    -Dpackaging=jar \
    -DgeneratePom=true

    mvn package


To create a new bucket for deployment artifacts, run `1-create-bucket.sh`.

    amazon-sqs-json-performance$ ./1-create-bucket.sh
    make_bucket: lambda-artifacts-a5e4xmplb5b22e0d


### Deploy
To deploy the application, run `2-deploy.sh`.

    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time:  7.025 s
    [INFO] Finished at: 2023-03-14T01:46:14-07:00
    [INFO] ------------------------------------------------------------------------

    Successfully packaged artifacts and wrote output template to file out.yml.
    Execute the following command to deploy the packaged template
    aws cloudformation deploy --template-file /Users/ezhilani/workspace/amazon-sqs-json-performance/out.yml --stack-name <YOUR STACK NAME>

    Waiting for changeset to be created..
    Waiting for stack create/update to complete
    Successfully created/updated stack - java-sqs-performance


This script uses AWS CloudFormation to deploy following resources:
* Target test queue
* AWS JSON and AWS/QUERY orchestrator queues, lambda functions and event source mappings
* IAM role needed for the lambda functions
* Cloudwatch dashboard to monitor results

### Test
- SEND orchestrates the handlers to send message to the target queue
- PROCESS orchestrates the handlers to receive and delete messages from the target queue

In two terminals, run the orchestrator commands:

To orchestrate AWS/QUERY handler for SEND operation, run `./3-orchestrate.sh SEND query`
To orchestrate AWS JSON handler for SEND operation, run `./3-orchestrate.sh SEND json`

You could use `timeout` command to automatically timeout the runs after some period of time. Or you can press `CRTL+C` to exit the run

### Analyze results
Open the `SqsLambdaDashboard-us-east-1` Cloudwatch dashboard and analyze the metrics. The dashboard contains
- Invocation duration metrics, CPU usage metrics of two lambda functions that uses Query and JSON clients. The reduction percentage is also plotted
- Lambda function network usage and invocation metrics
- Target queue metrics

![Dashboard](/images/dashboard.png)

### Cleanup
To delete the application, run `4-cleanup.sh`.

    $ ./4-cleanup.sh
    Deleting java-sqs-performance stack.
    Delete deployment artifacts and bucket (lambda-artifacts-96b468fea941fce9)? (y/n)y
    Deleting log groups.

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the MIT-0 License. See the LICENSE file.
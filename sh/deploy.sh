#! /bin/bash

SHA1=$1

cd docker
# Create new Elastic Beanstalk version
EB_BUCKET=elasticbeanstalk-us-west-2-525932482084

zip $CIRCLE_ARTIFACTS/signals Dockerfile Dockerrun.aws.json signals.jar

aws s3 cp $CIRCLE_ARTIFACTS/signals.zip s3://$EB_BUCKET/coinsignals/signals-$SHA1.zip
aws elasticbeanstalk create-application-version --application-name coinsignals --version-label $SHA1 --source-bundle S3Bucket=$EB_BUCKET,S3Key=coinsignals/signals-$SHA1.zip

# Update Elastic Beanstalk environment to new version
aws elasticbeanstalk update-environment --environment-name signals-staging --version-label $SHA1
aws ecs create-service \
  --cluster apartment-hunter-cluster \
  --service-name apartment-hunter \
  --task-definition apartment-hunter-task \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-0fd3a535ace50518d],assignPublicIp=ENABLED}" \
  --profile apartment-hunter
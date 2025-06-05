aws ecs update-service \
  --cluster apartment-hunter-cluster \
  --service apartment-hunter \
  --task-definition apartment-hunter-task \
  --desired-count 1 \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-0fd3a535ace50518d],securityGroups=[sg-0d282c48ba8b5abfa],assignPublicIp=ENABLED}" \
  --profile apartment-hunter
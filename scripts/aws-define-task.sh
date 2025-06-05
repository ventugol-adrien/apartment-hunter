TAG_DATA=$(curl -s 'https://hub.docker.com/v2/repositories/ventugoladrien/apartment-hunter/tags?page_size=1&page=1&ordering=last_updated')
TAG=$(echo "$TAG_DATA" | jq -r '.results[0].name')
echo "Using tag: $TAG"
CONTAINER_DEFINITIONS="[
    {
      \"name\": \"apartment-hunter\",
      \"image\": \"763927202345.dkr.ecr.eu-west-1.amazonaws.com/ventugoladrien/apartment-hunter:$TAG\",
      \"portMappings\": [
        {
          \"containerPort\": 8080,
          \"hostPort\": 8080,
          \"protocol\": \"tcp\"
        }
      ],
      \"command\" : [\"java\", \"-jar\", \"apartment-hunter-1.0.jar\"],
      \"secrets\"
      : [
        { \"name\": \"apartment-hunter-secrets\", \"valueFrom\": \"arn:aws:secretsmanager:eu-west-1:763927202345:secret:apartment-hunter-secrets-9SMXIh\" }
      ],
      \"environment\": [
        { \"name\": \"PORT\", \"value\": \"8080\"}
      ],
      \"logConfiguration\": {
        \"logDriver\": \"awslogs\",
        \"options\": {
          \"awslogs-group\": \"/ecs/apartment-hunter\",
          \"awslogs-region\": \"eu-west-1\",
          \"awslogs-stream-prefix\": \"ecs\",
          \"awslogs-create-group\": \"true\",
          \"awslogs-datetime-format\": \"%Y-%m-%dT%H:%M\",
          \"mode\": \"non-blocking\",
          \"max-buffer-size\": \"50m\"
        }
      }
    }
  ]"

aws ecs register-task-definition \
  --family apartment-hunter-task \
  --execution-role-arn arn:aws:iam::763927202345:role/ecs-admin \
  --task-role-arn arn:aws:iam::763927202345:role/ecs-admin \
  --profile apartment-hunter \
  --network-mode awsvpc \
  --requires-compatibilities FARGATE \
  --cpu "256" \
  --memory "512" \
  --container-definitions "${CONTAINER_DEFINITIONS}"
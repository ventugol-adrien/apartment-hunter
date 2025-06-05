TAG_DATA=$(curl -s 'https://hub.docker.com/v2/repositories/ventugoladrien/apartment-hunter/tags?page_size=1&page=1&ordering=last_updated')
TAG=$(echo "$TAG_DATA" | jq -r '.results[0].name')
VERSION=$(grep -o '[0-9]\+' <<< "$TAG")
NEW_VERSION=$(("$VERSION" + 1))
NEW_TAG="v${NEW_VERSION}"
echo "new tag: $NEW_TAG"

docker build -t ventugoladrien/apartment-hunter:"$NEW_TAG" -f .docker/Dockerfile .
docker tag ventugoladrien/apartment-hunter:"$NEW_TAG" 763927202345.dkr.ecr.eu-west-1.amazonaws.com/ventugoladrien/apartment-hunter:"$NEW_TAG"

docker push ventugoladrien/apartment-hunter:"$NEW_TAG"
docker push 763927202345.dkr.ecr.eu-west-1.amazonaws.com/ventugoladrien/apartment-hunter:"$NEW_TAG"
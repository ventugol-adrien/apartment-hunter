TAG_DATA=$(curl -s 'https://hub.docker.com/v2/repositories/ventugoladrien/apartment-hunter/tags?page_size=1&page=1&ordering=last_updated')
TAG=$(echo "$TAG_DATA" | jq -r '.results[0].name')
docker run --rm -p 8080:8080 ventugoladrien/apartment-hunter:"$TAG"
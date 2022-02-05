#!/bin/bash
#set -x #echo on

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export SERVICE_NAME=zl_aws_amplify_poc_docker_image_run
export WORK_DIR=/project

#if you want to start node command use 'docker-entrypoint.sh node' for example

docker run --rm -it --network host \
	--name=$SERVICE_NAME \
	-p 3000:3000 \
	-v $DIR/root:/root \
	-v $DIR:$WORK_DIR \
	-v /tmp/$SERVICE_NAME:/tmp \
	-w $WORK_DIR \
	zerolabs/aws-amplify-poc:latest \
	$*

#!/bin/bash

mkdir -p /gardle/images
mkdir -p /gardle/logs

s3fs images.gardle.ga /gardle/images/ -o use_path_request_style -o endpoint=eu-central-1 -o url="https://s3-eu-central-1.amazonaws.com"

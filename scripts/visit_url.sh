#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 <url>"
  exit 1
fi

curl -sS "https://r.jina.ai/$1"

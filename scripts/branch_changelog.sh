#!/bin/bash

git log --no-merges --oneline --decorate master..HEAD --pretty="- %s" --reverse > changelog.txt
cat changelog.txt

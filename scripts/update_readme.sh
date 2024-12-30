rm ./docs/docs/README.md
echo "---
title: FlowMVI
title_meta: FlowMVI - Kotlin Architecture Framework
sidebar_label: Home
sidebar_position: 0
hide_title: true
description: undefined
slug: /
---
" > ./docs/docs/README.md

cat ./README.md >> ./docs/docs/README.md

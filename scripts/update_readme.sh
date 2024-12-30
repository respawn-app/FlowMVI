rm ./docs/docs/README.md
echo "---
title: FlowMVI
title_meta: FlowMVI - Kotlin Architecture Framework
sidebar_label: Home
sidebar_position: 0
hide_title: true
description: Architecture Framework for Kotlin. Reuse every line of code. Handle all errors automatically. No boilerplate. Analytics, metrics, debugging in 3 lines. 50+ features.
slug: /
---
" > ./docs/docs/README.md

cat ./README.md >> ./docs/docs/README.md
